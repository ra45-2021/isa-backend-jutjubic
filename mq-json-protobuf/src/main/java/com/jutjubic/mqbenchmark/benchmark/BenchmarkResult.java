package com.jutjubic.mqbenchmark.benchmark;

import lombok.Builder;
import lombok.Data;

// DTO za rezultate benchmark testa (JSON vs Protobuf)
@Data
@Builder
public class BenchmarkResult {

    private int messageCount;

    // JSON rezultati
    private double jsonAvgSerializationMicros;
    private double jsonAvgDeserializationMicros;
    private double jsonAvgMessageSizeBytes;
    private long jsonTotalSizeBytes;

    // Protobuf rezultati
    private double protobufAvgSerializationMicros;
    private double protobufAvgDeserializationMicros;
    private double protobufAvgMessageSizeBytes;
    private long protobufTotalSizeBytes;

    // Poredjenje (koliko puta je Protobuf brzi/manji)
    private double serializationSpeedup;
    private double deserializationSpeedup;
    private double sizeReduction;

    private String summary;
}
