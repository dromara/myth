package org.dromara.myth.demo.springcloud.account.mq;

import org.dromara.myth.core.service.MythMqReceiveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * The type Activemq consumer.
 * @author xiaoyu
 */
@Component
@ConditionalOnProperty(prefix = "spring.activemq", name = "broker-url")
public class ActivemqConsumer {

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ActivemqConsumer.class);

    private final MythMqReceiveService mythMqReceiveService;

    /**
     * Instantiates a new Activemq consumer.
     *
     * @param mythMqReceiveService the myth mq receive service
     */
    @Autowired
    public ActivemqConsumer(MythMqReceiveService mythMqReceiveService) {
        this.mythMqReceiveService = mythMqReceiveService;
    }


    /**
     * Receive queue.
     *
     * @param message the message
     */
    @JmsListener(destination = "account",containerFactory = "queueListenerContainerFactory")
    public void receiveQueue(byte[] message) {
        LOGGER.info("=========扣减账户信息接收到Myth框架传入的信息==========");
        final Boolean success = mythMqReceiveService.processMessage(message);
        if(success){
            //消费成功，消息出队列，否则不消费
        }
    }
}
