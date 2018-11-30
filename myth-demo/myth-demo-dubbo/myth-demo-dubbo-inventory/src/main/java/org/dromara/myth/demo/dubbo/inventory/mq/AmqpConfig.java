package org.dromara.myth.demo.dubbo.inventory.mq;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AmqpConfig.
 * @author xiaoyu(Myth)
 */
@Configuration
public class AmqpConfig {

    @Bean
    @ConditionalOnProperty(prefix = "spring.rabbitmq", name = "host")
    public SimpleRabbitListenerContainerFactory myContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setPrefetchCount(100);
        configurer.configure(factory, connectionFactory);
        return factory;
    }

    @Bean
    public Queue inventoryQueue() {
        return new Queue("inventory");
    }


}