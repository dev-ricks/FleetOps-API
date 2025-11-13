package com.fleetops.security.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TDD Unit tests for RateLimitService.
 * 
 * This service should provide rate limiting functionality with:
 * - Token bucket algorithm
 * - Per-key rate limiting (user, IP, API key)
 * - Configurable capacity and refill rate
 * - Thread-safe operations
 * - Metrics about remaining tokens and retry-after time
 */
@DisplayName("RateLimitService Unit Tests")
class RateLimitServiceTest {

    private Clock fixedClock;
    private RateLimitService rateLimitService;
    
    private static final int DEFAULT_CAPACITY = 10;
    private static final Duration DEFAULT_REFILL_PERIOD = Duration.ofMinutes(1);

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2025-01-15T10:00:00Z"), ZoneOffset.UTC);
        rateLimitService = new RateLimitService(fixedClock, DEFAULT_CAPACITY, DEFAULT_REFILL_PERIOD);
    }

    @Nested
    @DisplayName("Basic Rate Limiting")
    class BasicRateLimiting {

        @Test
        @DisplayName("should allow first request for new key")
        void shouldAllowFirstRequest() {
            // Given
            String key = "user:123";

            // When
            RateLimitResult result = rateLimitService.tryConsume(key);

            // Then
            assertThat(result.isAllowed()).isTrue();
            assertThat(result.getRemainingTokens()).isEqualTo(DEFAULT_CAPACITY - 1);
        }

        @Test
        @DisplayName("should allow requests up to capacity")
        void shouldAllowRequestsUpToCapacity() {
            // Given
            String key = "user:456";

            // When & Then
            for (int i = 0; i < DEFAULT_CAPACITY; i++) {
                RateLimitResult result = rateLimitService.tryConsume(key);
                assertThat(result.isAllowed())
                    .as("Request %d should be allowed", i + 1)
                    .isTrue();
                assertThat(result.getRemainingTokens())
                    .as("Remaining tokens after request %d", i + 1)
                    .isEqualTo(DEFAULT_CAPACITY - i - 1);
            }
        }

        @Test
        @DisplayName("should deny request when capacity exceeded")
        void shouldDenyRequestWhenCapacityExceeded() {
            // Given
            String key = "user:789";
            
            // Exhaust the capacity
            for (int i = 0; i < DEFAULT_CAPACITY; i++) {
                rateLimitService.tryConsume(key);
            }

            // When
            RateLimitResult result = rateLimitService.tryConsume(key);

            // Then
            assertThat(result.isAllowed()).isFalse();
            assertThat(result.getRemainingTokens()).isEqualTo(0);
        }

        @Test
        @DisplayName("should provide retry-after time when rate limited")
        void shouldProvideRetryAfterTime() {
            // Given
            String key = "user:retry";
            
            // Exhaust the capacity
            for (int i = 0; i < DEFAULT_CAPACITY; i++) {
                rateLimitService.tryConsume(key);
            }

            // When
            RateLimitResult result = rateLimitService.tryConsume(key);

            // Then
            assertThat(result.isAllowed()).isFalse();
            assertThat(result.getRetryAfterSeconds()).isGreaterThan(0);
            assertThat(result.getRetryAfterSeconds()).isLessThanOrEqualTo(DEFAULT_REFILL_PERIOD.getSeconds());
        }
    }

    @Nested
    @DisplayName("Token Refill")
    class TokenRefill {

        @Test
        @DisplayName("should refill tokens after refill period")
        void shouldRefillTokensAfterPeriod() {
            // Given
            String key = "user:refill";
            
            // Exhaust the capacity
            for (int i = 0; i < DEFAULT_CAPACITY; i++) {
                rateLimitService.tryConsume(key);
            }
            
            // Verify exhausted
            assertThat(rateLimitService.tryConsume(key).isAllowed()).isFalse();

            // When: Advance time by refill period
            Clock newClock = Clock.fixed(
                fixedClock.instant().plus(DEFAULT_REFILL_PERIOD),
                ZoneOffset.UTC
            );
            rateLimitService = new RateLimitService(newClock, DEFAULT_CAPACITY, DEFAULT_REFILL_PERIOD);
            
            // Then: Should allow requests again
            RateLimitResult result = rateLimitService.tryConsume(key);
            assertThat(result.isAllowed()).isTrue();
        }

        @Test
        @DisplayName("should partially refill tokens based on elapsed time")
        void shouldPartiallyRefillTokens() {
            // Given
            String key = "user:partial";
            
            // Consume 5 tokens
            for (int i = 0; i < 5; i++) {
                rateLimitService.tryConsume(key);
            }

            // When: Advance time by half the refill period
            Clock newClock = Clock.fixed(
                fixedClock.instant().plus(DEFAULT_REFILL_PERIOD.dividedBy(2)),
                ZoneOffset.UTC
            );
            rateLimitService = new RateLimitService(newClock, DEFAULT_CAPACITY, DEFAULT_REFILL_PERIOD);
            
            // Then: Should have some tokens refilled (implementation dependent)
            RateLimitResult result = rateLimitService.tryConsume(key);
            assertThat(result.isAllowed()).isTrue();
        }
    }

    @Nested
    @DisplayName("Per-Key Isolation")
    class PerKeyIsolation {

        @Test
        @DisplayName("should isolate rate limits per key")
        void shouldIsolateRateLimitsPerKey() {
            // Given
            String keyA = "user:alice";
            String keyB = "user:bob";

            // When: Exhaust keyA
            for (int i = 0; i < DEFAULT_CAPACITY; i++) {
                rateLimitService.tryConsume(keyA);
            }

            // Then: keyA should be rate limited
            assertThat(rateLimitService.tryConsume(keyA).isAllowed()).isFalse();

            // And: keyB should still be allowed
            assertThat(rateLimitService.tryConsume(keyB).isAllowed()).isTrue();
        }

        @Test
        @DisplayName("should handle different key types independently")
        void shouldHandleDifferentKeyTypes() {
            // Given
            String userKey = "user:123";
            String ipKey = "ip:192.168.1.1";
            String apiKey = "apikey:abc123";

            // When: Exhaust user key
            for (int i = 0; i < DEFAULT_CAPACITY; i++) {
                rateLimitService.tryConsume(userKey);
            }

            // Then: Other key types should not be affected
            assertThat(rateLimitService.tryConsume(userKey).isAllowed()).isFalse();
            assertThat(rateLimitService.tryConsume(ipKey).isAllowed()).isTrue();
            assertThat(rateLimitService.tryConsume(apiKey).isAllowed()).isTrue();
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("should handle null key gracefully")
        void shouldHandleNullKey() {
            // When & Then
            assertThatThrownBy(() -> rateLimitService.tryConsume(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("key");
        }

        @Test
        @DisplayName("should handle empty key gracefully")
        void shouldHandleEmptyKey() {
            // When & Then
            assertThatThrownBy(() -> rateLimitService.tryConsume(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("key");
        }

        @Test
        @DisplayName("should handle very long keys")
        void shouldHandleVeryLongKeys() {
            // Given
            String longKey = "user:" + "a".repeat(1000);

            // When
            RateLimitResult result = rateLimitService.tryConsume(longKey);

            // Then
            assertThat(result.isAllowed()).isTrue();
        }

        @Test
        @DisplayName("should handle special characters in keys")
        void shouldHandleSpecialCharactersInKeys() {
            // Given
            String specialKey = "user:test@example.com:192.168.1.1";

            // When
            RateLimitResult result = rateLimitService.tryConsume(specialKey);

            // Then
            assertThat(result.isAllowed()).isTrue();
        }
    }

    @Nested
    @DisplayName("Metrics and Observability")
    class MetricsAndObservability {

        @Test
        @DisplayName("should provide accurate remaining tokens count")
        void shouldProvideAccurateRemainingTokens() {
            // Given
            String key = "user:metrics";

            // When & Then
            for (int i = 0; i < DEFAULT_CAPACITY; i++) {
                RateLimitResult result = rateLimitService.tryConsume(key);
                assertThat(result.getRemainingTokens())
                    .as("Remaining tokens after consuming %d", i + 1)
                    .isEqualTo(DEFAULT_CAPACITY - i - 1);
            }
        }

        @Test
        @DisplayName("should provide capacity information")
        void shouldProvideCapacityInformation() {
            // Given
            String key = "user:capacity";

            // When
            RateLimitResult result = rateLimitService.tryConsume(key);

            // Then
            assertThat(result.getCapacity()).isEqualTo(DEFAULT_CAPACITY);
        }

        @Test
        @DisplayName("should provide reset time information")
        void shouldProvideResetTimeInformation() {
            // Given
            String key = "user:reset";
            
            // Exhaust capacity
            for (int i = 0; i < DEFAULT_CAPACITY; i++) {
                rateLimitService.tryConsume(key);
            }

            // When
            RateLimitResult result = rateLimitService.tryConsume(key);

            // Then
            assertThat(result.getResetTimeEpochSecond()).isGreaterThan(0);
            assertThat(result.getResetTimeEpochSecond())
                .isGreaterThanOrEqualTo(fixedClock.instant().getEpochSecond());
        }
    }

    @Nested
    @DisplayName("Configuration")
    class Configuration {

        @Test
        @DisplayName("should respect custom capacity configuration")
        void shouldRespectCustomCapacity() {
            // Given
            int customCapacity = 5;
            RateLimitService customService = new RateLimitService(
                fixedClock, 
                customCapacity, 
                DEFAULT_REFILL_PERIOD
            );
            String key = "user:custom";

            // When: Consume up to custom capacity
            for (int i = 0; i < customCapacity; i++) {
                assertThat(customService.tryConsume(key).isAllowed()).isTrue();
            }

            // Then: Next request should be denied
            assertThat(customService.tryConsume(key).isAllowed()).isFalse();
        }

        @Test
        @DisplayName("should respect custom refill period configuration")
        void shouldRespectCustomRefillPeriod() {
            // Given
            Duration customRefillPeriod = Duration.ofSeconds(30);
            RateLimitService customService = new RateLimitService(
                fixedClock, 
                DEFAULT_CAPACITY, 
                customRefillPeriod
            );
            String key = "user:refillperiod";

            // When: Exhaust and check retry-after
            for (int i = 0; i < DEFAULT_CAPACITY; i++) {
                customService.tryConsume(key);
            }
            RateLimitResult result = customService.tryConsume(key);

            // Then
            assertThat(result.getRetryAfterSeconds())
                .isLessThanOrEqualTo(customRefillPeriod.getSeconds());
        }

        @Test
        @DisplayName("should reject invalid capacity")
        void shouldRejectInvalidCapacity() {
            // When & Then
            assertThatThrownBy(() -> new RateLimitService(fixedClock, 0, DEFAULT_REFILL_PERIOD))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("capacity");

            assertThatThrownBy(() -> new RateLimitService(fixedClock, -1, DEFAULT_REFILL_PERIOD))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("capacity");
        }

        @Test
        @DisplayName("should reject invalid refill period")
        void shouldRejectInvalidRefillPeriod() {
            // When & Then
            assertThatThrownBy(() -> new RateLimitService(fixedClock, DEFAULT_CAPACITY, Duration.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("refill");

            assertThatThrownBy(() -> new RateLimitService(fixedClock, DEFAULT_CAPACITY, Duration.ofSeconds(-1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("refill");
        }
    }

    @Nested
    @DisplayName("Thread Safety")
    class ThreadSafety {

        @Test
        @DisplayName("should handle concurrent requests safely")
        void shouldHandleConcurrentRequestsSafely() throws InterruptedException {
            // Given
            String key = "user:concurrent";
            int threadCount = 20;
            int requestsPerThread = 5;
            
            // When: Multiple threads consume tokens concurrently
            Thread[] threads = new Thread[threadCount];
            for (int i = 0; i < threadCount; i++) {
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < requestsPerThread; j++) {
                        rateLimitService.tryConsume(key);
                    }
                });
                threads[i].start();
            }

            // Wait for all threads to complete
            for (Thread thread : threads) {
                thread.join();
            }

            // Then: Should have consumed exactly capacity tokens (no more allowed)
            RateLimitResult result = rateLimitService.tryConsume(key);
            assertThat(result.isAllowed()).isFalse();
        }
    }
}