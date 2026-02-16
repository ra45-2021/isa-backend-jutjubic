package com.jutjubic.mqbenchmark.service;

import com.google.protobuf.InvalidProtocolBufferException;
import com.jutjubic.mqbenchmark.dto.UploadEvent;
import com.jutjubic.mqbenchmark.proto.UploadEventProto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

// Protobuf serijalizacija/deserijalizacija
@Service
public class ProtobufSerializationService {

    public byte[] serialize(UploadEvent event) {
        UploadEventProto.UploadEvent protoEvent = toProto(event);
        return protoEvent.toByteArray();
    }

    public UploadEvent deserialize(byte[] data) {
        try {
            UploadEventProto.UploadEvent protoEvent = UploadEventProto.UploadEvent.parseFrom(data);
            return fromProto(protoEvent);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("Greška pri Protobuf deserijalizaciji: " + e.getMessage(), e);
        }
    }

    // Konverzija POJO -> Protobuf
    private UploadEventProto.UploadEvent toProto(UploadEvent event) {
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

        return builder.build();
    }

    // Konverzija Protobuf -> POJO
    private UploadEvent fromProto(UploadEventProto.UploadEvent proto) {
        List<String> tags = new ArrayList<>(proto.getTagsList());

        return UploadEvent.builder()
                .eventId(proto.getEventId())
                .videoId(proto.getVideoId())
                .title(proto.getTitle())
                .author(proto.getAuthor())
                .fileSizeBytes(proto.getFileSizeBytes())
                .durationSeconds(proto.getDurationSeconds())
                .uploadTimestamp(proto.getUploadTimestamp())
                .tags(tags)
                .description(proto.getDescription())
                .replicaId(proto.getReplicaId())
                .build();
    }
}
