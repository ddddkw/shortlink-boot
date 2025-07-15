package org.example.config;

import lombok.Data;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Data
public class RabbitMQConfig {

    @Bean
    public Exchange trafficEventExchange(){
        return new TopicExchange(trafficEventExchange,true,false);
    }

    /**
     * 交换机
     */
    private String trafficEventExchange = "traffic.event.exchange";

    /**
     * 免费流量包队列名称
     */
    private String trafficFreeInitQueue = "traffic.free_init.queue";

    /**
     * 进入到免费流量包队列的routingkey
     */
    private String trafficFreeInitRoutingKey = "traffic.free_init.routing.key";

    /**
     * 过期时间，毫秒为单位，临时改为1分钟过期
     */
    private Integer ttl = 1000 * 60;

    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 新用户注册免费流量包
     * @return
     */
    @Bean
    public Binding orderCloseBinding(){
        return new Binding(trafficFreeInitQueue,Binding.DestinationType.QUEUE, trafficEventExchange,trafficFreeInitRoutingKey,null);
    }

    //======================================流量包扣减，创建短链死信队列配置===================================
    /**
     * 延迟队列名称
     */
    private String trafficReleaseDelayQueue = "traffic.release.delay.queue";

    /**
     * 延迟队列的routingkey
     */
    private String trafficReleaseDelayRoutingKey = "traffic.release.delay.routing.key";

    /**
     * 恢复流量包队列名称
     */
    private String trafficReleaseQueue = "traffic.release.queue";

    /**
     * 延迟队列的routingkey
     */
    private String trafficReleaseRoutingKey = "traffic.release.routing.key";

    /**
     * 延迟队列的配置
     * 生产者发送消息：消息被发送到trafficReleaseDelayQueue队列
     * 消息等待 TTL 过期：消息在队列中停留ttl毫秒，在消息过期后会自动根据key和交换机及将消息路由到死信队列即trafficReleaseQueue
     * 消息变为死信：TTL 过期后，消息被路由到trafficEventExchange交换器
     * 消费者消费消息：绑定到trafficEventExchange的队列（通过trafficReleaseRoutingKey路由键）接收消息，消费者开始处理
     * @return
     * 这里的逻辑是什么？
     * 在进行短链码创建和流量包扣减后为了防止短链码创建失败而进行回滚设计了这么一套逻辑
     * 流量包扣减后创建traffic_task任务并且发送消息到延迟队列，经过ttl后消息转发到死信队列，死信队列消费者收到消费信息后，去查询短链码是否已经创建
     * 如果短链码已经创建则直接删除对应的traffic_task任务，如果短链码没有创建成功则回滚流量包扣减
     */
    @Bean
    public Queue trafficReleaseDelayQueue(){
        Map<String,Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange",trafficEventExchange);
        args.put("x-dead-letter-routing-key",trafficReleaseRoutingKey);
        args.put("x-message-ttl",ttl);
        return new Queue(trafficReleaseDelayQueue,true,false,false,args);
    }


    /**
     * 死信队列
     */
    @Bean
    public Queue trafficReleaseQueue(){
        return new Queue(trafficReleaseQueue,true,false,false);
    }

    /**
     * 延迟队列的绑定关系
     * @return
     */
    @Bean
    public Binding trafficReleaseDelayBinding(){
        return new Binding(trafficReleaseDelayQueue,Binding.DestinationType.QUEUE, trafficEventExchange,trafficReleaseDelayRoutingKey,null);
    }

    /**
     * 死信队列绑定关系
     * @return
     */
    @Bean
    public Binding trafficReleaseBinding(){
        return new Binding(trafficReleaseQueue,Binding.DestinationType.QUEUE, trafficEventExchange,trafficReleaseRoutingKey,null);
    }

}
