package com.fleetops.test;

import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

public final class TestAuth {
    private TestAuth() {}

    /**
     * Default auth with ROLE_USER
     */
    public static RequestPostProcessor auth() {
        return jwt().authorities(() -> "ROLE_USER");
    }

    /**
     * Auth with a single role string like "ROLE_USER" or "ROLE_RESTRICTED"
     */
    public static RequestPostProcessor auth(String role) {
        return jwt().authorities(() -> role);
    }
}

