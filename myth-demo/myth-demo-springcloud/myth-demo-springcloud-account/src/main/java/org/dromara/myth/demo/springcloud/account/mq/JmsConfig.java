
package org.dromara.myth.demo.springcloud.account.mq;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;

import javax.jms.ConnectionFactory;


/**
 * The type Jms config.
 *
 * @author xiaoyu
 */
@Configuration
@EnableJms
public class JmsConfig {

    /**
     * Jms listener container queue jms listener container factory.
     *
     * @param activeMQConnectionFactory the active mq connection factory
     * @return the jms listener container factory
     */
    @Bean(name = "queueListenerContainerFactory")
    @ConditionalOnProperty(prefix = "spring.activemq", name = "broker-url")
    public JmsListenerContainerFactory<?> jmsListenerContainerQueue(ConnectionFactory activeMQConnectionFactory) {
        DefaultJmsListenerContainerFactory bean = new DefaultJmsListenerContainerFactory();
        bean.setConnectionFactory(activeMQConnectionFactory);
        bean.setPubSubDomain(Boolean.FALSE);
        return bean;
    }


}
