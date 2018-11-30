package org.dromara.myth.demo.dubbo.inventory.mq;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.dromara.myth.common.config.MythConfig;
import org.dromara.myth.core.service.MythMqReceiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.List;

/**
 * RocketmqConsumer.
 *
 * @author xiaoyu(Myth)
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.rocketmq", name = "namesrvAddr")
public class RocketmqConsumer {

    private static final String TAGS = "inventory";

    private final Environment env;

    private final MythMqReceiveService mythMqReceiveService;

    private final MythConfig mythConfig;

    @Autowired
    public RocketmqConsumer(Environment env, MythMqReceiveService mythMqReceiveService, MythConfig mythConfig) {
        this.env = env;
        this.mythMqReceiveService = mythMqReceiveService;
        this.mythConfig = mythConfig;
    }

    @Bean
    public DefaultMQPushConsumer pushConsumer() throws MQClientException {
        /**
         * 一个应用创建一个Consumer，由应用来维护此对象，可以设置为全局对象或者单例<br>
         * 注意：ConsumerGroupName需要由应用来保证唯一
         */
        DefaultMQPushConsumer consumer =
                new DefaultMQPushConsumer(env.getProperty("spring.rocketmq.consumerGroupName"));
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        consumer.setNamesrvAddr(env.getProperty("spring.rocketmq.namesrvAddr"));
        consumer.setConsumeMessageBatchMaxSize(2);
        //RECONSUME_LATER的重试次数，RocketMQ默认是16次
        consumer.setMaxReconsumeTimes(mythConfig.getRetryMax());
        /**
         * 订阅指定topic下tags
         */
        String topic = env.getProperty("spring.rocketmq.topic");
        consumer.subscribe(topic, TAGS);

        consumer.registerMessageListener((List<MessageExt> msgList,
                                          ConsumeConcurrentlyContext context) -> {

            MessageExt msg = msgList.get(0);
            try {
                // 默认msgList里只有一条消息，可以通过设置consumeMessageBatchMaxSize参数来批量接收消息
                final byte[] message = msg.getBody();
                final Boolean aBoolean = mythMqReceiveService.processMessage(message);

            } catch (Exception e) {
                e.printStackTrace();
                //重复消费
                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }

            //如果没有return success，consumer会重复消费此信息，直到success。
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });

        /**
         * Consumer对象在使用之前必须要调用start初始化，初始化一次即可<br>
         */
        consumer.start();

        return consumer;
    }
}
