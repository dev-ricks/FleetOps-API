package com.fleetops.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * TDD test for SEC-P1-01: Expect Strict-Transport-Security header when enabled under prod profile.
 * Will initially fail until HSTS configuration is implemented.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("prod")
@TestPropertySource(
        locations = "classpath:application-test.properties",
        properties = {
                "security.hsts.enabled=true",
                "security.hsts.max-age=15552000",
                // assuming not including subdomains initially; adjust once decision finalized
                "security.hsts.include-subdomains=false",
                "security.hsts.preload=false",
                "spring.datasource.url=jdbc:h2:mem:fleetops_prod;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.show-sql=false",
                "spring.liquibase.enabled=false",
                "spring.sql.init.mode=never"
        }
)
class HstsHeaderProdProfileTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("HSTS header present with expected max-age under prod profile when enabled over HTTPS")
    void hstsHeaderPresentInProd() throws Exception {
        mockMvc.perform(get("/v3/api-docs").secure(true))
                .andExpect(status().isOk())
                .andExpect(header().string("Strict-Transport-Security", "max-age=15552000"));
    }

    @Test
    @DisplayName("HSTS header appears only once over HTTPS")
    void hstsHeaderAppearsOnlyOnce() throws Exception {
        var result = mockMvc.perform(get("/v3/api-docs").secure(true))
                .andExpect(status().isOk())
                .andReturn();
        long count = result.getResponse().getHeaderNames().stream()
                .filter(h -> h.equalsIgnoreCase("Strict-Transport-Security"))
                .count();
        if (count != 1) {
            throw new AssertionError("Expected exactly one Strict-Transport-Security header but found " + count);
        }
    }
}
