package com.hht.test.rabbitmq.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * <br/>Author hanhaotian
 * <br/>Description : rabbitmq配置文件
 * <br/>CreateTime 2020/7/13
 */
@Configuration
public class RabbitmqConfig {
    //队列名
    public static String QUEUE_INFORM_EMAIL = "queue_inform_email";
    public static String QUEUE_INFORM_SMS = "queue_inform_sms";
    //交换机名称
    public static String EXCHANGE_FANOUT_INFORM = "exchange_fanout_inform";
    public static String EXCHANGE_DIRECT_INFORM = "exchange_direct_inform";
    public static String EXCHANGE_TOPICS_INFORM = "exchange_topics_inform";
    //routingkey规则
    public static String ROUTINGKEY_EMAIL = "inform.#.email.#";
    public static String ROUTINGKEY_SMS = "inform.#.sms.#";


    //声明队列
    @Bean
    public Queue queueEmail() {
        return new Queue(QUEUE_INFORM_EMAIL);
    }

    @Bean
    public Queue queueSms() {
        return new Queue(QUEUE_INFORM_SMS);
    }

    //声明fanout交换机
    @Bean
    Exchange fanoutExchange() {
        return ExchangeBuilder.fanoutExchange(EXCHANGE_FANOUT_INFORM)
                .durable(true)
                .build();
    }

    //声明Topic交换机
    @Bean
    Exchange topicExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE_TOPICS_INFORM)
                .durable(true)
                .build();
    }

    //将队列与Topic交换机进行绑定，并指定路由键
    @Bean
    Binding topicBindingEmail(@Qualifier("queueEmail") Queue queue, @Qualifier("topicExchange")Exchange exchange) {
        return BindingBuilder.bind(queue)
                .to(exchange)
                .with(ROUTINGKEY_EMAIL)
                .noargs();
    }
    @Bean
    Binding topicBindingSms(@Qualifier("queueSms") Queue queue, @Qualifier("topicExchange")Exchange exchange) {
        return BindingBuilder.bind(queue)
                .to(exchange)
                .with(ROUTINGKEY_SMS)
                .noargs();
    }
}
