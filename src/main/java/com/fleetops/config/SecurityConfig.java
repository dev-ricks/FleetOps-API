package com.fleetops.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // TODO: remove /actuator/** and /api/** pass throughs to skip security for development
                        // Allow unauthenticated access to basic actuator health (no details due to app config)
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info", "/actuator/**").permitAll()
                        .requestMatchers("/api/public/**", "/swagger-ui/**", "/v3/api-docs/**", "/api/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(
                        Customizer.withDefaults()));
        return http.build();
    }
}
