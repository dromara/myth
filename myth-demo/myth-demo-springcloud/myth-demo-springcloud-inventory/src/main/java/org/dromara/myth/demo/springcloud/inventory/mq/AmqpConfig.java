package org.dromara.myth.demo.springcloud.inventory.mq;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The type Amqp config.
 * @author xiaoyu
 */
@Configuration
public class AmqpConfig {

    /**
     * My container factory simple rabbit listener container factory.
     *
     * @param configurer        the configurer
     * @param connectionFactory the connection factory
     * @return the simple rabbit listener container factory
     */
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

    /**
     * Inventory queue queue.
     *
     * @return the queue
     */
    @Bean
    public Queue inventoryQueue() {
        return new Queue("inventory");
    }


}