package com.fleetops.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * SEC-P1-01 boundary test: Even if HSTS is enabled in prod, it must only apply to secure (HTTPS) requests.
 * MockMvc insecure requests (.secure(false) / default) should not receive the header.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("prod")
@TestPropertySource(properties = {
        "security.hsts.enabled=true",
        "security.hsts.max-age=15552000",
        "spring.datasource.url=jdbc:h2:mem:fleetops_prod_insecure;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "spring.liquibase.enabled=false",
        "spring.sql.init.mode=never"
})
class HstsHeaderProdInsecureRequestTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("No HSTS header on insecure (HTTP) request even when enabled in prod")
    void noHeaderOnInsecureRequest() throws Exception { // ...existing code...
        var result = mockMvc.perform(get("/v3/api-docs")) // insecure by default
                .andExpect(status().isOk())
                .andReturn();
        assertNull(result.getResponse().getHeader("Strict-Transport-Security"), "Should not send HSTS for HTTP request");
    }
}
