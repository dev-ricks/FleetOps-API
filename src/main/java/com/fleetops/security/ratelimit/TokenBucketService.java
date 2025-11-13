package com.fleetops.security.ratelimit;

import io.github.bucket4j.*;

import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TokenBucketService {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final int capacity;
    private final Duration duration;
    private final Clock clock;

    public TokenBucketService(Clock clock, int capacity, Duration duration) {
        this.clock = clock;
        this.capacity = capacity;
        this.duration = duration;
    }

    /**
     * Try to consume 1 token for the given key.
     * Returns true when consumed (allowed), false when rate-limited.
     * <p>
     * Note: this uses an in-memory map of buckets. For distributed usage replace
     * the map with Bucket4j Redis-backed storage.
     */
    public boolean allowRequest(String key) {
        Bucket bucket = buckets.computeIfAbsent(key, k -> {
            // canonical Bucket4j 8.x construction: classic capacity + greedy refill
            Bandwidth limit = Bandwidth.classic(capacity, Refill.greedy(capacity, duration));
            return Bucket.builder().addLimit(limit).build();
        });

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        return probe.isConsumed();
    }
}
