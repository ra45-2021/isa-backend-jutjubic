package com.jutjubic.mqbenchmark.controller;

import com.jutjubic.mqbenchmark.benchmark.BenchmarkResult;
import com.jutjubic.mqbenchmark.benchmark.BenchmarkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// REST Controller za pokretanje benchmark testova
@Slf4j
@RestController
@RequestMapping("/api/benchmark")
public class BenchmarkController {

    private final BenchmarkService benchmarkService;

    public BenchmarkController(BenchmarkService benchmarkService) {
        this.benchmarkService = benchmarkService;
    }

    @GetMapping("/local")
    public ResponseEntity<BenchmarkResult> runLocalBenchmark(
            @RequestParam(defaultValue = "100") int count
    ) {
        log.info("Primljen zahtev za lokalni benchmark sa {} poruka", count);

        if (count < 1 || count > 10000) {
            return ResponseEntity.badRequest().build();
        }

        BenchmarkResult result = benchmarkService.runLocalBenchmark(count);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/rabbitmq")
    public ResponseEntity<BenchmarkResult> runRabbitMqBenchmark(
            @RequestParam(defaultValue = "100") int count
    ) {
        log.info("Primljen zahtev za RabbitMQ benchmark sa {} poruka", count);

        if (count < 1 || count > 10000) {
            return ResponseEntity.badRequest().build();
        }

        BenchmarkResult result = benchmarkService.runRabbitMqBenchmark(count);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/results")
    public ResponseEntity<BenchmarkResult> getCurrentResults() {
        BenchmarkResult result = benchmarkService.getCurrentResults();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/reset")
    public ResponseEntity<String> reset() {
        benchmarkService.reset();
        return ResponseEntity.ok("Benchmark statistika resetovana");
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("MQ Benchmark aplikacija radi!");
    }
}
