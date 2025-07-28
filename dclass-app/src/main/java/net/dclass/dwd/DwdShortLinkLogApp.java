package net.dclass.dwd;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import net.dclass.func.VisitorMapFunction;
import net.dclass.util.DeviceUtil;
import net.dclass.util.KafkaUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.util.Collector;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

/**
 * 对kafka中存储的原始数据进行处理
 * 1. 将原始字符串解析为JSONObject
 * 2. 生成设备唯一ID（udid）并添加到JSON中
 * 3. 提取referer的主机名并添加到JSON中
 * 4. 记录是新旧访客
 * 5. 将处理后的数据存储到kafka对应的topic下
 */
@Slf4j
public class DwdShortLinkLogApp {

    private static final String SOURCE_TOPIC = "ods_link_visit_topic";

    private static final String SINK_TOPIC = "dwd_link_visit_topic";

    private static final String GROUP_ID = "dwd_short_link_group";

    public static void  main(String [] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(1);
        DataStreamSource<String> ds =env.socketTextStream("127.0.0.1",8888);

        /**
         通过 FlinkKafkaConsumer 从 ODS 层 Kafka 主题（ods_link_visit_topic）读取原始日志。
          */
        // FlinkKafkaConsumer kafkaConsumer = KafkaUtil.getKafkaConsumer(SOURCE_TOPIC,GROUP_ID);
        // DataStreamSource<String> ds = env.addSource(kafkaConsumer);

        ds.print();

        // 数据补齐
        SingleOutputStreamOperator<JSONObject> jsonDS = ds.flatMap(new FlatMapFunction<String, JSONObject>() {
            @Override
            public void flatMap(String value, Collector<JSONObject> out) throws Exception {

                JSONObject jsonObject = JSON.parseObject(value);
                //生成设备唯一id
                String udid = getDeviceId(jsonObject);
                jsonObject.put("udid",udid);

                String referer = getReferer(jsonObject);
                jsonObject.put("referer",referer);

                out.collect(jsonObject);
            }
        });

        // 分组，通过udid进行分组，使用设备id保证唯一性，确保每个udid可以被路由到固定的分组，针对每个udid都有一个状态管理字段
        KeyedStream<JSONObject, String> keyedStream = jsonDS.keyBy(new KeySelector<JSONObject, String>() {
            @Override
            public String getKey(JSONObject value) throws Exception {
                return value.getString("udid");
            }
        });

        // 识别 richMap open函数，做状态存储的初始化
        SingleOutputStreamOperator<String> jsonDSWithVisitorState = keyedStream.map(new VisitorMapFunction());

        jsonDSWithVisitorState.print("ods新老访客");

        // 存储
        FlinkKafkaProducer<String> kafkaProducer = KafkaUtil.getKafkaProducer(SINK_TOPIC);
        jsonDSWithVisitorState.addSink(kafkaProducer);

        env.execute();
    }

    public static String getReferer(JSONObject jsonObject) throws MalformedURLException {
        JSONObject jsonDataObject = jsonObject.getJSONObject("data");
        if (jsonDataObject.containsKey("referer")){
            String referer = jsonDataObject.getString("referer");
            if (StringUtils.isNotBlank(referer)) {
                try {
                    URL url =new URL(referer);
                    return url.getHost();
                }catch (MalformedURLException e){
                    log.error("提取referer失败", e);
                }
            }
        }
        return "";

    }

    public static String getDeviceId(JSONObject jsonObject){
        Map map = new TreeMap();
        try {
            map.put("ip",jsonObject.getString("ip"));
            map.put("event",jsonObject.getString("event"));
            map.put("bizId",jsonObject.getString("bizId"));
            String userAgent = jsonObject.getJSONObject("data").getString("user-agent");
            map.put("userAgent",userAgent);

            String deviceId = DeviceUtil.geneWebUniqueDeviceId(map);
            return deviceId;
        } catch (Exception e){
            log.error("生成唯一deviceId异常：{}", jsonObject);
            return null;
        }
    }
}
