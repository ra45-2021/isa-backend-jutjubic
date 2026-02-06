package com.jutjubic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * DTO klasa koja predstavlja događaj upload-a novog videa.
 *
 * Ova klasa se koristi za slanje poruke u RabbitMQ kada
 * korisnik upload-uje novi video na platformu.
 *
 * Poruka se šalje u dva formata:
 * 1. JSON - za upload.events.json.queue
 * 2. Protobuf - za upload.events.protobuf.queue
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadEventDto implements Serializable {

    /**
     * Jedinstveni identifikator događaja (UUID).
     */
    private String eventId;

    /**
     * ID videa (Post) u bazi podataka.
     */
    private Long videoId;

    /**
     * Naslov videa.
     */
    private String title;

    /**
     * Korisničko ime autora.
     */
    private String author;

    /**
     * Veličina video fajla u bajtovima.
     */
    private Long fileSizeBytes;

    /**
     * Trajanje videa u sekundama.
     */
    private Integer durationSeconds;

    /**
     * Unix timestamp (milisekunde) kada je video upload-ovan.
     */
    private Long uploadTimestamp;

    /**
     * Lista tagova videa.
     */
    private List<String> tags;

    /**
     * Opis videa.
     */
    private String description;

    /**
     * ID replike koja je obradila upload (replica_1 ili replica_2).
     */
    private String replicaId;
}
