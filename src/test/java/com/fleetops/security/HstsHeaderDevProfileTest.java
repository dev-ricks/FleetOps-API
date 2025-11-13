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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * SEC-P1-01: Under non-prod (dev) profile HSTS should NOT be sent (TDD failing initially until implemented correctly).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:fleetops_dev;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "spring.liquibase.enabled=false",
        "spring.sql.init.mode=never"
})
class HstsHeaderDevProfileTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("No HSTS header under dev profile for HTTP request")
    void noHstsHeaderInDev() throws Exception {
        mockMvc.perform(get("/v3/api-docs")) // insecure by default
                .andExpect(status().isOk())
                .andExpect(result -> {
                    if (result.getResponse().getHeader("Strict-Transport-Security") != null) {
                        throw new AssertionError("Did not expect Strict-Transport-Security header in dev profile over HTTP");
                    }
                });
    }
}
