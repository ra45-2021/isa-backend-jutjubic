package com.jutjubic.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.naming.Binding;
import java.util.Queue;

@Configuration
public class RabbitConfig {

    @Bean
    public Queue transcodeQueue(TranscodingProps p) {
        return QueueBuilder.durable(p.getQueue()).build();
    }

    @Bean
    public DirectExchange transcodeExchange(TranscodingProps p) {
        return new DirectExchange(p.getExchange(), true, false);
    }

    @Bean
    public Binding transcodeBinding(Queue transcodeQueue, DirectExchange transcodeExchange, TranscodingProps p) {
        return BindingBuilder.bind(transcodeQueue).to(transcodeExchange).with(p.getRoutingKey());
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);

        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setPrefetchCount(1);
        return factory;
    }
}
