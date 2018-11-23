package org.dromara.myth.demo.motan.inventory.mq;

import org.dromara.myth.core.service.MythMqReceiveService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * The type Rabbit consumer.
 *
 * @author xiaoyu
 */
@Component
@ConditionalOnProperty(prefix = "spring.rabbitmq", name = "host")
@RabbitListener(queues = "inventory", containerFactory = "myContainerFactory")
public class RabbitConsumer {

    private final MythMqReceiveService mythMqReceiveService;

    /**
     * Instantiates a new Rabbit consumer.
     *
     * @param mythMqReceiveService the myth mq receive service
     */
    @Autowired
    public RabbitConsumer(MythMqReceiveService mythMqReceiveService) {
        this.mythMqReceiveService = mythMqReceiveService;
    }


    /**
     * Process.
     *
     * @param msg the msg
     */
    @RabbitHandler
    public void process(byte[] msg) {
        mythMqReceiveService.processMessage(msg);
    }


}
