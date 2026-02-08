package com.jutjubic.mqbenchmark.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// RabbitMQ konfiguracija - exchange, queue-ovi i binding-i
@Configuration
public class RabbitMqConfig {

    private final UploadEventProperties properties;

    public RabbitMqConfig(UploadEventProperties properties) {
        this.properties = properties;
    }

    @Bean
    public DirectExchange uploadEventExchange() {
        return new DirectExchange(properties.getExchange(), true, false);
    }

    @Bean
    public Queue jsonQueue() {
        return QueueBuilder.durable(properties.getJson().getQueue()).build();
    }

    @Bean
    public Queue protobufQueue() {
        return QueueBuilder.durable(properties.getProtobuf().getQueue()).build();
    }

    @Bean
    public Binding jsonBinding(Queue jsonQueue, DirectExchange uploadEventExchange) {
        return BindingBuilder
                .bind(jsonQueue)
                .to(uploadEventExchange)
                .with(properties.getJson().getRoutingKey());
    }

    @Bean
    public Binding protobufBinding(Queue protobufQueue, DirectExchange uploadEventExchange) {
        return BindingBuilder
                .bind(protobufQueue)
                .to(uploadEventExchange)
                .with(properties.getProtobuf().getRoutingKey());
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
