package com.jutjubic.security;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class CommentRateLimiterTest {

    static final class MutableClock extends Clock {
        private volatile Instant instant;

        MutableClock(Instant start) { this.instant = start; }
        void plusSeconds(long s) { this.instant = this.instant.plusSeconds(s); }

        @Override public ZoneOffset getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId zone) { return this; }
        @Override public Instant instant() { return instant; }
    }

    @Test
    void allows60PerHour_blocks61st_then_refillsAfter1min() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        CommentRateLimiter limiter = new CommentRateLimiter(clock);

        String user = "ana.zaric@mail.com";

        for (int i = 0; i < 60; i++) {
            assertDoesNotThrow(() -> limiter.assertAllowed(user));
        }

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> limiter.assertAllowed(user));
        assertEquals(429, ex.getStatusCode().value());

        clock.plusSeconds(60);
        assertDoesNotThrow(() -> limiter.assertAllowed(user));
    }

    @Test
    void stressTest_concurrentRequests_finishesFast_and_neverExceeds60() throws Exception {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        CommentRateLimiter limiter = new CommentRateLimiter(clock);

        String user = "ana.zaric@mail.com";

        int threads = 64;
        int totalAttempts = 5000;

        ExecutorService pool = Executors.newFixedThreadPool(threads);

        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(totalAttempts);

        AtomicInteger allowed = new AtomicInteger(0);
        AtomicInteger blocked = new AtomicInteger(0);

        long t0 = System.currentTimeMillis();

        for (int i = 0; i < totalAttempts; i++) {
            pool.submit(() -> {
                try {
                    start.await();
                    try {
                        limiter.assertAllowed(user);
                        allowed.incrementAndGet();
                    } catch (ResponseStatusException e) {
                        blocked.incrementAndGet();
                    }
                } catch (InterruptedException ignored) {
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown();

        // "no degradation": must finish within time
        assertTrue(done.await(2, TimeUnit.SECONDS),
                "Too slow / possible degradation: did not finish within 2s");

        long durationMs = System.currentTimeMillis() - t0;

        pool.shutdown();
        assertTrue(pool.awaitTermination(2, TimeUnit.SECONDS),
                "Thread pool did not terminate quickly (possible resource leak)");
        pool.shutdownNow();

        assertEquals(60, allowed.get(), "Must allow exactly 60 per hour");
        assertEquals(totalAttempts - 60, blocked.get(), "All others must be blocked with 429");

        System.out.println("Stress test finished in " + durationMs + "ms");
    }
}
