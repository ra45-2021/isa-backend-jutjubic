package com.jutjubic.service;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ActiveUserMetrics {

    private final Map<String, Instant> lastActivity = new ConcurrentHashMap<>();
    private final AtomicInteger activeUsers = new AtomicInteger(0);

    // consider user active if last request was within 5 minutes
    private static final long ACTIVE_WINDOW_SECONDS = 24 * 60 * 60;

    public ActiveUserMetrics(MeterRegistry registry) {
        registry.gauge("active_users", activeUsers, AtomicInteger::get);
    }

    // call this on each valid request
    public void recordActivity(String userId) {
        lastActivity.put(userId, Instant.now());
        recalcActiveUsers();
    }

    private void recalcActiveUsers() {
        Instant now = Instant.now();
        long count = lastActivity.values().stream()
                .filter(ts -> ts.isAfter(now.minusSeconds(ACTIVE_WINDOW_SECONDS)))
                .count();
        activeUsers.set((int) count);
    }
}
