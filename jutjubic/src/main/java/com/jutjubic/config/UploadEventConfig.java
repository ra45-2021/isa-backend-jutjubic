package com.jutjubic.config;

import lombok.Data;
import org.springframework.amqp.core.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "upload-event")
public class UploadEventConfig {

    private String exchange = "upload.events.exchange";

    private QueueConfig json = new QueueConfig("upload.events.json.queue", "upload.json");

    private QueueConfig protobuf = new QueueConfig("upload.events.protobuf.queue", "upload.protobuf");

    @Data
    public static class QueueConfig {
        private String queue;
        private String routingKey;

        public QueueConfig() {}

        public QueueConfig(String queue, String routingKey) {
            this.queue = queue;
            this.routingKey = routingKey;
        }
    }

    // EXCHANGE

    @Bean
    public DirectExchange uploadEventExchange() {
        return new DirectExchange(exchange, true, false);
    }

    // QUEUES

    @Bean
    public Queue uploadEventJsonQueue() {
        return QueueBuilder.durable(json.getQueue()).build();
    }

    @Bean
    public Queue uploadEventProtobufQueue() {
        return QueueBuilder.durable(protobuf.getQueue()).build();
    }

    // BINDINGS

    @Bean
    public Binding uploadEventJsonBinding(Queue uploadEventJsonQueue, DirectExchange uploadEventExchange) {
        return BindingBuilder
                .bind(uploadEventJsonQueue)
                .to(uploadEventExchange)
                .with(json.getRoutingKey());
    }

    @Bean
    public Binding uploadEventProtobufBinding(Queue uploadEventProtobufQueue, DirectExchange uploadEventExchange) {
        return BindingBuilder
                .bind(uploadEventProtobufQueue)
                .to(uploadEventExchange)
                .with(protobuf.getRoutingKey());
    }
}
