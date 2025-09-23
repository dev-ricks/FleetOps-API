package com.fleetops.security.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TokenBucketServiceTest {

    private final int serviceTokenCapacity = 5;
    private TokenBucketService service;

    @BeforeEach
    void setUp() {
        // Use a fixed clock for deterministic behavior in unit tests.
        Clock fixedClock = Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneOffset.UTC);
        service = new TokenBucketService(fixedClock, serviceTokenCapacity, Duration.ofSeconds(1));
    }

    @Test
    void initializeBucket_FirstRequestShouldBeAllowed() {
        TokenBucketService tokenBucketService = new TokenBucketService(Clock.systemUTC(), 5, Duration.ofSeconds(1));
        assertTrue(tokenBucketService.allowRequest("test-key"), "first request to a new bucket should be allowed");
    }

    @Test
    void allowRequest_WithNRequests_ShouldAllowNRequests() {
        String key = "user-123";
        for (int i = 0; i < serviceTokenCapacity; i++) {
            boolean allowed = service.allowRequest(key);
            assertTrue(allowed, "request " + i + " should be allowed");
        }
    }

    @Test
    void allowRequest_ToNCapacity_ShouldThenRejectNextRequest() {
        String key = "user-123";
        for (int i = 0; i < serviceTokenCapacity; i++) {
            assertTrue(service.allowRequest(key), "request " + i + " should be allowed");
        }
        // next request should be denied since capacity exhausted and the clock is fixed (no refill)
        assertFalse(service.allowRequest(key), "request after capacity exhausted should be denied");
    }

    @Test
    void perKeyIsolation_ShouldNotShareStateBetweenKeys() {
        String keyA = "user-A";
        String keyB = "user-B";

        // Exhaust keyA
        for (int i = 0; i < serviceTokenCapacity; i++) {
            assertTrue(service.allowRequest(keyA), "keyA request " + i + " should be allowed");
        }
        assertFalse(service.allowRequest(keyA), "keyA should now be rate limited");

        // keyB should still be fresh and allow full capacity
        for (int i = 0; i < serviceTokenCapacity; i++) {
            assertTrue(service.allowRequest(keyB), "keyB request " + i + " should be allowed");
        }
    }
}
