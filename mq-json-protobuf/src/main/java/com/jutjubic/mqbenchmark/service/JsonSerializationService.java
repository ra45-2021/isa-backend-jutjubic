package com.jutjubic.mqbenchmark.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jutjubic.mqbenchmark.dto.UploadEvent;
import org.springframework.stereotype.Service;

/**
 * Servis za JSON serijalizaciju i deserijalizaciju UploadEvent objekata.
 *
 * Koristi Jackson ObjectMapper koji je standardna biblioteka za JSON u Javi.
 * Jackson je već uključen u spring-boot-starter-web zavisnost.
 *
 * Prednosti JSON-a:
 * - Čitljiv format (human-readable)
 * - Široka podrška u svim jezicima
 * - Lako debug-ovanje
 *
 * Mane JSON-a:
 * - Veća veličina poruke (tekstualni format)
 * - Sporija serijalizacija/deserijalizacija
 */
@Service
public class JsonSerializationService {

    private final ObjectMapper objectMapper;

    public JsonSerializationService() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Serijalizuje UploadEvent objekat u JSON byte niz.
     *
     * @param event UploadEvent objekat za serijalizaciju
     * @return byte[] JSON reprezentacija objekta
     */
    public byte[] serialize(UploadEvent event) {
        try {
            return objectMapper.writeValueAsBytes(event);
        } catch (Exception e) {
            throw new RuntimeException("Greška pri JSON serijalizaciji: " + e.getMessage(), e);
        }
    }

    /**
     * Deserijalizuje JSON byte niz u UploadEvent objekat.
     *
     * @param data byte[] JSON podaci
     * @return UploadEvent deserijalizovani objekat
     */
    public UploadEvent deserialize(byte[] data) {
        try {
            return objectMapper.readValue(data, UploadEvent.class);
        } catch (Exception e) {
            throw new RuntimeException("Greška pri JSON deserijalizaciji: " + e.getMessage(), e);
        }
    }

    /**
     * Vraća JSON string reprezentaciju (za debug/logovanje).
     */
    public String toJsonString(UploadEvent event) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(event);
        } catch (Exception e) {
            throw new RuntimeException("Greška pri konverziji u JSON string: " + e.getMessage(), e);
        }
    }
}
