package org.example.listener;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.example.model.EventMessage;
import org.example.service.TrafficService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 由于流量包是与账号绑定的，所以需要在账号服务这边监听消息进行流量包发放操作
 */
@Component
@RabbitListener(queuesToDeclare = {
        @Queue("order.traffic.queue")
})
@Slf4j
public class TrafficMQListener {

    @Autowired
    private TrafficService trafficService;

    @RabbitHandler
    public void trafficHandler(EventMessage eventMessage, Message message, Channel channel) {
        log.info("监听到消息trafficHandler:{}",eventMessage);
        try {
            trafficService.handlerTrafficMessage(eventMessage);
        } catch (Exception e){
            log.error("消息消费失败：{}", eventMessage);
        }
    }


}
