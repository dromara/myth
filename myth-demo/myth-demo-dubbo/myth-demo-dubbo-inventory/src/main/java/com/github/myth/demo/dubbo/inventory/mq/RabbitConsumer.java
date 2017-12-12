package com.github.myth.demo.dubbo.inventory.mq;

import com.github.myth.core.service.MythMqReceiveService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * <p>Description: .</p>
 *
 * @author xiaoyu(Myth)
 * @version 1.0
 * @date 2017/12/7 17:19
 * @since JDK 1.8
 */
@Component
@ConditionalOnProperty(prefix = "spring.rabbitmq", name = "host")
@RabbitListener(queues = "inventory",containerFactory = "myContainerFactory")
public class RabbitConsumer {

    private final MythMqReceiveService mythMqReceiveService;

    @Autowired
    public RabbitConsumer(MythMqReceiveService mythMqReceiveService) {
        this.mythMqReceiveService = mythMqReceiveService;
    }


    @RabbitHandler
    public void process(byte[] msg) {
        mythMqReceiveService.processMessage(msg);
    }


}
