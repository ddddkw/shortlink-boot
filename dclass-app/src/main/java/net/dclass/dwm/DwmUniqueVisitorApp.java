package net.dclass.dwm;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import net.dclass.func.UniqueVisitorFilterFunction;
import net.dclass.util.KafkaUtil;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;

public class DwmUniqueVisitorApp {

    /**
     * 定义source topic
     */
    public static final String SOURCE_TOPIC = "dwm_link_visit_topic";

    /**
     * 定义消费者组
     */
    public static final String GROUP_ID = "dwm_unique_visitor_group";

    /**
     * 定义输出的主题
     */
    public static final String SINK_TOPIC = "dwm_unique_visitor_topic";


    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(1);

        //DataStream<String> ds =  env.socketTextStream("127.0.0.1",8888);

        //1、获取数据流
        FlinkKafkaConsumer<String> kafkaConsumer = KafkaUtil.getKafkaConsumer(SOURCE_TOPIC,GROUP_ID);

        // 配置数据源
        DataStreamSource<String> ds = env.addSource(kafkaConsumer);

        //2、对数据源中的数据进行转换
        SingleOutputStreamOperator<JSONObject> jsonDS = ds.map(jsonStr -> JSON.parseObject(jsonStr));

        //3、使用udid进行分组（udid需要使用设备id和用户id做唯一区分）
        KeyedStream<JSONObject, String> keyedStream = jsonDS.keyBy(new KeySelector<JSONObject, String>() {
            @Override
            public String getKey(JSONObject value) throws Exception {
                return value.getString("udid");
            }
        });

        //4、排重过滤
        SingleOutputStreamOperator<JSONObject> filterDS = keyedStream.filter(new UniqueVisitorFilterFunction());

        filterDS.print("独立访客");

        //5、转成字符串写入kafka
        SingleOutputStreamOperator<String> uniqueVisitorDS = filterDS.map(obj -> obj.toJSONString());

        FlinkKafkaProducer<String> kafkaProducer = KafkaUtil.getKafkaProducer(SINK_TOPIC);

        uniqueVisitorDS.addSink(kafkaProducer);

        env.execute();

    }


}
