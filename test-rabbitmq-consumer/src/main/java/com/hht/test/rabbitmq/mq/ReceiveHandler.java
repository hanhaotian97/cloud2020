package com.hht.test.rabbitmq.mq;

import com.hht.test.rabbitmq.config.RabbitmqConfig;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

/**
 * <br/>Author hanhaotian
 * <br/>Description : 消费者接受消息
 * <br/>CreateTime 2020/7/13
 */
@Component
public class ReceiveHandler {

    //@RabbitListener(queues = "#{queue.name}")
    //@RabbitListener(queues = {RabbitmqConfig.QUEUE_INFORM_EMAIL})
    @RabbitListener(queues = "#{rabbitmqConfig.QUEUE_INFORM_EMAIL}")
    public void send_mail(String msg, Message message, Channel channel) throws UnsupportedEncodingException {
        System.out.println("receive msg is:" + msg);
        System.out.println("receive message is:" + new String(message.getBody(), "utf-8"));
    }
}
