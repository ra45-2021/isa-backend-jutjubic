package com.jutjubic.mqbenchmark;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Glavna klasa aplikacije za benchmark JSON vs Protobuf serijalizacije.
 *
 * Ova aplikacija:
 * 1. Prima UploadEvent poruke iz RabbitMQ (poslate od jutjubic aplikacije)
 * 2. Meri vreme deserijalizacije za oba formata
 * 3. Izvršava benchmark testove i prikazuje rezultate
 */
@SpringBootApplication
public class MqBenchmarkApplication {

    public static void main(String[] args) {
        SpringApplication.run(MqBenchmarkApplication.class, args);
    }
}
