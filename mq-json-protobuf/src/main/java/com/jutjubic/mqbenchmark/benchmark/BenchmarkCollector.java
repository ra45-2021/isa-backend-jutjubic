package com.jutjubic.mqbenchmark.benchmark;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Komponenta za prikupljanje benchmark statistike.
 *
 * Beleži:
 * - Vremena serijalizacije/deserijalizacije za JSON i Protobuf
 * - Veličine poruka
 *
 * Thread-safe implementacija korišćenjem synchronized lista.
 */
@Slf4j
@Component
public class BenchmarkCollector {

    // Liste za JSON statistiku
    private final List<Long> jsonSerializationTimesNanos = Collections.synchronizedList(new ArrayList<>());
    private final List<Long> jsonDeserializationTimesNanos = Collections.synchronizedList(new ArrayList<>());
    private final List<Integer> jsonMessageSizes = Collections.synchronizedList(new ArrayList<>());

    // Liste za Protobuf statistiku
    private final List<Long> protobufSerializationTimesNanos = Collections.synchronizedList(new ArrayList<>());
    private final List<Long> protobufDeserializationTimesNanos = Collections.synchronizedList(new ArrayList<>());
    private final List<Integer> protobufMessageSizes = Collections.synchronizedList(new ArrayList<>());

    // ========================================
    // METODE ZA BELEŽENJE STATISTIKE
    // ========================================

    /**
     * Beleži statistiku za JSON serijalizaciju (poziva Producer).
     */
    public void recordJsonSerialization(int messageSize, long serializationTimeNanos) {
        jsonSerializationTimesNanos.add(serializationTimeNanos);
        jsonMessageSizes.add(messageSize);
    }

    /**
     * Beleži statistiku za JSON deserijalizaciju (poziva Consumer).
     */
    public void recordJsonReceived(int messageSize, long deserializationTimeNanos) {
        jsonDeserializationTimesNanos.add(deserializationTimeNanos);
        // Veličinu beležimo samo jednom (pri serijalizaciji)
    }

    /**
     * Beleži statistiku za Protobuf serijalizaciju (poziva Producer).
     */
    public void recordProtobufSerialization(int messageSize, long serializationTimeNanos) {
        protobufSerializationTimesNanos.add(serializationTimeNanos);
        protobufMessageSizes.add(messageSize);
    }

    /**
     * Beleži statistiku za Protobuf deserijalizaciju (poziva Consumer).
     */
    public void recordProtobufReceived(int messageSize, long deserializationTimeNanos) {
        protobufDeserializationTimesNanos.add(deserializationTimeNanos);
        // Veličinu beležimo samo jednom (pri serijalizaciji)
    }

    // ========================================
    // METODE ZA IZRAČUNAVANJE REZULTATA
    // ========================================

    /**
     * Izračunava prosek liste Long vrednosti.
     */
    private double calculateAverageNanos(List<Long> values) {
        if (values.isEmpty()) return 0;
        return values.stream().mapToLong(Long::longValue).average().orElse(0);
    }

    /**
     * Izračunava prosek liste Integer vrednosti.
     */
    private double calculateAverageSize(List<Integer> values) {
        if (values.isEmpty()) return 0;
        return values.stream().mapToInt(Integer::intValue).average().orElse(0);
    }

    /**
     * Izračunava sumu liste Integer vrednosti.
     */
    private long calculateTotalSize(List<Integer> values) {
        return values.stream().mapToLong(Integer::intValue).sum();
    }

    /**
     * Generiše rezultate benchmark-a.
     */
    public BenchmarkResult generateResult() {
        // Konvertuj nanosekunde u mikrosekunde za čitljiviji prikaz
        double jsonAvgSerMicros = calculateAverageNanos(jsonSerializationTimesNanos) / 1000.0;
        double jsonAvgDeserMicros = calculateAverageNanos(jsonDeserializationTimesNanos) / 1000.0;
        double jsonAvgSize = calculateAverageSize(jsonMessageSizes);
        long jsonTotalSize = calculateTotalSize(jsonMessageSizes);

        double protoAvgSerMicros = calculateAverageNanos(protobufSerializationTimesNanos) / 1000.0;
        double protoAvgDeserMicros = calculateAverageNanos(protobufDeserializationTimesNanos) / 1000.0;
        double protoAvgSize = calculateAverageSize(protobufMessageSizes);
        long protoTotalSize = calculateTotalSize(protobufMessageSizes);

        // Izračunaj speedup (koliko puta je Protobuf brži)
        double serSpeedup = jsonAvgSerMicros > 0 ? jsonAvgSerMicros / protoAvgSerMicros : 0;
        double deserSpeedup = jsonAvgDeserMicros > 0 ? jsonAvgDeserMicros / protoAvgDeserMicros : 0;
        double sizeReduction = protoAvgSize > 0 ? jsonAvgSize / protoAvgSize : 0;

        int messageCount = jsonMessageSizes.size();

        // Generiši sažetak
        String summary = String.format(
                """
                ╔══════════════════════════════════════════════════════════════╗
                ║           BENCHMARK REZULTATI: JSON vs Protobuf              ║
                ╠══════════════════════════════════════════════════════════════╣
                ║  Broj poruka: %-46d ║
                ╠══════════════════════════════════════════════════════════════╣
                ║                        JSON         Protobuf      Razlika    ║
                ╠══════════════════════════════════════════════════════════════╣
                ║  Serijalizacija:    %8.2f μs    %8.2f μs    %.2fx brži   ║
                ║  Deserijalizacija:  %8.2f μs    %8.2f μs    %.2fx brži   ║
                ║  Veličina poruke:   %8.1f B     %8.1f B     %.2fx manji  ║
                ║  Ukupna veličina:   %8d B     %8d B                   ║
                ╚══════════════════════════════════════════════════════════════╝
                """,
                messageCount,
                jsonAvgSerMicros, protoAvgSerMicros, serSpeedup,
                jsonAvgDeserMicros, protoAvgDeserMicros, deserSpeedup,
                jsonAvgSize, protoAvgSize, sizeReduction,
                jsonTotalSize, protoTotalSize
        );

        log.info("\n{}", summary);

        return BenchmarkResult.builder()
                .messageCount(messageCount)
                .jsonAvgSerializationMicros(jsonAvgSerMicros)
                .jsonAvgDeserializationMicros(jsonAvgDeserMicros)
                .jsonAvgMessageSizeBytes(jsonAvgSize)
                .jsonTotalSizeBytes(jsonTotalSize)
                .protobufAvgSerializationMicros(protoAvgSerMicros)
                .protobufAvgDeserializationMicros(protoAvgDeserMicros)
                .protobufAvgMessageSizeBytes(protoAvgSize)
                .protobufTotalSizeBytes(protoTotalSize)
                .serializationSpeedup(serSpeedup)
                .deserializationSpeedup(deserSpeedup)
                .sizeReduction(sizeReduction)
                .summary(summary)
                .build();
    }

    /**
     * Resetuje sve statistike.
     */
    public void reset() {
        jsonSerializationTimesNanos.clear();
        jsonDeserializationTimesNanos.clear();
        jsonMessageSizes.clear();
        protobufSerializationTimesNanos.clear();
        protobufDeserializationTimesNanos.clear();
        protobufMessageSizes.clear();
        log.info("Benchmark statistika resetovana");
    }

    /**
     * Vraća broj prikupljenih JSON poruka.
     */
    public int getJsonMessageCount() {
        return jsonMessageSizes.size();
    }

    /**
     * Vraća broj prikupljenih Protobuf poruka.
     */
    public int getProtobufMessageCount() {
        return protobufMessageSizes.size();
    }
}
