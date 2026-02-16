package com.jutjubic.dto;

import java.io.Serializable;

public record TranscodeJobMessageDto(
        String jobId,
        Long postId,
        String inputAbsolutePath,
        String preset
) implements Serializable {}
