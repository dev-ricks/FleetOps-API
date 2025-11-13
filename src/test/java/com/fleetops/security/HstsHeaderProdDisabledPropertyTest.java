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
 * SEC-P1-01: Even under prod profile, if the property is disabled, header must be absent.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("prod")
@TestPropertySource(properties = {
        "security.hsts.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:fleetops_prod_disabled;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "spring.liquibase.enabled=false",
        "spring.sql.init.mode=never"
})
class HstsHeaderProdDisabledPropertyTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("No HSTS header when disabled via property despite prod profile over HTTPS")
    void noHeaderWhenDisabled() {
        try {
            var result = mockMvc.perform(get("/v3/api-docs").secure(true))
                    .andExpect(status().isOk())
                    .andReturn();
            assertNull(result.getResponse().getHeader("Strict-Transport-Security"), "Header should be absent when security.hsts.enabled=false");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
