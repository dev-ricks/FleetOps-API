package com.fleetops.security.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * TDD Unit tests for RateLimitFilter.
 * 
 * This filter should:
 * - Extract rate limit key from request (user ID, IP address, or API key)
 * - Check rate limit using RateLimitService
 * - Allow request if not rate limited
 * - Return 429 Too Many Requests if rate limited
 * - Add rate limit headers to response
 * - Skip rate limiting for whitelisted paths
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimitFilter Unit Tests")
class RateLimitFilterTest {

    @Mock
    private RateLimitService rateLimitService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private RateLimitFilter rateLimitFilter;

    @BeforeEach
    void setUp() {
        rateLimitFilter = new RateLimitFilter(rateLimitService);
        SecurityContextHolder.setContext(securityContext);
    }

    @Nested
    @DisplayName("Request Allowed")
    class RequestAllowed {

        @Test
        @DisplayName("should allow request when rate limit not exceeded")
        void shouldAllowRequestWhenNotRateLimited() throws Exception {
            // Given
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("user123");
            when(request.getRequestURI()).thenReturn("/api/vehicles");
            
            RateLimitResult allowedResult = new RateLimitResult(
                true,  // allowed
                9,     // remaining tokens
                10,    // capacity
                0,     // retry after
                0      // reset time
            );
            when(rateLimitService.tryConsume(anyString())).thenReturn(allowedResult);

            // When
            rateLimitFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain).doFilter(request, response);
            verify(response, never()).setStatus(429); // 429 Too Many Requests
        }

        @Test
        @DisplayName("should add rate limit headers to response when allowed")
        void shouldAddRateLimitHeadersWhenAllowed() throws Exception {
            // Given
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("user123");
            when(request.getRequestURI()).thenReturn("/api/vehicles");
            
            RateLimitResult allowedResult = new RateLimitResult(
                true,
                5,
                10,
                0,
                1705320000L
            );
            when(rateLimitService.tryConsume(anyString())).thenReturn(allowedResult);

            // When
            rateLimitFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(response).setHeader("X-RateLimit-Limit", "10");
            verify(response).setHeader("X-RateLimit-Remaining", "5");
            verify(response).setHeader("X-RateLimit-Reset", "1705320000");
        }
    }

    @Nested
    @DisplayName("Request Rate Limited")
    class RequestRateLimited {

        @Test
        @DisplayName("should return 429 when rate limit exceeded")
        void shouldReturn429WhenRateLimited() throws Exception {
            // Given
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("user123");
            when(request.getRequestURI()).thenReturn("/api/vehicles");
            
            RateLimitResult deniedResult = new RateLimitResult(
                false, // not allowed
                0,     // no remaining tokens
                10,
                60,    // retry after 60 seconds
                1705320060L
            );
            when(rateLimitService.tryConsume(anyString())).thenReturn(deniedResult);

            // When
            rateLimitFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(response).setStatus(429); // 429 Too Many Requests
            verify(filterChain, never()).doFilter(any(), any());
        }

        @Test
        @DisplayName("should add rate limit headers when rate limited")
        void shouldAddRateLimitHeadersWhenRateLimited() throws Exception {
            // Given
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("user123");
            when(request.getRequestURI()).thenReturn("/api/vehicles");
            
            RateLimitResult deniedResult = new RateLimitResult(
                false,
                0,
                10,
                60,
                1705320060L
            );
            when(rateLimitService.tryConsume(anyString())).thenReturn(deniedResult);

            // When
            rateLimitFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(response).setHeader("X-RateLimit-Limit", "10");
            verify(response).setHeader("X-RateLimit-Remaining", "0");
            verify(response).setHeader("X-RateLimit-Reset", "1705320060");
            verify(response).setHeader("Retry-After", "60");
        }

        @Test
        @DisplayName("should write error response body when rate limited")
        void shouldWriteErrorResponseBodyWhenRateLimited() throws Exception {
            // Given
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("user123");
            when(request.getRequestURI()).thenReturn("/api/vehicles");
            
            RateLimitResult deniedResult = new RateLimitResult(false, 0, 10, 60, 1705320060L);
            when(rateLimitService.tryConsume(anyString())).thenReturn(deniedResult);

            // When
            rateLimitFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(response).setContentType("application/json");
            verify(response).getWriter();
        }
    }

    @Nested
    @DisplayName("Rate Limit Key Extraction")
    class RateLimitKeyExtraction {

        @Test
        @DisplayName("should use authenticated user ID as rate limit key")
        void shouldUseAuthenticatedUserIdAsKey() throws Exception {
            // Given
            String userId = "user123";
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn(userId);
            when(request.getRequestURI()).thenReturn("/api/vehicles");
            
            RateLimitResult allowedResult = new RateLimitResult(true, 9, 10, 0, 0);
            when(rateLimitService.tryConsume(anyString())).thenReturn(allowedResult);

            // When
            rateLimitFilter.doFilterInternal(request, response, filterChain);

            // Then
            ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
            verify(rateLimitService).tryConsume(keyCaptor.capture());
            assertThat(keyCaptor.getValue()).contains(userId);
        }

        @Test
        @DisplayName("should use IP address as rate limit key for unauthenticated requests")
        void shouldUseIpAddressForUnauthenticatedRequests() throws Exception {
            // Given
            String ipAddress = "192.168.1.100";
            when(securityContext.getAuthentication()).thenReturn(null);
            when(request.getRemoteAddr()).thenReturn(ipAddress);
            when(request.getRequestURI()).thenReturn("/api/public/status");
            
            RateLimitResult allowedResult = new RateLimitResult(true, 9, 10, 0, 0);
            when(rateLimitService.tryConsume(anyString())).thenReturn(allowedResult);

            // When
            rateLimitFilter.doFilterInternal(request, response, filterChain);

            // Then
            ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
            verify(rateLimitService).tryConsume(keyCaptor.capture());
            assertThat(keyCaptor.getValue()).contains(ipAddress);
        }

        @Test
        @DisplayName("should use X-Forwarded-For header if present")
        void shouldUseXForwardedForHeader() throws Exception {
            // Given
            String forwardedIp = "203.0.113.1";
            when(securityContext.getAuthentication()).thenReturn(null);
            when(request.getHeader("X-Forwarded-For")).thenReturn(forwardedIp);
            when(request.getRequestURI()).thenReturn("/api/public/status");
            
            RateLimitResult allowedResult = new RateLimitResult(true, 9, 10, 0, 0);
            when(rateLimitService.tryConsume(anyString())).thenReturn(allowedResult);

            // When
            rateLimitFilter.doFilterInternal(request, response, filterChain);

            // Then
            ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
            verify(rateLimitService).tryConsume(keyCaptor.capture());
            assertThat(keyCaptor.getValue()).contains(forwardedIp);
        }

        @Test
        @DisplayName("should handle multiple IPs in X-Forwarded-For header")
        void shouldHandleMultipleIpsInXForwardedFor() throws Exception {
            // Given
            String forwardedIps = "203.0.113.1, 198.51.100.1, 192.0.2.1";
            when(securityContext.getAuthentication()).thenReturn(null);
            when(request.getHeader("X-Forwarded-For")).thenReturn(forwardedIps);
            when(request.getRequestURI()).thenReturn("/api/public/status");
            
            RateLimitResult allowedResult = new RateLimitResult(true, 9, 10, 0, 0);
            when(rateLimitService.tryConsume(anyString())).thenReturn(allowedResult);

            // When
            rateLimitFilter.doFilterInternal(request, response, filterChain);

            // Then
            ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
            verify(rateLimitService).tryConsume(keyCaptor.capture());
            // Should use the first IP (client IP)
            assertThat(keyCaptor.getValue()).contains("203.0.113.1");
        }
    }

    @Nested
    @DisplayName("Whitelisted Paths")
    class WhitelistedPaths {

        @Test
        @DisplayName("should skip rate limiting for actuator health endpoints")
        void shouldSkipRateLimitingForActuatorHealth() throws Exception {
            // Given
            when(request.getRequestURI()).thenReturn("/actuator/health");

            // When
            rateLimitFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(rateLimitService, never()).tryConsume(anyString());
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("should skip rate limiting for swagger UI")
        void shouldSkipRateLimitingForSwaggerUI() throws Exception {
            // Given
            when(request.getRequestURI()).thenReturn("/swagger-ui/index.html");

            // When
            rateLimitFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(rateLimitService, never()).tryConsume(anyString());
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("should skip rate limiting for API docs")
        void shouldSkipRateLimitingForApiDocs() throws Exception {
            // Given
            when(request.getRequestURI()).thenReturn("/v3/api-docs");

            // When
            rateLimitFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(rateLimitService, never()).tryConsume(anyString());
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("should apply rate limiting to non-whitelisted paths")
        void shouldApplyRateLimitingToNonWhitelistedPaths() throws Exception {
            // Given
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("user123");
            when(request.getRequestURI()).thenReturn("/api/vehicles");
            
            RateLimitResult allowedResult = new RateLimitResult(true, 9, 10, 0, 0);
            when(rateLimitService.tryConsume(anyString())).thenReturn(allowedResult);

            // When
            rateLimitFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(rateLimitService).tryConsume(anyString());
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        @DisplayName("should handle rate limit service exceptions gracefully")
        void shouldHandleRateLimitServiceExceptions() throws Exception {
            // Given
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("user123");
            when(request.getRequestURI()).thenReturn("/api/vehicles");
            when(rateLimitService.tryConsume(anyString()))
                .thenThrow(new RuntimeException("Rate limit service error"));

            // When
            rateLimitFilter.doFilterInternal(request, response, filterChain);

            // Then: Should allow request to proceed (fail open)
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("should handle missing authentication gracefully")
        void shouldHandleMissingAuthenticationGracefully() throws Exception {
            // Given
            when(securityContext.getAuthentication()).thenReturn(null);
            when(request.getRemoteAddr()).thenReturn("192.168.1.1");
            when(request.getRequestURI()).thenReturn("/api/vehicles");
            
            RateLimitResult allowedResult = new RateLimitResult(true, 9, 10, 0, 0);
            when(rateLimitService.tryConsume(anyString())).thenReturn(allowedResult);

            // When
            rateLimitFilter.doFilterInternal(request, response, filterChain);

            // Then: Should use IP address as fallback
            verify(rateLimitService).tryConsume(anyString());
            verify(filterChain).doFilter(request, response);
        }
    }
}