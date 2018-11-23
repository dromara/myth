package org.dromara.myth.demo.dubbo.account.mq;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.Consumer;
import com.aliyun.openservices.ons.api.ONSFactory;
import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.client.exception.MQClientException;
import org.dromara.myth.core.service.MythMqReceiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Properties;

/**
 * <p>Description: .</p>
 *
 * @author xiaoyu(Myth)
 * @version 1.0
 * @date 2017/12/12 14:29
 * @since JDK 1.8
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.aliyunmq", name = "broker-url")
public class AliyunmqConsumer {


    private static final String TAG = "account";

    @Autowired
    private Environment env;

    @Autowired
    private MythMqReceiveService mythMqReceiveService;

    @Bean
    public Consumer pushConsumer() throws MQClientException {
        /**
         * 一个应用创建一个Consumer，由应用来维护此对象，可以设置为全局对象或者单例<br>
         * 注意：ConsumerGroupName需要由应用来保证唯一
         */
        Properties properties = new Properties();
        properties.setProperty(PropertyKeyConst.ConsumerId,env.getProperty("spring.aliyunmq.consumerId"));
        properties.setProperty(PropertyKeyConst.AccessKey,env.getProperty("spring.aliyunmq.accessKey"));
        properties.setProperty(PropertyKeyConst.SecretKey,env.getProperty("spring.aliyunmq.secretKey"));
        properties.setProperty(PropertyKeyConst.ONSAddr,env.getProperty("spring.aliyunmq.broker-url"));

        Consumer consumer = ONSFactory.createConsumer(properties);

        String topic = env.getProperty("spring.aliyunmq.topic");
        consumer.subscribe(topic, TAG, (message, consumeContext) -> {
            try {
                final byte[] body = message.getBody();
                mythMqReceiveService.processMessage(body);
                return Action.CommitMessage;
            }catch (Exception e) {
                //消费失败
                return Action.ReconsumeLater;
            }
        });

        /**
         * Consumer对象在使用之前必须要调用start初始化，初始化一次即可<br>
         */
        consumer.start();

        return consumer;
    }
}
