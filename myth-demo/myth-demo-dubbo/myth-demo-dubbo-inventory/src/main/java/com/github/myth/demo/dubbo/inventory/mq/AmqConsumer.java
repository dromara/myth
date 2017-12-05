package com.github.myth.demo.dubbo.inventory.mq;

import com.github.myth.core.service.MythMqReceiveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * @author yu.xiao@happylifeplat.com
 * @version 1.0
 * @date 2017/8/23 17:58
 * @since JDK 1.8
 */
@Component
public class AmqConsumer {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AmqConsumer.class);


    private final MythMqReceiveService mythMqReceiveService;

    @Autowired
    public AmqConsumer(MythMqReceiveService mythMqReceiveService) {
        this.mythMqReceiveService = mythMqReceiveService;
    }


    @JmsListener(destination = "inventory",containerFactory = "queueListenerContainerFactory")
    public void receiveQueue(byte[] message) {
        LOGGER.info("=========扣减库存接收到Myth框架传入的信息==========");
        final Boolean success = mythMqReceiveService.processMessage(message);
        if(success){
            //消费成功，消息出队列，否则不消费
        }

    }
}
