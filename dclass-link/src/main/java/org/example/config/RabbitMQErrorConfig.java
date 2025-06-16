package org.example.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
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
    private String ShortLinkErrorExchange = "short_link.error.exchange";
    /**
     * 异常队列
     */
    private String ShortLinkErrorQueue = "short_link.error.queue";
    /**
     * 异常routingkey
     */
    private String ShortLinkErrorRoutingKey = "short_link.error.routing.key";

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 创建异常交换机
     * @return
     */
    @Bean
    public TopicExchange ErrorTopicExchange(){
        return new TopicExchange(ShortLinkErrorExchange,true,false);
    }

    /**
     * 创建异常队列
     * @return
     */
    @Bean
    public Queue errorQueue(){
        return new Queue(ShortLinkErrorQueue,true);
    }

    /**
     * 对key，交换机和队列进行绑定
     * @return
     */
    @Bean
    public Binding bindingErrorExchange(){
        return BindingBuilder.bind(errorQueue()).to(ErrorTopicExchange()).with(ShortLinkErrorRoutingKey);
    }

    @Bean
    public MessageRecoverer messageRecoverer(){
        return new RepublishMessageRecoverer(rabbitTemplate,ShortLinkErrorExchange,ShortLinkErrorRoutingKey);
    }
}
