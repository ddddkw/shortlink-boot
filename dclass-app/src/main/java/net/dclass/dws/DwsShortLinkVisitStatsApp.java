package net.dclass.dws;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import net.dclass.model.ShortLinkVisitStatsDO;
import net.dclass.util.KafkaUtil;
import net.dclass.util.MyClickHouseSink;
import net.dclass.util.TimeUtil;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple9;
import org.apache.flink.streaming.api.datastream.*;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;

import java.time.Duration;

/**
 * 短链接服务需要统计访问数据（如点击量 PV、独立访客量 UV），并按多维度（如访问来源、地区、设备类型等）分析
 **/

public class DwsShortLinkVisitStatsApp {


    /**
     * 宽表，数据来源表
     */
    public static final String SHORT_LINK_SOURCE_TOPIC = "dwm_link_visit_topic";

    public static final String SHORT_LINK_SOURCE_GROUP = "dws_link_visit_group";

    /**
     * uv的数据流
     */
    public static final String UNIQUE_VISITOR_SOURCE_TOPIC = "dwm_unique_visitor_topic";

    public static final String UNIQUE_VISITOR_SOURCE_GROUP = "dws_unique_visitor_group";





    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(1);

        //1、获取多个数据
        FlinkKafkaConsumer<String> shortLinkSource = KafkaUtil.getKafkaConsumer(SHORT_LINK_SOURCE_TOPIC,SHORT_LINK_SOURCE_GROUP);
        DataStreamSource<String> shortLinkDS = env.addSource(shortLinkSource);

        FlinkKafkaConsumer<String> uniqueVisitorSource = KafkaUtil.getKafkaConsumer(UNIQUE_VISITOR_SOURCE_TOPIC, UNIQUE_VISITOR_SOURCE_GROUP);
        DataStreamSource<String> uniqueVisitorDS = env.addSource(uniqueVisitorSource);

        /**
         * 对dwm层已进行处理过的数据进行统计分析
         */
        //2、结构转换 uniqueVisitorDS、shortLinkDS
        // 处理PV数据流：每条记录代表1次访问，PV=1，UV=0
        SingleOutputStreamOperator<ShortLinkVisitStatsDO> shortLinkMapDS = shortLinkDS.map(new MapFunction<String, ShortLinkVisitStatsDO>() {
            @Override
            public ShortLinkVisitStatsDO map(String value) throws Exception {

                ShortLinkVisitStatsDO visitStatsDO = parseVisitStats(value);
                visitStatsDO.setPv(1L);
                visitStatsDO.setUv(0L);
                return visitStatsDO;
            }
        });

        // 处理UV数据流：每条记录代表1个独立访客，UV=1，PV=0
        SingleOutputStreamOperator<ShortLinkVisitStatsDO> uniqueVisitorMapDS = uniqueVisitorDS.map(new MapFunction<String, ShortLinkVisitStatsDO>() {
            @Override
            public ShortLinkVisitStatsDO map(String value) throws Exception {
                ShortLinkVisitStatsDO visitStatsDO = parseVisitStats(value);
                visitStatsDO.setPv(0L);
                visitStatsDO.setUv(1L);
                return visitStatsDO;
            }
        });



        //3、多流合并（合并相同结构的流）
        DataStream<ShortLinkVisitStatsDO> unionDS = shortLinkMapDS.union(uniqueVisitorMapDS);

        //4、设置WaterMark

        SingleOutputStreamOperator<ShortLinkVisitStatsDO> watermarkDS = unionDS.assignTimestampsAndWatermarks(WatermarkStrategy
                //指定允许乱序延迟最大3秒
                .<ShortLinkVisitStatsDO>forBoundedOutOfOrderness(Duration.ofSeconds(3))
                //指定事件时间列，毫秒
                .withTimestampAssigner((event, timestamp) -> event.getVisitTime()));

        /**
         * 5、多维度、多个字段分组，进行分组聚合
         * 分组就是对这九个字段中所有可能的字段做一个笛卡尔积，再根据得到的结果进行逻辑分组
         * 类似于三个字段A,B,C，每个字段下有三种不同的数值，则最后的结果就是3*3*3=27种结果，相应的就会有27种逻辑分组
         * 按 9 个维度分组（Tuple9包含 9 个字段），确保同一维度组合的访问数据被聚合到一起：
         * 业务维度：code（短链接标识）、referer（访问来源）、isNew（是否新用户）
         * 地理维度：province（省份）、city（城市）、ip（IP 地址）
         * 设备维度：browserName（浏览器）、os（操作系统）、deviceType（设备类型）
         */
        KeyedStream<ShortLinkVisitStatsDO, Tuple9<String, String, Integer, String, String, String, String, String, String>> keyedStream = watermarkDS.keyBy(new KeySelector<ShortLinkVisitStatsDO, Tuple9<String, String, Integer, String, String, String, String, String, String>>() {
            @Override
            public Tuple9<String, String, Integer, String, String, String, String, String, String> getKey(ShortLinkVisitStatsDO obj) throws Exception {
                return Tuple9.of(obj.getCode(), obj.getReferer(), obj.getIsNew(),
                        obj.getProvince(), obj.getCity(), obj.getIp(),
                        obj.getBrowserName(), obj.getOs(), obj.getDeviceType());
            }
        });

        /**
         * 窗口：是流数据的 “时间切片”，将无限流转化为有限数据集，便于批量计算。
         * 开窗：通过代码定义窗口的大小、类型（如滚动窗口）、时间基准（如事件时间）等规则，指定 “如何切分数据”。
         * 将每十秒的数据进行统计插入到clickhouse
         * -------------------------------------------------------
         * 窗口类型：TumblingEventTimeWindows表示基于事件时间的滚动窗口
         * 窗口长度固定为 10 秒，相邻窗口无重叠，每个事件仅属于一个窗口。
         * 时间语义：EventTime使用数据自带的时间戳（如日志中的事件发生时间），而非系统处理时间，可处理乱序事件。但是此处未使用
         * 触发时机：窗口结束时自动触发计算（如 10:00:00~10:00:10 的窗口在水位线越过 10:00:10 时触发）。
         */

        //6、开窗 10秒一次数据插入到 ck
        WindowedStream<ShortLinkVisitStatsDO, Tuple9<String, String, Integer, String, String, String, String, String, String>, TimeWindow> windowedStream =
                keyedStream.window(TumblingEventTimeWindows.of(Time.seconds(10)));


        /**
         * 7、聚合统计(补充统计起止时间)
         * 聚合就是对每一个逻辑分组下的数据进行处理，如将某一逻辑分组下的pv，uv进行累加就可以得到符合这一条件的pv总量和uv总量
         */
        SingleOutputStreamOperator<Object> reduceDS = windowedStream.reduce(new ReduceFunction<ShortLinkVisitStatsDO>() {
            /**
             * 对窗口内同一维度的记录进行 PV 和 UV 的累加
             * （例如：10 秒内同一短链接、同一地区、同一设备的所有访问，PV 总和和 UV 总和）
             *  reduce -- 增量聚合
             *  作用：窗口内每到来一条新数据，立即与当前聚合结果合并，减少状态存储开销。
             *  优势：适合高频写入场景，避免全量遍历窗口数据
             */
            @Override
            public ShortLinkVisitStatsDO reduce(ShortLinkVisitStatsDO value1, ShortLinkVisitStatsDO value2) throws Exception {
                value1.setPv(value1.getPv() + value2.getPv());
                value1.setUv(value1.getUv() + value2.getUv());

                return value1;
            }
        }, new ProcessWindowFunction<ShortLinkVisitStatsDO, Object, Tuple9<String, String, Integer, String, String, String, String, String, String>, TimeWindow>() {
            @Override
            public void process(Tuple9<String, String, Integer, String, String, String, String, String, String> tuple,
                                Context context, Iterable<ShortLinkVisitStatsDO> elements, Collector<Object> out) throws Exception {
                /**
                 * 全量聚合
                 * 作用：为增量聚合结果补充窗口元信息（起止时间），并逐条输出。
                 * 关键对象：
                 * context.window()：获取当前窗口的时间范围。
                 * elements：包含窗口内所有数据（此处仅含 reduce输出的聚合结果）
                 */
                for (ShortLinkVisitStatsDO visitStatsDO : elements) {
                    //窗口开始和结束时间
                    String startTime = TimeUtil.formatWithTime(context.window().getStart());
                    String endTime = TimeUtil.formatWithTime(context.window().getEnd());
                    visitStatsDO.setStartTime(startTime);
                    visitStatsDO.setEndTime(endTime);
                    out.collect(visitStatsDO);
                }

            }
        });

        reduceDS.print(">>>>>>");

        //8、输出Clickhouse
        String sql = "insert into visit_stats values(?,?,?,? ,?,?,?,? ,?,?,?,? ,?,?,?)";


        /**
         * reduceDS是聚合后的数据流，每条数据都是一个ShortLinkVisitStatsDO对象（包含完整的统计字段）。
         * addSink是 Flink 的终端操作，将数据流与MyClickHouseSink生成的SinkFunction绑定(MyClickHouseSink.getJdbcSink方法返回的是一个SinkFunction)
         * 我的理解是SinkFunction就是一个插入数据库的方法，每当有数据输出时，就调用方法，将输出的数据写入到ClickHouse
         * 当数据流中有数据输出时，自动调用SinkFunction将数据写入 ClickHouse。
         */

        reduceDS.addSink(MyClickHouseSink.getJdbcSink(sql));

        env.execute();

    }




    private static ShortLinkVisitStatsDO parseVisitStats(String value) {

        JSONObject jsonObj = JSON.parseObject(value);

        ShortLinkVisitStatsDO visitStatsDO = ShortLinkVisitStatsDO.builder()
                .code(jsonObj.getString("code"))
                .accountNo(jsonObj.getLong("accountNo"))
                .visitTime(jsonObj.getLong("visitTime"))
                .referer(jsonObj.getString("referer"))
                .isNew(jsonObj.getInteger("isNew"))
                .udid(jsonObj.getString("udid"))

                //地理位置信息
                .province(jsonObj.getString("province"))
                .city(jsonObj.getString("city"))
                .isp(jsonObj.getString("isp"))
                .ip(jsonObj.getString("ip"))

                //设备信息
                .browserName(jsonObj.getString("browserName"))
                .os(jsonObj.getString("os"))
                .osVersion(jsonObj.getString("osVersion"))
                .deviceType(jsonObj.getString("deviceType"))
                .deviceManufacturer(jsonObj.getString("deviceManufacturer"))

                .build();

        return visitStatsDO;
    }

}
