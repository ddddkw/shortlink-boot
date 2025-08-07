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

    /**
     * 交换机
     */
    private String orderEventExchange = "order.event.exchange";

    /**
     * 延迟队列名称
     */
    private String orderCloseDelayQueue = "order.close.delay.queue";

    /**
     * 关单队列名称，延迟队列的消息过期后转发的队列，用于被消费者监听
     */
    private String orderCloseQueue = "order.close.queue";

    /**
     * 进入到延迟队列的routingkey
     */
    private String orderCloseDelayRoutingKey = "order.close.delay.routing.key";

    /**
     * 进入到死信队列的routingkey
     */
    private String orderCloseRoutingKey = "order.close.routing.key";

    /**
     * 过期时间，毫秒为单位，临时改为1分钟过期
     */
    private Integer ttl = 1000 * 60;

    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Exchange orderEventExchange(){
        return new TopicExchange(orderEventExchange,true,false);
    }

    /**
     * 延迟队列
     * 经过ttl时间后会自动进入到死信队列
     */
    @Bean
    public Queue orderCloseDelayQueue(){
        Map<String,Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange",orderEventExchange);
        args.put("x-dead-letter-routing-key",orderCloseRoutingKey);
        args.put("x-message-ttl",ttl);
        return new Queue(orderCloseDelayQueue,true,false,false,args);

    }

    /**
     * 死信队列
     */
    @Bean
    public Queue orderCloseQueue(){

        return new Queue(orderCloseQueue,true,false,false);

    }

    /**
     * 将交换机和队列和key进行绑定
     * @return
     */
    @Bean
    public Binding orderCloseDelayBinding(){
        return new Binding(orderCloseDelayQueue,Binding.DestinationType.QUEUE, orderEventExchange,orderCloseDelayRoutingKey,null);
    }


    /**
     * 将交换机和队列和key进行绑定
     * @return
     */
    @Bean
    public Binding orderCloseBinding(){
        return new Binding(orderCloseQueue,Binding.DestinationType.QUEUE, orderEventExchange,orderCloseRoutingKey,null);
    }

    //=======================订单支付成功配置================================
    /**
     * 更新订单队列名称
     */
    private String orderUpdateQueue = "order.update.queue";

    /**
     * 订单发放流量包队列名称
     */
    private String orderTrafficQueue = "order.traffic.queue";

    /**
     * 进入到更新订单队列的routingkey
     */
    private String orderUpdateRoutingKey = "order.update.routing.key";

    /**
     * 进入到发放流量包队列的routingkey
     */
    private String orderTrafficRoutingKey = "order.traffic.routing.key";

    /**
     * 微信回调发送通知的routingkey
     */
    private String orderUpdateTrafficRoutingKey = "order.update.traffic.routing.key";

    /**
     * 用于绑定更新订单队列和交换机的key
     */
    private String orderUpdateBindingKey = "order.update.*.routing.key";

    /**
     * 用于绑定流量包发放订单队列和交换机的key
     */
    private String orderTrafficBindingKey = "order.*.traffic.routing.key";

    /**
     * 将交换机和订单更新队列和key进行绑定
     * @return
     */
    @Bean
    public Binding orderUpdateBinding(){
        return new Binding(orderUpdateQueue,Binding.DestinationType.QUEUE, orderEventExchange,orderUpdateRoutingKey,null);
    }

    /**
     * 将交换机和发放流量包队列和key进行绑定
     * @return
     */
    @Bean
    public Binding orderTrafficBinding(){
        return new Binding(orderTrafficQueue,Binding.DestinationType.QUEUE, orderEventExchange,orderTrafficRoutingKey,null);
    }

    /**
     * 更新订单队列
     */
    @Bean
    public Queue orderUpdateQueue(){

        return new Queue(orderUpdateQueue,true,false,false);

    }

    /**
     * 发放流量包队列
     */
    @Bean
    public Queue orderTrafficQueue(){

        return new Queue(orderTrafficQueue,true,false,false);

    }
}
