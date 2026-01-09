package com.jutjubic.security;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class CommentRateLimiter {

    private static final long CAPACITY = 60;
    private static final long REFILL_INTERVAL_MS = 60_000;

    private final Clock clock;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public CommentRateLimiter(Clock clock) {
        this.clock = clock;
    }

    public void assertAllowed(String userKey) {
        boolean allowed = buckets
                .computeIfAbsent(userKey, k -> new Bucket(clock.millis()))
                .tryConsume(clock.millis());

        if (!allowed) {
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Rate limit exceeded: max 60 comments per hour. Try again in 1 minute."
            );
        }
    }

    private static final class Bucket {
        private final AtomicLong tokens = new AtomicLong(CAPACITY);
        private final AtomicLong lastRefillMs = new AtomicLong();

        Bucket(long nowMs) {
            lastRefillMs.set(nowMs);
        }

        boolean tryConsume(long nowMs) {
            refillIfNeeded(nowMs);

            while (true) {
                long current = tokens.get();
                if (current <= 0) return false;
                if (tokens.compareAndSet(current, current - 1)) return true;
            }
        }

        private void refillIfNeeded(long nowMs) {
            while (true) {
                long last = lastRefillMs.get();
                long elapsed = nowMs - last;
                if (elapsed < REFILL_INTERVAL_MS) return;

                long newTokens = elapsed / REFILL_INTERVAL_MS;
                long updatedLast = last + newTokens * REFILL_INTERVAL_MS;

                if (!lastRefillMs.compareAndSet(last, updatedLast)) continue;

                tokens.getAndUpdate(t -> Math.min(CAPACITY, t + newTokens));
                return;
            }
        }
    }
}
