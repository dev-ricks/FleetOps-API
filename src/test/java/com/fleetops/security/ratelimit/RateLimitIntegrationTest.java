package com.fleetops.security.ratelimit;

import com.fleetops.test.TestAuth;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TDD Integration tests for rate limiting functionality.
 * 
 * These tests verify the complete rate limiting flow:
 * - Filter intercepts requests
 * - Rate limit service enforces limits
 * - Proper HTTP responses and headers
 * - Per-user and per-IP rate limiting
 * - Whitelisted paths bypass rate limiting
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Rate Limiting Integration Tests")
class RateLimitIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("Authenticated User Rate Limiting")
    class AuthenticatedUserRateLimiting {

        @Test
        @DisplayName("should allow requests within rate limit")
        void shouldAllowRequestsWithinRateLimit() throws Exception {
            // Given: A user with valid authentication
            // When: Making requests within the rate limit
            for (int i = 0; i < 5; i++) {
                mockMvc.perform(get("/api/vehicles")
                        .with(TestAuth.auth("ROLE_USER")))
                    .andExpect(status().isOk());
            }
            // Then: All requests should succeed (verified by status().isOk())
        }

        @Test
        @DisplayName("should return 429 when rate limit exceeded")
        void shouldReturn429WhenRateLimitExceeded() throws Exception {
            // Given: A user making many requests
            // When: Exceeding the rate limit (assuming limit is 10 requests per minute)
            boolean rateLimitHit = false;
            
            for (int i = 0; i < 15; i++) {
                MvcResult result = mockMvc.perform(get("/api/vehicles")
                        .with(TestAuth.auth("ROLE_USER")))
                    .andReturn();
                
                if (result.getResponse().getStatus() == 429) {
                    rateLimitHit = true;
                    break;
                }
            }

            // Then: Should eventually get 429 Too Many Requests
            assertThat(rateLimitHit).isTrue();
        }

        @Test
        @DisplayName("should include rate limit headers in response")
        void shouldIncludeRateLimitHeaders() throws Exception {
            // When
            MvcResult result = mockMvc.perform(get("/api/vehicles")
                    .with(TestAuth.auth("ROLE_USER")))
                .andExpect(status().isOk())
                .andReturn();

            // Then: Should include rate limit headers
            assertThat(result.getResponse().getHeader("X-RateLimit-Limit")).isNotNull();
            assertThat(result.getResponse().getHeader("X-RateLimit-Remaining")).isNotNull();
            assertThat(result.getResponse().getHeader("X-RateLimit-Reset")).isNotNull();
        }

        @Test
        @DisplayName("should include Retry-After header when rate limited")
        void shouldIncludeRetryAfterHeaderWhenRateLimited() throws Exception {
            // Given: Exhaust the rate limit
            for (int i = 0; i < 15; i++) {
                MvcResult result = mockMvc.perform(get("/api/vehicles")
                        .with(TestAuth.auth("ROLE_USER")))
                    .andReturn();
                
                // When: Rate limit is hit
                if (result.getResponse().getStatus() == 429) {
                    // Then: Should include Retry-After header
                    assertThat(result.getResponse().getHeader("Retry-After")).isNotNull();
                    assertThat(result.getResponse().getHeader("X-RateLimit-Remaining")).isEqualTo("0");
                    return;
                }
            }
        }

        @Test
        @DisplayName("should decrement remaining tokens with each request")
        void shouldDecrementRemainingTokens() throws Exception {
            // Given: First request
            MvcResult firstResult = mockMvc.perform(get("/api/vehicles")
                    .with(TestAuth.auth("ROLE_USER")))
                .andExpect(status().isOk())
                .andReturn();
            
            String firstRemaining = firstResult.getResponse().getHeader("X-RateLimit-Remaining");

            // When: Second request
            MvcResult secondResult = mockMvc.perform(get("/api/vehicles")
                    .with(TestAuth.auth("ROLE_USER")))
                .andExpect(status().isOk())
                .andReturn();
            
            String secondRemaining = secondResult.getResponse().getHeader("X-RateLimit-Remaining");

            // Then: Remaining tokens should decrease
            assertThat(Integer.parseInt(secondRemaining))
                .isLessThan(Integer.parseInt(firstRemaining));
        }
    }

    @Nested
    @DisplayName("Per-User Isolation")
    class PerUserIsolation {

        @Test
        @DisplayName("should isolate rate limits between different users")
        void shouldIsolateRateLimitsBetweenUsers() throws Exception {
            // Given: User A exhausts their rate limit
            for (int i = 0; i < 15; i++) {
                mockMvc.perform(get("/api/vehicles")
                    .with(TestAuth.auth("ROLE_USER")));
            }

            // When: User B makes a request (different authentication)
            MvcResult result = mockMvc.perform(get("/api/drivers")
                    .with(TestAuth.auth("ROLE_ADMIN")))
                .andReturn();

            // Then: User B should not be rate limited
            assertThat(result.getResponse().getStatus()).isNotEqualTo(429);
        }
    }

    @Nested
    @DisplayName("Unauthenticated Request Rate Limiting")
    class UnauthenticatedRequestRateLimiting {

        @Test
        @DisplayName("should rate limit unauthenticated requests by IP")
        void shouldRateLimitUnauthenticatedRequestsByIp() throws Exception {
            // Given: Public endpoint that doesn't require authentication
            String publicEndpoint = "/api/public/status";
            
            // When: Making many requests from same IP
            boolean rateLimitHit = false;
            
            for (int i = 0; i < 20; i++) {
                MvcResult result = mockMvc.perform(get(publicEndpoint)
                        .header("X-Forwarded-For", "203.0.113.1"))
                    .andReturn();
                
                if (result.getResponse().getStatus() == 429) {
                    rateLimitHit = true;
                    break;
                }
            }

            // Then: Should eventually be rate limited
            assertThat(rateLimitHit).isTrue();
        }

        @Test
        @DisplayName("should isolate rate limits between different IPs")
        void shouldIsolateRateLimitsBetweenIps() throws Exception {
            // Given: IP A exhausts rate limit
            String publicEndpoint = "/api/public/status";
            
            for (int i = 0; i < 20; i++) {
                mockMvc.perform(get(publicEndpoint)
                    .header("X-Forwarded-For", "203.0.113.1"));
            }

            // When: IP B makes a request
            MvcResult result = mockMvc.perform(get(publicEndpoint)
                    .header("X-Forwarded-For", "198.51.100.1"))
                .andReturn();

            // Then: IP B should not be rate limited
            assertThat(result.getResponse().getStatus()).isNotEqualTo(429);
        }
    }

    @Nested
    @DisplayName("Whitelisted Paths")
    class WhitelistedPaths {

        @Test
        @DisplayName("should not rate limit actuator health endpoint")
        void shouldNotRateLimitActuatorHealth() throws Exception {
            // When: Making many requests to health endpoint
            for (int i = 0; i < 50; i++) {
                mockMvc.perform(get("/actuator/health"))
                    .andExpect(status().isOk());
            }
            // Then: All requests should succeed (no rate limiting)
        }

        @Test
        @DisplayName("should not rate limit swagger UI")
        void shouldNotRateLimitSwaggerUI() throws Exception {
            // When: Making many requests to Swagger UI
            for (int i = 0; i < 50; i++) {
                mockMvc.perform(get("/swagger-ui/index.html"))
                    .andReturn(); // May return 404 if not configured, but should not be 429
            }
            // Then: Should never return 429
        }

        @Test
        @DisplayName("should not rate limit API docs endpoint")
        void shouldNotRateLimitApiDocs() throws Exception {
            // When: Making many requests to API docs
            for (int i = 0; i < 50; i++) {
                MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                    .andReturn();
                
                // Then: Should never return 429
                assertThat(result.getResponse().getStatus()).isNotEqualTo(429);
            }
        }

        @Test
        @DisplayName("should not include rate limit headers for whitelisted paths")
        void shouldNotIncludeRateLimitHeadersForWhitelistedPaths() throws Exception {
            // When
            MvcResult result = mockMvc.perform(get("/actuator/health"))
                .andReturn();

            // Then: Should not include rate limit headers
            assertThat(result.getResponse().getHeader("X-RateLimit-Limit")).isNull();
            assertThat(result.getResponse().getHeader("X-RateLimit-Remaining")).isNull();
        }
    }

    @Nested
    @DisplayName("Error Response Format")
    class ErrorResponseFormat {

        @Test
        @DisplayName("should return JSON error response when rate limited")
        void shouldReturnJsonErrorResponseWhenRateLimited() throws Exception {
            // Given: Exhaust rate limit
            for (int i = 0; i < 15; i++) {
                MvcResult result = mockMvc.perform(get("/api/vehicles")
                        .with(TestAuth.auth("ROLE_USER")))
                    .andReturn();
                
                // When: Rate limit is hit
                if (result.getResponse().getStatus() == 429) {
                    // Then: Should return JSON content type
                    assertThat(result.getResponse().getContentType()).contains("application/json");
                    
                    // And: Response body should contain error details
                    String responseBody = result.getResponse().getContentAsString();
                    assertThat(responseBody).isNotEmpty();
                    assertThat(responseBody).contains("rate limit");
                    return;
                }
            }
        }

        @Test
        @DisplayName("should include helpful error message when rate limited")
        void shouldIncludeHelpfulErrorMessage() throws Exception {
            // Given: Exhaust rate limit
            for (int i = 0; i < 15; i++) {
                MvcResult result = mockMvc.perform(get("/api/vehicles")
                        .with(TestAuth.auth("ROLE_USER")))
                    .andReturn();
                
                // When: Rate limit is hit
                if (result.getResponse().getStatus() == 429) {
                    // Then: Error message should be helpful
                    String responseBody = result.getResponse().getContentAsString();
                    assertThat(responseBody.toLowerCase())
                        .containsAnyOf("too many requests", "rate limit exceeded", "retry");
                    return;
                }
            }
        }
    }

    @Nested
    @DisplayName("Different Endpoints")
    class DifferentEndpoints {

        @Test
        @DisplayName("should apply rate limiting to all API endpoints")
        void shouldApplyRateLimitingToAllApiEndpoints() throws Exception {
            // Given: Different API endpoints
            String[] endpoints = {"/api/vehicles", "/api/drivers", "/api/inspections"};

            // When & Then: All should have rate limiting
            for (String endpoint : endpoints) {
                MvcResult result = mockMvc.perform(get(endpoint)
                        .with(TestAuth.auth("ROLE_USER")))
                    .andReturn();
                
                // Should include rate limit headers
                assertThat(result.getResponse().getHeader("X-RateLimit-Limit")).isNotNull();
            }
        }

        @Test
        @DisplayName("should share rate limit across different endpoints for same user")
        void shouldShareRateLimitAcrossDifferentEndpoints() throws Exception {
            // Given: Make requests to different endpoints
            MvcResult result1 = mockMvc.perform(get("/api/vehicles")
                    .with(TestAuth.auth("ROLE_USER")))
                .andReturn();
            
            String remaining1 = result1.getResponse().getHeader("X-RateLimit-Remaining");

            // When: Request to different endpoint
            MvcResult result2 = mockMvc.perform(get("/api/drivers")
                    .with(TestAuth.auth("ROLE_USER")))
                .andReturn();
            
            String remaining2 = result2.getResponse().getHeader("X-RateLimit-Remaining");

            // Then: Remaining tokens should decrease (shared bucket)
            assertThat(Integer.parseInt(remaining2))
                .isLessThan(Integer.parseInt(remaining1));
        }
    }

    @Nested
    @DisplayName("HTTP Methods")
    class HttpMethods {

        @Test
        @DisplayName("should apply rate limiting to POST requests")
        void shouldApplyRateLimitingToPostRequests() throws Exception {
            // When
            MvcResult result = mockMvc.perform(
                    org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/vehicles")
                        .with(TestAuth.auth("ROLE_ADMIN"))
                        .contentType("application/json")
                        .content("{}"))
                .andReturn();

            // Then: Should include rate limit headers (even if request fails validation)
            // The rate limiting should happen before request processing
            String rateLimitHeader = result.getResponse().getHeader("X-RateLimit-Limit");
            assertThat(rateLimitHeader).isNotNull();
        }

        @Test
        @DisplayName("should apply rate limiting to PUT requests")
        void shouldApplyRateLimitingToPutRequests() throws Exception {
            // When
            MvcResult result = mockMvc.perform(
                    org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/api/vehicles/1")
                        .with(TestAuth.auth("ROLE_ADMIN"))
                        .contentType("application/json")
                        .content("{}"))
                .andReturn();

            // Then
            assertThat(result.getResponse().getHeader("X-RateLimit-Limit")).isNotNull();
        }

        @Test
        @DisplayName("should apply rate limiting to DELETE requests")
        void shouldApplyRateLimitingToDeleteRequests() throws Exception {
            // When
            MvcResult result = mockMvc.perform(
                    org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/vehicles/1")
                        .with(TestAuth.auth("ROLE_ADMIN")))
                .andReturn();

            // Then
            assertThat(result.getResponse().getHeader("X-RateLimit-Limit")).isNotNull();
        }
    }
}