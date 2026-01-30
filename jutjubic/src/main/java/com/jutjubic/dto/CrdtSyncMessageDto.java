package com.jutjubic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrdtSyncMessageDto {
    private String action = "View Count Sync";
    private String timestamp = LocalDateTime.now().toString();
    private String sourceReplicaId;
    private Long videoId;
    private Long count;
}