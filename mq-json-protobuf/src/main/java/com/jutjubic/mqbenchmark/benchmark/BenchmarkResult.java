package com.jutjubic.mqbenchmark.benchmark;

import lombok.Builder;
import lombok.Data;

/**
 * DTO klasa koja sadrži rezultate benchmark testa.
 *
 * Prikazuje poređenje između JSON i Protobuf formata:
 * - Prosečno vreme serijalizacije
 * - Prosečno vreme deserijalizacije
 * - Prosečna veličina poruke
 * - Ukupan broj poruka
 */
@Data
@Builder
public class BenchmarkResult {

    /**
     * Broj poruka korišćenih u benchmark testu.
     */
    private int messageCount;

    // ========================================
    // JSON REZULTATI
    // ========================================

    /**
     * Prosečno vreme JSON serijalizacije u mikrosekundama.
     */
    private double jsonAvgSerializationMicros;

    /**
     * Prosečno vreme JSON deserijalizacije u mikrosekundama.
     */
    private double jsonAvgDeserializationMicros;

    /**
     * Prosečna veličina JSON poruke u bajtovima.
     */
    private double jsonAvgMessageSizeBytes;

    /**
     * Ukupna veličina svih JSON poruka u bajtovima.
     */
    private long jsonTotalSizeBytes;

    // ========================================
    // PROTOBUF REZULTATI
    // ========================================

    /**
     * Prosečno vreme Protobuf serijalizacije u mikrosekundama.
     */
    private double protobufAvgSerializationMicros;

    /**
     * Prosečno vreme Protobuf deserijalizacije u mikrosekundama.
     */
    private double protobufAvgDeserializationMicros;

    /**
     * Prosečna veličina Protobuf poruke u bajtovima.
     */
    private double protobufAvgMessageSizeBytes;

    /**
     * Ukupna veličina svih Protobuf poruka u bajtovima.
     */
    private long protobufTotalSizeBytes;

    // ========================================
    // POREĐENJE
    // ========================================

    /**
     * Koliko puta je Protobuf brži od JSON-a pri serijalizaciji.
     * Vrednost > 1 znači da je Protobuf brži.
     */
    private double serializationSpeedup;

    /**
     * Koliko puta je Protobuf brži od JSON-a pri deserijalizaciji.
     * Vrednost > 1 znači da je Protobuf brži.
     */
    private double deserializationSpeedup;

    /**
     * Koliko puta je Protobuf manji od JSON-a.
     * Vrednost > 1 znači da je Protobuf manji.
     */
    private double sizeReduction;

    /**
     * Tekstualni sažetak rezultata.
     */
    private String summary;
}
