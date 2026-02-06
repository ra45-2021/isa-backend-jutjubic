package com.jutjubic.mqbenchmark.controller;

import com.jutjubic.mqbenchmark.benchmark.BenchmarkResult;
import com.jutjubic.mqbenchmark.benchmark.BenchmarkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller za pokretanje benchmark testova.
 *
 * Endpoints:
 * - GET /api/benchmark/local?count=100  - Lokalni benchmark (bez RabbitMQ)
 * - GET /api/benchmark/rabbitmq?count=100 - Benchmark kroz RabbitMQ
 * - GET /api/benchmark/results - Trenutni rezultati
 * - POST /api/benchmark/reset - Resetuje statistiku
 */
@Slf4j
@RestController
@RequestMapping("/api/benchmark")
public class BenchmarkController {

    private final BenchmarkService benchmarkService;

    public BenchmarkController(BenchmarkService benchmarkService) {
        this.benchmarkService = benchmarkService;
    }

    /**
     * Pokreće lokalni benchmark (bez slanja u RabbitMQ).
     *
     * Ovo testira čiste performanse serijalizacije/deserijalizacije
     * bez uticaja mrežne latencije.
     *
     * Primer: GET /api/benchmark/local?count=100
     *
     * @param count Broj poruka za testiranje (default: 100)
     * @return BenchmarkResult sa rezultatima
     */
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

    /**
     * Pokreće benchmark kroz RabbitMQ.
     *
     * Šalje poruke u oba formata kroz RabbitMQ queue-ove.
     * Consumer prima poruke i beleži vreme deserijalizacije.
     *
     * Primer: GET /api/benchmark/rabbitmq?count=100
     *
     * @param count Broj poruka za testiranje (default: 100)
     * @return BenchmarkResult sa rezultatima
     */
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

    /**
     * Vraća trenutne rezultate benchmark-a.
     *
     * Korisno za praćenje rezultata dok se poruke primaju.
     *
     * Primer: GET /api/benchmark/results
     */
    @GetMapping("/results")
    public ResponseEntity<BenchmarkResult> getCurrentResults() {
        BenchmarkResult result = benchmarkService.getCurrentResults();
        return ResponseEntity.ok(result);
    }

    /**
     * Resetuje sve statistike.
     *
     * Primer: POST /api/benchmark/reset
     */
    @PostMapping("/reset")
    public ResponseEntity<String> reset() {
        benchmarkService.reset();
        return ResponseEntity.ok("Benchmark statistika resetovana");
    }

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("MQ Benchmark aplikacija radi!");
    }
}
