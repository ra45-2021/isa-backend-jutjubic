package com.jutjubic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
  DTO klasa koja predstavlja događaj upload-a novog videa.

  Ova klasa se koristi za slanje poruke u RabbitMQ kada
  korisnik upload-uje novi video na platformu.

  Poruka se šalje u dva formata:
  1. JSON - za upload.events.json.queue
  2. Protobuf - za upload.events.protobuf.queue
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadEventDto implements Serializable {

    private String eventId;

    private Long videoId;

    private String title;

    private String author;

    private Long fileSizeBytes;

    private Integer durationSeconds;

    private Long uploadTimestamp;

    private List<String> tags;

    private String description;

    private String replicaId;
}
