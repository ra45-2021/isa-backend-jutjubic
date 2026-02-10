package com.jutjubic.mqbenchmark.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jutjubic.mqbenchmark.dto.UploadEvent;
import org.springframework.stereotype.Service;

// JSON serijalizacija/deserijalizacija preko Jackson-a
@Service
public class JsonSerializationService {

    private final ObjectMapper objectMapper;

    public JsonSerializationService() {
        this.objectMapper = new ObjectMapper();
    }

    public byte[] serialize(UploadEvent event) {
        try {
            return objectMapper.writeValueAsBytes(event);
        } catch (Exception e) {
            throw new RuntimeException("Greška pri JSON serijalizaciji: " + e.getMessage(), e);
        }
    }

    public UploadEvent deserialize(byte[] data) {
        try {
            return objectMapper.readValue(data, UploadEvent.class);
        } catch (Exception e) {
            throw new RuntimeException("Greška pri JSON deserijalizaciji: " + e.getMessage(), e);
        }
    }

    public String toJsonString(UploadEvent event) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(event);
        } catch (Exception e) {
            throw new RuntimeException("Greška pri konverziji u JSON string: " + e.getMessage(), e);
        }
    }
}
