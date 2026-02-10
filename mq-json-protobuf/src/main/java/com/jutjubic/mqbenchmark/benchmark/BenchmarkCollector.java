package com.jutjubic.mqbenchmark.benchmark;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Prikuplja benchmark statistiku za JSON i Protobuf
@Slf4j
@Component
public class BenchmarkCollector {

    // JSON statistika
    private final List<Long> jsonSerializationTimesNanos = Collections.synchronizedList(new ArrayList<>());
    private final List<Long> jsonDeserializationTimesNanos = Collections.synchronizedList(new ArrayList<>());
    private final List<Integer> jsonMessageSizes = Collections.synchronizedList(new ArrayList<>());

    // Protobuf statistika
    private final List<Long> protobufSerializationTimesNanos = Collections.synchronizedList(new ArrayList<>());
    private final List<Long> protobufDeserializationTimesNanos = Collections.synchronizedList(new ArrayList<>());
    private final List<Integer> protobufMessageSizes = Collections.synchronizedList(new ArrayList<>());

    public void recordJsonSerialization(int messageSize, long serializationTimeNanos) {
        jsonSerializationTimesNanos.add(serializationTimeNanos);
        jsonMessageSizes.add(messageSize);
    }

    public void recordJsonReceived(int messageSize, long deserializationTimeNanos) {
        jsonDeserializationTimesNanos.add(deserializationTimeNanos);
    }

    public void recordProtobufSerialization(int messageSize, long serializationTimeNanos) {
        protobufSerializationTimesNanos.add(serializationTimeNanos);
        protobufMessageSizes.add(messageSize);
    }

    public void recordProtobufReceived(int messageSize, long deserializationTimeNanos) {
        protobufDeserializationTimesNanos.add(deserializationTimeNanos);
    }

    private double calculateAverageNanos(List<Long> values) {
        if (values.isEmpty()) return 0;
        return values.stream().mapToLong(Long::longValue).average().orElse(0);
    }

    private double calculateAverageSize(List<Integer> values) {
        if (values.isEmpty()) return 0;
        return values.stream().mapToInt(Integer::intValue).average().orElse(0);
    }

    private long calculateTotalSize(List<Integer> values) {
        return values.stream().mapToLong(Integer::intValue).sum();
    }

    // Generise rezultate benchmark-a
    public BenchmarkResult generateResult() {
        double jsonAvgSerMicros = calculateAverageNanos(jsonSerializationTimesNanos) / 1000.0;
        double jsonAvgDeserMicros = calculateAverageNanos(jsonDeserializationTimesNanos) / 1000.0;
        double jsonAvgSize = calculateAverageSize(jsonMessageSizes);
        long jsonTotalSize = calculateTotalSize(jsonMessageSizes);

        double protoAvgSerMicros = calculateAverageNanos(protobufSerializationTimesNanos) / 1000.0;
        double protoAvgDeserMicros = calculateAverageNanos(protobufDeserializationTimesNanos) / 1000.0;
        double protoAvgSize = calculateAverageSize(protobufMessageSizes);
        long protoTotalSize = calculateTotalSize(protobufMessageSizes);

        // Speedup - koliko puta je Protobuf brzi
        double serSpeedup = jsonAvgSerMicros > 0 ? jsonAvgSerMicros / protoAvgSerMicros : 0;
        double deserSpeedup = jsonAvgDeserMicros > 0 ? jsonAvgDeserMicros / protoAvgDeserMicros : 0;
        double sizeReduction = protoAvgSize > 0 ? jsonAvgSize / protoAvgSize : 0;

        int messageCount = jsonMessageSizes.size();

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

    public void reset() {
        jsonSerializationTimesNanos.clear();
        jsonDeserializationTimesNanos.clear();
        jsonMessageSizes.clear();
        protobufSerializationTimesNanos.clear();
        protobufDeserializationTimesNanos.clear();
        protobufMessageSizes.clear();
        log.info("Benchmark statistika resetovana");
    }

    public int getJsonMessageCount() {
        return jsonMessageSizes.size();
    }

    public int getProtobufMessageCount() {
        return protobufMessageSizes.size();
    }
}
