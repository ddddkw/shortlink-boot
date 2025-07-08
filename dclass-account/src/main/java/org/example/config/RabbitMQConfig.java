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

}
