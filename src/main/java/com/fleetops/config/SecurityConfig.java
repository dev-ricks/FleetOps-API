package com.fleetops.config;

import com.fleetops.security.HstsProperties;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration.
 * <p>
 * Configures the application as an OAuth2 Resource Server that validates JWT bearer tokens.
 * Permits unauthenticated access to basic Actuator endpoints for health/info and to public API/
 * documentation routes as currently configured.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    public static final String STRICT_TRANSPORT_SECURITY_HEADER = "Strict-Transport-Security";

    @Value("${security.oauth2.resourceserver.enabled:true}")
    private boolean oauth2Enabled;

    /**
     * Define the HTTP security filter chain for the application.
     *
     * @param http the {@link HttpSecurity} builder
     * @return the configured {@link SecurityFilterChain}
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, HstsProperties hstsProperties) throws Exception {
        final HstsProperties hsts = hstsProperties; // may be null if not bound
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info", "/actuator/**").hasRole("ADMIN")
                        .requestMatchers("/api/inspections/**", "/api/vehicles/**", "/api/drivers/**").hasAnyRole("ADMIN","USER")
                        .requestMatchers("/api/public/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().denyAll());
        if (oauth2Enabled) {
            http.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        }
        http.addFilterAfter((request, response, chain) -> {
            chain.doFilter(request, response);
            HttpServletResponse resp = (HttpServletResponse) response;
            boolean secure = request.isSecure();
            if (hsts != null && hsts.isEnabled() && secure) {
                StringBuilder value = new StringBuilder("max-age=" + hsts.getMaxAge());
                if (hsts.isIncludeSubdomains()) {
                    value.append("; includeSubDomains");
                }
                if (hsts.isPreload()) {
                    value.append("; preload");
                }
                resp.setHeader(SecurityConfig.STRICT_TRANSPORT_SECURITY_HEADER, value.toString());
            } else if (resp.getHeader(SecurityConfig.STRICT_TRANSPORT_SECURITY_HEADER) != null) {
                resp.setHeader(SecurityConfig.STRICT_TRANSPORT_SECURITY_HEADER, "");
                resp.setHeader(SecurityConfig.STRICT_TRANSPORT_SECURITY_HEADER, null);
            }
        }, org.springframework.security.web.access.intercept.AuthorizationFilter.class);
        return http.build();
    }
}
