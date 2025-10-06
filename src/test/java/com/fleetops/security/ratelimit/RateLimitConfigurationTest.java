package com.fleetops.security.ratelimit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TDD tests for rate limiting configuration.
 * 
 * Verifies that:
 * - Rate limiting beans are properly configured
 * - Configuration properties are loaded correctly
 * - Filter is registered in the filter chain
 */
@SpringBootTest
@DisplayName("Rate Limit Configuration Tests")
class RateLimitConfigurationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("should have RateLimitService bean configured")
    void shouldHaveRateLimitServiceBean() {
        // When
        boolean hasBean = applicationContext.containsBean("rateLimitService");

        // Then
        assertThat(hasBean).isTrue();
        
        // And: Bean should be of correct type
        Object bean = applicationContext.getBean("rateLimitService");
        assertThat(bean).isInstanceOf(RateLimitService.class);
    }

    @Test
    @DisplayName("should have RateLimitFilter bean configured")
    void shouldHaveRateLimitFilterBean() {
        // When
        RateLimitFilter filter = applicationContext.getBean(RateLimitFilter.class);

        // Then
        assertThat(filter).isNotNull();
    }

    @Test
    @DisplayName("should load rate limit configuration properties")
    void shouldLoadRateLimitConfigurationProperties() {
        // When
        boolean hasBean = applicationContext.containsBean("rateLimitProperties");

        // Then: Configuration properties should be loaded
        assertThat(hasBean).isTrue();
    }

    @Test
    @DisplayName("should configure rate limit with sensible defaults")
    void shouldConfigureRateLimitWithSensibleDefaults() {
        // Given
        RateLimitService service = applicationContext.getBean(RateLimitService.class);

        // When: Make a request
        RateLimitResult result = service.tryConsume("test-key");

        // Then: Should have reasonable capacity (e.g., 10-100 requests)
        assertThat(result.getCapacity()).isGreaterThan(0);
        assertThat(result.getCapacity()).isLessThanOrEqualTo(1000);
    }
}