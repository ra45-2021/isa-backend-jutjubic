package com.jutjubic.mqbenchmark.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

// DTO za upload event - koristi se za JSON i Protobuf serijalizaciju
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadEvent implements Serializable {

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
