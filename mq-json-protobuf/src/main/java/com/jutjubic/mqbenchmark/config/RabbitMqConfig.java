package com.jutjubic.mqbenchmark.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ konfiguracija za novu benchmark aplikaciju.
 *
 * Definiše:
 * 1. Exchange - prima poruke od producer-a
 * 2. Queue-ove - JSON queue i Protobuf queue
 * 3. Binding-e - povezuje exchange sa queue-ovima preko routing key-eva
 *
 * Arhitektura:
 *
 *   jutjubic (replica1/replica2)
 *           │
 *           │ šalje poruku sa routing key-em
 *           ▼
 *   ┌───────────────────────────┐
 *   │   upload.events.exchange  │  (Direct Exchange)
 *   └───────────────────────────┘
 *           │
 *           ├─── routing-key: "upload.json" ────▶ upload.events.json.queue
 *           │
 *           └─── routing-key: "upload.protobuf" ─▶ upload.events.protobuf.queue
 *
 *   mq-benchmark aplikacija sluša oba queue-a
 */
@Configuration
public class RabbitMqConfig {

    private final UploadEventProperties properties;

    public RabbitMqConfig(UploadEventProperties properties) {
        this.properties = properties;
    }

    // ========================================
    // EXCHANGE
    // ========================================

    /**
     * Direct Exchange za upload event poruke.
     *
     * Direct exchange rutira poruke na queue-ove čiji routing key
     * tačno odgovara routing key-u poruke.
     */
    @Bean
    public DirectExchange uploadEventExchange() {
        return new DirectExchange(properties.getExchange(), true, false);
    }

    // ========================================
    // QUEUES
    // ========================================

    /**
     * Queue za JSON poruke.
     * Durable = true - queue preživljava restart RabbitMQ-a
     */
    @Bean
    public Queue jsonQueue() {
        return QueueBuilder.durable(properties.getJson().getQueue()).build();
    }

    /**
     * Queue za Protobuf poruke.
     */
    @Bean
    public Queue protobufQueue() {
        return QueueBuilder.durable(properties.getProtobuf().getQueue()).build();
    }

    // ========================================
    // BINDINGS
    // ========================================

    /**
     * Povezuje JSON queue sa exchange-om.
     * Poruke sa routing key-em "upload.json" idu na ovaj queue.
     */
    @Bean
    public Binding jsonBinding(Queue jsonQueue, DirectExchange uploadEventExchange) {
        return BindingBuilder
                .bind(jsonQueue)
                .to(uploadEventExchange)
                .with(properties.getJson().getRoutingKey());
    }

    /**
     * Povezuje Protobuf queue sa exchange-om.
     * Poruke sa routing key-em "upload.protobuf" idu na ovaj queue.
     */
    @Bean
    public Binding protobufBinding(Queue protobufQueue, DirectExchange uploadEventExchange) {
        return BindingBuilder
                .bind(protobufQueue)
                .to(uploadEventExchange)
                .with(properties.getProtobuf().getRoutingKey());
    }

    // ========================================
    // LISTENER FACTORY
    // ========================================

    /**
     * Factory za kreiranje RabbitMQ listener-a.
     * Podešava manual acknowledgement - poruke se potvrđuju nakon uspešne obrade.
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);

        // Manual ACK - consumer eksplicitno potvrđuje obradu poruke
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);

        // Prefetch = 1 - uzima jednu po jednu poruku (za fer distribuciju)
        factory.setPrefetchCount(1);

        return factory;
    }
}
