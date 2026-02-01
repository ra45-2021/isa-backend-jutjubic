package com.jutjubic.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom health controller koji pruža informacije o stanju replike.
 *
 * Koristi se pored standardnog /actuator/health endpoint-a za:
 * - Identifikaciju koja replika je odgovorila
 * - Proveru konekcije prema bazi
 * - Load balancer health checks
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Value("${replica.id:unknown}")
    private String replicaId;

    @Value("${server.port:8080}")
    private int serverPort;

    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Osnovni health check - brz odgovor za load balancer.
     * Vraća 200 OK ako je aplikacija živa.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("replicaId", replicaId);
        response.put("port", serverPort);
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }

    /**
     * Detaljni health check - proverava i konekciju prema bazi.
     * Koristan za debugging i monitoring.
     */
    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("replicaId", replicaId);
        response.put("port", serverPort);
        response.put("timestamp", System.currentTimeMillis());

        // Provera konekcije prema bazi
        boolean dbHealthy = checkDatabaseConnection();
        response.put("database", dbHealthy ? "UP" : "DOWN");

        // Ukupni status
        String overallStatus = dbHealthy ? "UP" : "DOWN";
        response.put("status", overallStatus);

        if (!dbHealthy) {
            return ResponseEntity.status(503).body(response); // Service Unavailable
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Proverava da li je konekcija prema bazi aktivna.
     */
    private boolean checkDatabaseConnection() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(2); // timeout 2 sekunde
        } catch (Exception e) {
            return false;
        }
    }
}
