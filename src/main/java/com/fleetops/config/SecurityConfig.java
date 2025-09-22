package com.fleetops.config;

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

    /**
     * Define the HTTP security filter chain for the application.
     *
     * @param http the {@link HttpSecurity} builder
     * @return the configured {@link SecurityFilterChain}
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // TODO: remove /actuator/** and /api/** pass throughs to skip security for development
                        // Allow unauthenticated access to basic actuator health (no details due to app config)
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info", "/actuator/**").hasRole("ADMIN")
                        .requestMatchers("/api/inspections/**", "/api/vehicles/**", "/api/drivers/**").hasAnyRole("ADMIN","USER")
                        .requestMatchers("/api/public/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().denyAll())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(
                        Customizer.withDefaults()));
        return http.build();
    }
}
