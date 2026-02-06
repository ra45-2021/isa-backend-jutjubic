package com.jutjubic.mqbenchmark.service;

import com.google.protobuf.InvalidProtocolBufferException;
import com.jutjubic.mqbenchmark.dto.UploadEvent;
import com.jutjubic.mqbenchmark.proto.UploadEventProto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Servis za Protobuf serijalizaciju i deserijalizaciju UploadEvent objekata.
 *
 * Protocol Buffers (Protobuf) je Google-ov binarni format za serijalizaciju.
 * Zahteva definiciju šeme (.proto fajl) koja se kompajlira u Java klase.
 *
 * Prednosti Protobuf-a:
 * - Manja veličina poruke (binarni format)
 * - Brža serijalizacija/deserijalizacija
 * - Stroga tipizacija (šema definiše tipove)
 * - Unapred/unazad kompatibilnost
 *
 * Mane Protobuf-a:
 * - Nije čitljiv (binarni format)
 * - Zahteva .proto definiciju i kompajliranje
 * - Kompleksnija integracija
 */
@Service
public class ProtobufSerializationService {

    /**
     * Serijalizuje UploadEvent objekat u Protobuf byte niz.
     *
     * Proces:
     * 1. Konvertuje UploadEvent (Java POJO) u UploadEventProto.UploadEvent (Protobuf klasa)
     * 2. Poziva toByteArray() na Protobuf objektu
     *
     * @param event UploadEvent objekat za serijalizaciju
     * @return byte[] Protobuf reprezentacija objekta
     */
    public byte[] serialize(UploadEvent event) {
        UploadEventProto.UploadEvent protoEvent = toProto(event);
        return protoEvent.toByteArray();
    }

    /**
     * Deserijalizuje Protobuf byte niz u UploadEvent objekat.
     *
     * Proces:
     * 1. Parsira byte[] u UploadEventProto.UploadEvent (Protobuf klasa)
     * 2. Konvertuje Protobuf objekat u UploadEvent (Java POJO)
     *
     * @param data byte[] Protobuf podaci
     * @return UploadEvent deserijalizovani objekat
     */
    public UploadEvent deserialize(byte[] data) {
        try {
            UploadEventProto.UploadEvent protoEvent = UploadEventProto.UploadEvent.parseFrom(data);
            return fromProto(protoEvent);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("Greška pri Protobuf deserijalizaciji: " + e.getMessage(), e);
        }
    }

    /**
     * Konvertuje Java POJO u Protobuf objekat.
     * Ova konverzija je potrebna jer Protobuf koristi generisane klase.
     */
    private UploadEventProto.UploadEvent toProto(UploadEvent event) {
        UploadEventProto.UploadEvent.Builder builder = UploadEventProto.UploadEvent.newBuilder();

        // Postavljamo polja (proveravamo null vrednosti)
        if (event.getEventId() != null) {
            builder.setEventId(event.getEventId());
        }
        if (event.getVideoId() != null) {
            builder.setVideoId(event.getVideoId());
        }
        if (event.getTitle() != null) {
            builder.setTitle(event.getTitle());
        }
        if (event.getAuthor() != null) {
            builder.setAuthor(event.getAuthor());
        }
        if (event.getFileSizeBytes() != null) {
            builder.setFileSizeBytes(event.getFileSizeBytes());
        }
        if (event.getDurationSeconds() != null) {
            builder.setDurationSeconds(event.getDurationSeconds());
        }
        if (event.getUploadTimestamp() != null) {
            builder.setUploadTimestamp(event.getUploadTimestamp());
        }
        if (event.getTags() != null) {
            builder.addAllTags(event.getTags());
        }
        if (event.getDescription() != null) {
            builder.setDescription(event.getDescription());
        }
        if (event.getReplicaId() != null) {
            builder.setReplicaId(event.getReplicaId());
        }

        return builder.build();
    }

    /**
     * Konvertuje Protobuf objekat u Java POJO.
     */
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
