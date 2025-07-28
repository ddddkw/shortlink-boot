package net.dclass.dwm;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import net.dclass.model.DeviceInfoDO;
import net.dclass.model.ShortLinkWideDO;
import net.dclass.util.DeviceUtil;
import net.dclass.util.KafkaUtil;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;

/**
 * 对dwd层数据进一步处理
 * 补齐设备相关信息、
 * group_id 是 Kafka 实现分布式消费的核心机制，其意义可概括为：
 * 并行消费：通过多个消费者分担分区，提高吞吐量；
 * 进度管理：统一维护消费偏移量，确保消费状态一致；
 * 场景隔离：不同组独立消费同一 Topic，满足多业务需求。
 **/

public class DwmShortLinWideApp {

    /**
     * 定义source topic，定义数据源，从dwd层读取已经处理过一次的数据
     */
    public static final String SOURCE_TOPIC = "dwd_link_visit_topic";

    /**
     * 定义消费者组，一个消费者组下面可以有多个消费者
     */
    public static final String GROUP_ID = "dwm_short_link_group";


    public static void main(String [] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(1);

        //DataStream<String> ds =  env.socketTextStream("127.0.0.1",8888);

        FlinkKafkaConsumer<String> kafkaConsumer = KafkaUtil.getKafkaConsumer(SOURCE_TOPIC, GROUP_ID);

        DataStreamSource<String> ds = env.addSource(kafkaConsumer);


        //格式装换，补齐设备信息
        SingleOutputStreamOperator<ShortLinkWideDO> deviceWideDS = ds.flatMap(new FlatMapFunction<String, ShortLinkWideDO>() {
            @Override
            public void flatMap(String value, Collector<ShortLinkWideDO> out) throws Exception {

                //还原json
                JSONObject jsonObject = JSON.parseObject(value);

                String userAgent = jsonObject.getJSONObject("data").getString("user-agent");

                DeviceInfoDO deviceInfoDO = DeviceUtil.getDeviceInfo(userAgent);

                String udid = jsonObject.getString("udid");
                deviceInfoDO.setUdid(udid);

                //配置短链基本信息宽表
                ShortLinkWideDO shortLinkWideDO = ShortLinkWideDO.builder()
                        //短链访问基本信息
                        .visitTime(jsonObject.getLong("ts"))
                        .accountNo(jsonObject.getJSONObject("data").getLong("accountNo"))
                        .code(jsonObject.getString("bizId"))
                        .referer(jsonObject.getString("referer"))
                        .isNew(jsonObject.getInteger("is_new"))
                        .ip(jsonObject.getString("ip"))

                        //设备信息补齐
                        .browserName(deviceInfoDO.getBrowserName())
                        .os(deviceInfoDO.getOs())
                        .osVersion(deviceInfoDO.getOsVersion())
                        .deviceType(deviceInfoDO.getDeviceType())
                        .deviceManufacturer(deviceInfoDO.getDeviceManufacturer())
                        .udid(deviceInfoDO.getUdid())

                        .build();

                out.collect(shortLinkWideDO);

            }
        });


        deviceWideDS.print("设备信息宽表补齐");

        env.execute();
    }
}
