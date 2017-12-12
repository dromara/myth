package com.github.myth.demo.dubbo.inventory.mq;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
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