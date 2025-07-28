package net.dclass.dwm;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import net.dclass.func.DeviceMapFunction;
import net.dclass.func.LocationMapFunction;
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
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
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
     * 定义数据插入 topic，定义数据源，dwm层处理后的数据进行存储
     */
    public static final String SINK_TOPIC = "dwm_link_visit_topic";

    /**
     * 定义消费者组，一个消费者组下面可以有多个消费者
     */
    public static final String GROUP_ID = "dwm_short_link_group";


    public static void main(String [] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(1);

        //DataStream<String> ds =  env.socketTextStream("127.0.0.1",8888);

        FlinkKafkaConsumer<String> kafkaConsumer = KafkaUtil.getKafkaConsumer(SOURCE_TOPIC, GROUP_ID);

        // 配置数据源
        DataStreamSource<String> ds = env.addSource(kafkaConsumer);


        /**
         * deviceWideDS 和 shortLinkWideDs 是 Flink 流处理中的 DataStream（数据流）
         */
        // 格式装换，补齐设备信息
        SingleOutputStreamOperator<ShortLinkWideDO> deviceWideDS = ds.map(new DeviceMapFunction());
        deviceWideDS.print("设备信息宽表补齐");

        // 在原本设备信息的基础上补齐地理位置信息
        SingleOutputStreamOperator<String> shortLinkWideDs = deviceWideDS.map(new LocationMapFunction());

        shortLinkWideDs.print("地理位置信息宽表补齐");

        FlinkKafkaProducer<String> kafkaProducer = KafkaUtil.getKafkaProducer(SINK_TOPIC);

        // kafka存储地理位置信息
        shortLinkWideDs.addSink(kafkaProducer);

        env.execute();
    }
}
