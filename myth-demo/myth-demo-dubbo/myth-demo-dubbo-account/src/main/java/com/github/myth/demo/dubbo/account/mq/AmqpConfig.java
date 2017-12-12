package com.github.myth.demo.dubbo.account.mq;

import com.github.myth.core.service.MythMqReceiveService;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>Description: .</p>
 *
 * @author xiaoyu(Myth)
 * @version 1.0
 * @date 2017/12/7 16:38
 * @since JDK 1.8
 */
@Configuration
public class AmqpConfig {
    private static final String EXCHANGE = "account";
    private static final String ROUTING_KEY = "account";


    /**
     * 针对消费者配置
     * 1. 设置交换机类型
     * 2. 将队列绑定到交换机
     * <p>
     * <p>
     * FanoutExchange: 将消息分发到所有的绑定队列，无routingkey的概念
     * HeadersExchange ：通过添加属性key-value匹配
     * DirectExchange:按照routingkey分发到指定队列
     * TopicExchange:多关键字匹配
     */
    @Bean
    public DirectExchange defaultExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Queue queue() {
        //队列持久
        return new Queue("account", true);

    }

    @Bean
    public Binding binding() {
        return BindingBuilder.bind(queue()).to(defaultExchange()).with(AmqpConfig.ROUTING_KEY);
    }

    @Autowired
    private MythMqReceiveService mythMqReceiveService;


    @Autowired
    private  ConnectionFactory connectionFactory;

    @Bean
    @ConditionalOnProperty(prefix = "spring.rabbitmq", name = "host")
    public SimpleMessageListenerContainer messageContainer() {
        SimpleMessageListenerContainer container =
                new SimpleMessageListenerContainer(connectionFactory);
        container.setQueues(queue());
        container.setExposeListenerChannel(true);
        container.setMaxConcurrentConsumers(1);
        container.setConcurrentConsumers(1);
        //设置确认模式手工确认
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.setMessageListener((ChannelAwareMessageListener) (message, channel) -> {
            byte[] messageBody = message.getBody();
            //确认消息成功消费
            final Boolean success = mythMqReceiveService.processMessage(messageBody);
            if (success) {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            }
        });
        return container;
    }

}