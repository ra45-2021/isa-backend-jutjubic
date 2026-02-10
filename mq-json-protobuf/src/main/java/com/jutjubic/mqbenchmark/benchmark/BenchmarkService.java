package com.jutjubic.mqbenchmark.benchmark;

import com.jutjubic.mqbenchmark.config.UploadEventProperties;
import com.jutjubic.mqbenchmark.dto.UploadEvent;
import com.jutjubic.mqbenchmark.service.JsonSerializationService;
import com.jutjubic.mqbenchmark.service.ProtobufSerializationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

// Servis za izvrsavanje benchmark testova (lokalno ili kroz RabbitMQ)
@Slf4j
@Service
public class BenchmarkService {

    private final JsonSerializationService jsonService;
    private final ProtobufSerializationService protobufService;
    private final BenchmarkCollector collector;
    private final RabbitTemplate rabbitTemplate;
    private final UploadEventProperties properties;

    private static final List<String> SAMPLE_TITLES = List.of(
            "Kako napraviti savršenu pizzu",
            "React Tutorial za početnike",
            "Vlog iz Beograda",
            "Gaming highlights - Fortnite",
            "Workout rutina za početnike",
            "Recenzija novog iPhona",
            "Putovanje kroz Srbiju",
            "Programiranje u Javi",
            "DIY projekti za dom",
            "Muzički cover - popularna pesma"
    );

    private static final List<String> SAMPLE_AUTHORS = List.of(
            "marko_tech", "ana_vlogger", "petar_gamer", "jovana_fit",
            "stefan_code", "milica_diy", "nikola_music", "tamara_food"
    );

    private static final List<String> SAMPLE_TAGS = List.of(
            "tutorial", "vlog", "gaming", "fitness", "tech", "music",
            "food", "travel", "programming", "diy", "review", "serbia"
    );

    public BenchmarkService(
            JsonSerializationService jsonService,
            ProtobufSerializationService protobufService,
            BenchmarkCollector collector,
            RabbitTemplate rabbitTemplate,
            UploadEventProperties properties
    ) {
        this.jsonService = jsonService;
        this.protobufService = protobufService;
        this.collector = collector;
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
    }

    private UploadEvent generateRandomEvent(int index) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        List<String> tags = new ArrayList<>();
        int tagCount = random.nextInt(2, 5);
        for (int i = 0; i < tagCount; i++) {
            tags.add(SAMPLE_TAGS.get(random.nextInt(SAMPLE_TAGS.size())));
        }

        return UploadEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .videoId((long) (index + 1000))
                .title(SAMPLE_TITLES.get(random.nextInt(SAMPLE_TITLES.size())) + " #" + index)
                .author(SAMPLE_AUTHORS.get(random.nextInt(SAMPLE_AUTHORS.size())))
                .fileSizeBytes(random.nextLong(1_000_000, 500_000_000))
                .durationSeconds(random.nextInt(30, 3600))
                .uploadTimestamp(System.currentTimeMillis())
                .tags(tags)
                .description("Ovo je opis videa broj " + index + ". " +
                        "Lorem ipsum dolor sit amet, consectetur adipiscing elit.")
                .replicaId("benchmark_test")
                .build();
    }

    // Lokalni benchmark - bez RabbitMQ, meri ciste performanse serijalizacije
    public BenchmarkResult runLocalBenchmark(int messageCount) {
        log.info("Pokrećem lokalni benchmark sa {} poruka...", messageCount);
        collector.reset();

        List<UploadEvent> events = new ArrayList<>();
        for (int i = 0; i < messageCount; i++) {
            events.add(generateRandomEvent(i));
        }

        // JSON benchmark
        log.info("Testiram JSON serijalizaciju...");
        List<byte[]> jsonMessages = new ArrayList<>();

        for (UploadEvent event : events) {
            long serStart = System.nanoTime();
            byte[] jsonData = jsonService.serialize(event);
            long serEnd = System.nanoTime();

            jsonMessages.add(jsonData);
            collector.recordJsonSerialization(jsonData.length, serEnd - serStart);
        }

        log.info("Testiram JSON deserijalizaciju...");
        for (byte[] jsonData : jsonMessages) {
            long deserStart = System.nanoTime();
            jsonService.deserialize(jsonData);
            long deserEnd = System.nanoTime();

            collector.recordJsonReceived(jsonData.length, deserEnd - deserStart);
        }

        // Protobuf benchmark
        log.info("Testiram Protobuf serijalizaciju...");
        List<byte[]> protoMessages = new ArrayList<>();

        for (UploadEvent event : events) {
            long serStart = System.nanoTime();
            byte[] protoData = protobufService.serialize(event);
            long serEnd = System.nanoTime();

            protoMessages.add(protoData);
            collector.recordProtobufSerialization(protoData.length, serEnd - serStart);
        }

        log.info("Testiram Protobuf deserijalizaciju...");
        for (byte[] protoData : protoMessages) {
            long deserStart = System.nanoTime();
            protobufService.deserialize(protoData);
            long deserEnd = System.nanoTime();

            collector.recordProtobufReceived(protoData.length, deserEnd - deserStart);
        }

        log.info("Lokalni benchmark završen!");
        return collector.generateResult();
    }

    // RabbitMQ benchmark - salje poruke kroz MQ i meri end-to-end vreme
    public BenchmarkResult runRabbitMqBenchmark(int messageCount) {
        log.info("Pokrećem RabbitMQ benchmark sa {} poruka...", messageCount);
        collector.reset();

        for (int i = 0; i < messageCount; i++) {
            UploadEvent event = generateRandomEvent(i);

            // JSON
            long jsonSerStart = System.nanoTime();
            byte[] jsonData = jsonService.serialize(event);
            long jsonSerEnd = System.nanoTime();

            collector.recordJsonSerialization(jsonData.length, jsonSerEnd - jsonSerStart);

            rabbitTemplate.convertAndSend(
                    properties.getExchange(),
                    properties.getJson().getRoutingKey(),
                    jsonData
            );

            // Protobuf
            long protoSerStart = System.nanoTime();
            byte[] protoData = protobufService.serialize(event);
            long protoSerEnd = System.nanoTime();

            collector.recordProtobufSerialization(protoData.length, protoSerEnd - protoSerStart);

            rabbitTemplate.convertAndSend(
                    properties.getExchange(),
                    properties.getProtobuf().getRoutingKey(),
                    protoData
            );

            if ((i + 1) % 10 == 0) {
                log.info("Poslato {}/{} poruka", i + 1, messageCount);
            }
        }

        log.info("Sve poruke poslate! Čekam da consumer primi poruke...");

        // Cekaj da consumer primi sve poruke (max 10s)
        int maxWaitSeconds = 10;
        for (int i = 0; i < maxWaitSeconds; i++) {
            if (collector.getJsonMessageCount() >= messageCount &&
                    collector.getProtobufMessageCount() >= messageCount) {
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        log.info("RabbitMQ benchmark završen! Primljeno: JSON={}, Protobuf={}",
                collector.getJsonMessageCount(), collector.getProtobufMessageCount());

        return collector.generateResult();
    }

    public BenchmarkResult getCurrentResults() {
        return collector.generateResult();
    }

    public void reset() {
        collector.reset();
    }
}
