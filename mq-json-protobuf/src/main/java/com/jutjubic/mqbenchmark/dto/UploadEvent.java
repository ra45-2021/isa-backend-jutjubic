package com.jutjubic.mqbenchmark.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * DTO klasa koja predstavlja događaj upload-a novog videa.
 *
 * Ova klasa se koristi za:
 * 1. JSON serijalizaciju/deserijalizaciju (preko Jackson-a)
 * 2. Konverziju u/iz Protobuf formata
 *
 * Sadrži osnovne informacije o novokreiranom videu koje se
 * šalju kroz RabbitMQ kada korisnik upload-uje video.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadEvent implements Serializable {

    /**
     * Jedinstveni identifikator događaja (UUID).
     * Svaki upload generiše novi UUID.
     */
    private String eventId;

    /**
     * ID videa (Post) u bazi podataka.
     */
    private Long videoId;

    /**
     * Naslov/naziv videa koji je korisnik uneo.
     */
    private String title;

    /**
     * Korisničko ime autora koji je upload-ovao video.
     */
    private String author;

    /**
     * Veličina video fajla u bajtovima.
     */
    private Long fileSizeBytes;

    /**
     * Trajanje videa u sekundama.
     * Može biti null ako trajanje nije poznato.
     */
    private Integer durationSeconds;

    /**
     * Unix timestamp (milisekunde) kada je video upload-ovan.
     */
    private Long uploadTimestamp;

    /**
     * Lista tagova koje je korisnik dodao videu.
     */
    private List<String> tags;

    /**
     * Opis videa koji je korisnik uneo.
     */
    private String description;

    /**
     * ID replike koja je obradila upload.
     * Korisno za praćenje koja instanca aplikacije je primila zahtev.
     */
    private String replicaId;
}
