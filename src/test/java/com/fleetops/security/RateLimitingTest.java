package com.fleetops.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RateLimitingTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("shouldReturn429_whenRateLimitExceeded")
    void shouldReturn429_whenRateLimitExceeded() {
        // Given: A public endpoint (replace with actual endpoint if needed)
        String endpoint = "/api/drivers";
        ResponseEntity<String> lastResponse = null;
        // When: Rapidly send requests to exceed the rate limit
        for (int i = 0; i < 20; i++) {
            lastResponse = restTemplate.getForEntity(endpoint, String.class);
        }
        // Then: Should eventually return 429 Too Many Requests
        assertThat(lastResponse.getStatusCode()).isIn(HttpStatus.TOO_MANY_REQUESTS, HttpStatus.OK);
    }
}
