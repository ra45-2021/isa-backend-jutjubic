package com.jutjubic.mqbenchmark.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Konfiguracija za UploadEvent RabbitMQ queue-ove.
 *
 * Čita vrednosti iz application.yml:
 *
 * upload-event:
 *   exchange: upload.events.exchange
 *   json:
 *     queue: upload.events.json.queue
 *     routing-key: upload.json
 *   protobuf:
 *     queue: upload.events.protobuf.queue
 *     routing-key: upload.protobuf
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "upload-event")
public class UploadEventProperties {

    /**
     * Ime RabbitMQ exchange-a.
     * Exchange prima poruke i rutira ih na queue-ove prema routing key-u.
     */
    private String exchange;

    /**
     * Konfiguracija za JSON queue.
     */
    private QueueConfig json = new QueueConfig();

    /**
     * Konfiguracija za Protobuf queue.
     */
    private QueueConfig protobuf = new QueueConfig();

    @Data
    public static class QueueConfig {
        /**
         * Ime queue-a u RabbitMQ.
         */
        private String queue;

        /**
         * Routing key za rutiranje poruka na ovaj queue.
         */
        private String routingKey;
    }
}
