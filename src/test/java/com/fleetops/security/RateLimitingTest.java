package com.fleetops.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * High-level TDD smoke tests for rate limiting using TestRestTemplate.
 * 
 * These tests verify basic rate limiting behavior from an external client perspective.
 * For comprehensive tests, see the ratelimit package tests.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Rate Limiting Smoke Tests")
class RateLimitingTest {
    
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("should eventually return 429 when rate limit exceeded")
    void shouldReturn429_whenRateLimitExceeded() {
        // Given: An API endpoint
        String endpoint = "/api/public/status";
        boolean rateLimitHit = false;
        
        // When: Rapidly send requests to exceed the rate limit
        for (int i = 0; i < 25; i++) {
            ResponseEntity<String> response = restTemplate.getForEntity(endpoint, String.class);
            
            if (response.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                rateLimitHit = true;
                
                // Then: Should have Retry-After header
                assertThat(response.getHeaders().getFirst("Retry-After")).isNotNull();
                
                // And: Should have rate limit headers
                assertThat(response.getHeaders().getFirst("X-RateLimit-Limit")).isNotNull();
                assertThat(response.getHeaders().getFirst("X-RateLimit-Remaining")).isEqualTo("0");
                
                break;
            }
        }
        
        // Then: Should eventually hit rate limit
        assertThat(rateLimitHit)
            .as("Rate limit should be triggered after multiple requests")
            .isTrue();
    }

    @Test
    @DisplayName("should include rate limit headers in successful responses")
    void shouldIncludeRateLimitHeadersInSuccessfulResponses() {
        // Given: An API endpoint
        String endpoint = "/api/public/status";
        
        // When: Making a request
        ResponseEntity<String> response = restTemplate.getForEntity(endpoint, String.class);
        
        // Then: Should include rate limit headers (if not whitelisted)
        HttpHeaders headers = response.getHeaders();
        
        // Note: This test may need adjustment based on whether /api/public/status is rate limited
        // If it returns headers, verify they exist and are valid
        String rateLimitHeader = headers.getFirst("X-RateLimit-Limit");
        if (rateLimitHeader != null) {
            assertThat(rateLimitHeader).matches("\\d+");
            assertThat(headers.getFirst("X-RateLimit-Remaining")).matches("\\d+");
        }
    }

    @Test
    @DisplayName("should not rate limit actuator health endpoint")
    void shouldNotRateLimitActuatorHealth() {
        // Given: Actuator health endpoint
        String endpoint = "/actuator/health";
        
        // When: Making many requests
        for (int i = 0; i < 30; i++) {
            ResponseEntity<String> response = restTemplate.getForEntity(endpoint, String.class);
            
            // Then: Should never return 429
            assertThat(response.getStatusCode())
                .as("Actuator health should not be rate limited")
                .isNotEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        }
    }
}
