package com.jutjubic.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jutjubic.config.UploadEventConfig;
import com.jutjubic.domain.Post;
import com.jutjubic.dto.UploadEventDto;
import com.jutjubic.proto.UploadEventProto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Servis za slanje UploadEvent poruka u RabbitMQ.
 *
 * Kada korisnik upload-uje novi video, ovaj servis:
 * 1. Kreira UploadEventDto sa informacijama o videu
 * 2. Serijalizuje u JSON format i šalje na JSON queue
 * 3. Serijalizuje u Protobuf format i šalje na Protobuf queue
 *
 * Ovo omogućava benchmark aplikaciji da primi iste podatke
 * u oba formata i uporedi performanse.
 */
@Slf4j
@Service
public class UploadEventProducer {

    private final RabbitTemplate rabbitTemplate;
    private final UploadEventConfig config;
    private final ObjectMapper objectMapper;

    @Value("${REPLICA_ID:unknown}")
    private String replicaId;

    public UploadEventProducer(RabbitTemplate rabbitTemplate, UploadEventConfig config) {
        this.rabbitTemplate = rabbitTemplate;
        this.config = config;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Šalje UploadEvent poruku za novi video.
     *
     * Poziva se iz PostService nakon uspešnog upload-a videa.
     *
     * @param post Post entitet (novi video)
     * @param fileSizeBytes Veličina fajla u bajtovima
     */
    public void sendUploadEvent(Post post, long fileSizeBytes) {
        try {
            // Kreiraj UploadEventDto
            UploadEventDto event = createUploadEvent(post, fileSizeBytes);

            // Šalji u JSON formatu
            sendJsonMessage(event);

            // Šalji u Protobuf formatu
            sendProtobufMessage(event);

            log.info("[UploadEvent] Poruka poslata za video: id={}, title='{}', replika={}",
                    post.getId(), post.getTitle(), replicaId);

        } catch (Exception e) {
            // Ne prekidaj upload ako slanje poruke ne uspe
            log.error("[UploadEvent] Greška pri slanju poruke: {}", e.getMessage(), e);
        }
    }

    /**
     * Kreira UploadEventDto iz Post entiteta.
     */
    private UploadEventDto createUploadEvent(Post post, long fileSizeBytes) {
        // Parsiraj tagove ako postoje
        List<String> tags = null;
        if (post.getTags() != null && !post.getTags().isEmpty()) {
            tags = Arrays.asList(post.getTags().split(","));
        }

        return UploadEventDto.builder()
                .eventId(UUID.randomUUID().toString())
                .videoId(post.getId())
                .title(post.getTitle())
                .author(post.getAuthor().getUsername())
                .fileSizeBytes(fileSizeBytes)
                .durationSeconds(null) // Trajanje nije poznato pri upload-u
                .uploadTimestamp(post.getCreatedAt().toEpochMilli())
                .tags(tags)
                .description(post.getDescription())
                .replicaId(replicaId)
                .build();
    }

    /**
     * Serijalizuje i šalje poruku u JSON formatu.
     * Koristi Message objekat direktno da izbegne transformaciju od MessageConverter-a.
     */
    private void sendJsonMessage(UploadEventDto event) {
        try {
            byte[] jsonData = objectMapper.writeValueAsBytes(event);

            // Kreiraj Message sa content-type application/json
            MessageProperties props = new MessageProperties();
            props.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            Message message = new Message(jsonData, props);

            rabbitTemplate.send(
                    config.getExchange(),
                    config.getJson().getRoutingKey(),
                    message
            );

            log.debug("[UploadEvent] JSON poruka poslata, veličina: {} bytes", jsonData.length);

        } catch (Exception e) {
            log.error("[UploadEvent] Greška pri slanju JSON poruke: {}", e.getMessage(), e);
        }
    }

    /**
     * Serijalizuje i šalje poruku u Protobuf formatu.
     * Koristi Message objekat direktno da izbegne transformaciju od MessageConverter-a.
     */
    private void sendProtobufMessage(UploadEventDto event) {
        try {
            // Konvertuj u Protobuf objekat
            UploadEventProto.UploadEvent.Builder builder = UploadEventProto.UploadEvent.newBuilder();

            if (event.getEventId() != null) builder.setEventId(event.getEventId());
            if (event.getVideoId() != null) builder.setVideoId(event.getVideoId());
            if (event.getTitle() != null) builder.setTitle(event.getTitle());
            if (event.getAuthor() != null) builder.setAuthor(event.getAuthor());
            if (event.getFileSizeBytes() != null) builder.setFileSizeBytes(event.getFileSizeBytes());
            if (event.getDurationSeconds() != null) builder.setDurationSeconds(event.getDurationSeconds());
            if (event.getUploadTimestamp() != null) builder.setUploadTimestamp(event.getUploadTimestamp());
            if (event.getTags() != null) builder.addAllTags(event.getTags());
            if (event.getDescription() != null) builder.setDescription(event.getDescription());
            if (event.getReplicaId() != null) builder.setReplicaId(event.getReplicaId());

            byte[] protoData = builder.build().toByteArray();

            // Kreiraj Message sa content-type application/x-protobuf
            MessageProperties props = new MessageProperties();
            props.setContentType("application/x-protobuf");
            Message message = new Message(protoData, props);

            rabbitTemplate.send(
                    config.getExchange(),
                    config.getProtobuf().getRoutingKey(),
                    message
            );

            log.debug("[UploadEvent] Protobuf poruka poslata, veličina: {} bytes", protoData.length);

        } catch (Exception e) {
            log.error("[UploadEvent] Greška pri slanju Protobuf poruke: {}", e.getMessage(), e);
        }
    }
}
