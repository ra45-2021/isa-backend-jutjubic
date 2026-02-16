package com.jutjubic.mqbenchmark.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

// Konfiguracija za RabbitMQ queue-ove (cita iz application.yml)
@Data
@Configuration
@ConfigurationProperties(prefix = "upload-event")
public class UploadEventProperties {

    private String exchange;
    private QueueConfig json = new QueueConfig();
    private QueueConfig protobuf = new QueueConfig();

    @Data
    public static class QueueConfig {
        private String queue;
        private String routingKey;
    }
}
