package org.example.listener;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.example.enums.BizCodeEnum;
import org.example.enums.EventMessageType;
import org.example.exception.BizException;
import org.example.model.EventMessage;
import org.example.service.LinkSeniorService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Description
 * RabbitListener 定义从哪个队列中获取消息，队列的消费者
 **/

@Component
@Slf4j
@RabbitListener(queues = "short_link.update.link.queue")
public class ShortLinkUpdateLinkMQListener {

    @Autowired
    private LinkSeniorService linkSeniorService;

    @RabbitHandler
    public void shortLinkHandler(EventMessage eventMessage, Message message, Channel channel) throws IOException {
        log.info("监听到消息ShortLinkUpdateLinkMQListener message消息内容:{}",message);
        long tag = message.getMessageProperties().getDeliveryTag();
        try{
            eventMessage.setEventMessageType(EventMessageType.SHORT_LINK_UPDATE_LINK.name());
            //TODO 处理业务逻辑

        }catch (Exception e){

            //处理业务异常，还有进行其他操作，比如记录失败原因
            log.error("消费失败:{}",eventMessage);
            throw new BizException(BizCodeEnum.MQ_CONSUME_EXCEPTION);
        }
        log.info("消费成功:{}",eventMessage);
        // 手动确认消息消费成功
        // channel.basicAck(tag,false);

    }



}
