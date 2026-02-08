package com.jutjubic.mqbenchmark.service;

import com.jutjubic.mqbenchmark.benchmark.BenchmarkCollector;
import com.jutjubic.mqbenchmark.dto.UploadEvent;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

// Consumer koji slusa RabbitMQ queue-ove i prima UploadEvent poruke
@Slf4j
@Service
public class UploadEventConsumer {

    private final JsonSerializationService jsonService;
    private final ProtobufSerializationService protobufService;
    private final BenchmarkCollector benchmarkCollector;

    public UploadEventConsumer(
            JsonSerializationService jsonService,
            ProtobufSerializationService protobufService,
            BenchmarkCollector benchmarkCollector
    ) {
        this.jsonService = jsonService;
        this.protobufService = protobufService;
        this.benchmarkCollector = benchmarkCollector;
    }

    @RabbitListener(queues = "${upload-event.json.queue}")
    public void handleJsonMessage(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
            byte[] body = message.getBody();

            long startTime = System.nanoTime();
            UploadEvent event = jsonService.deserialize(body);
            long endTime = System.nanoTime();

            long deserializationTimeNanos = endTime - startTime;
            benchmarkCollector.recordJsonReceived(body.length, deserializationTimeNanos);

            log.info("[JSON] Primljena poruka: videoId={}, title='{}', autor={}, replika={}, veličina={}B, deserijalizacija={}μs",
                    event.getVideoId(),
                    event.getTitle(),
                    event.getAuthor(),
                    event.getReplicaId(),
                    body.length,
                    deserializationTimeNanos / 1000);

            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("[JSON] Greška pri obradi poruke: {}", e.getMessage(), e);
            channel.basicNack(deliveryTag, false, false);
        }
    }

    @RabbitListener(queues = "${upload-event.protobuf.queue}")
    public void handleProtobufMessage(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
            byte[] body = message.getBody();

            long startTime = System.nanoTime();
            UploadEvent event = protobufService.deserialize(body);
            long endTime = System.nanoTime();

            long deserializationTimeNanos = endTime - startTime;
            benchmarkCollector.recordProtobufReceived(body.length, deserializationTimeNanos);

            log.info("[PROTOBUF] Primljena poruka: videoId={}, title='{}', autor={}, replika={}, veličina={}B, deserijalizacija={}μs",
                    event.getVideoId(),
                    event.getTitle(),
                    event.getAuthor(),
                    event.getReplicaId(),
                    body.length,
                    deserializationTimeNanos / 1000);

            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("[PROTOBUF] Greška pri obradi poruke: {}", e.getMessage(), e);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
