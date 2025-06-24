package org.example.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class RabbitMQErrorConfig {

    /**
     * 错误的交换机
     */
    private String OrderErrorExchange = "order.error.exchange";
    /**
     * 异常队列
     */
    private String OrderErrorQueue = "order.error.queue";
    /**
     * 异常routingkey
     */
    private String OrderErrorRoutingKey = "order.error.routing.key";

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 创建异常交换机
     * @return
     */
    @Bean
    public TopicExchange ErrorTopicExchange(){
        return new TopicExchange(OrderErrorExchange,true,false);
    }

    /**
     * 创建异常队列
     * @return
     */
    @Bean
    public Queue errorQueue(){
        return new Queue(OrderErrorQueue,true);
    }

    /**
     * 对key，交换机和队列进行绑定
     * @return
     */
    @Bean
    public Binding bindingErrorExchange(){
        return BindingBuilder.bind(errorQueue()).to(ErrorTopicExchange()).with(OrderErrorRoutingKey);
    }

    @Bean
    public MessageRecoverer messageRecoverer(){
        return new RepublishMessageRecoverer(rabbitTemplate,OrderErrorExchange,OrderErrorRoutingKey);
    }
}
