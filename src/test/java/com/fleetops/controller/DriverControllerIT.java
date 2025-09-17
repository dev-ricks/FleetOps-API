package com.fleetops.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetops.dto.DriverRequest;
import com.fleetops.entity.Driver;
import com.fleetops.repository.DriverRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DriverControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DriverRepository driverRepository;

    @AfterEach
    void cleanup() {
        driverRepository.deleteAll();
    }

    @Nested
    @DisplayName("POST /api/drivers")
    class Create {
        @Test
        @DisplayName("201 Created + Location and persists entity")
        void createsDriver() throws Exception {
            DriverRequest req = new DriverRequest();
            req.setName("Jane Doe");
            req.setLicenseNumber("LIC-XYZ");

            String response = mockMvc.perform(post("/api/drivers").contentType(MediaType.APPLICATION_JSON)
                                                                  .content(objectMapper.writeValueAsString(req))).andExpect(
                                             org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isCreated())
                                     .andExpect(header().string("Location", matchesPattern(".*/api/drivers/\\d+")))
                                     .andExpect(header().string("Content-Type",
                                                                containsString(MediaType.APPLICATION_JSON_VALUE)))
                                     .andExpect(jsonPath("$.id", notNullValue()))
                                     .andExpect(jsonPath("$.name").value("Jane Doe"))
                                     .andExpect(jsonPath("$.licenseNumber").value("LIC-XYZ")).andReturn().getResponse()
                                     .getContentAsString();

            com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(response);
            Long id = node.get("id").asLong();
            Driver saved = driverRepository.findById(id).orElse(null);
            assertThat(saved).isNotNull();
            assertThat(saved.getName()).isEqualTo("Jane Doe");
        }
    }

    @Nested
    @DisplayName("GET /api/drivers/{id}")
    class GetById {
        @Test
        @DisplayName("returns DTO for existing driver")
        void returnsDto() throws Exception {
            Driver d = driverRepository.save(Driver.builder().name("A").licenseNumber("L1").build());

            mockMvc.perform(get("/api/drivers/" + d.getId()))
                   .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                   .andExpect(header().string("Content-Type", containsString(MediaType.APPLICATION_JSON_VALUE)))
                   .andExpect(jsonPath("$.id").value(d.getId()))
                   .andExpect(jsonPath("$.name").value("A"))
                   .andExpect(jsonPath("$.licenseNumber").value("L1"));
        }
    }

    @Nested
    @DisplayName("GET /api/drivers/list")
    class ListAll {
        @Test
        @DisplayName("returns array of drivers")
        void returnsArray() throws Exception {
            driverRepository.save(Driver.builder().name("A").licenseNumber("L1").build());
            driverRepository.save(Driver.builder().name("B").licenseNumber("L2").build());

            mockMvc.perform(get("/api/drivers/list"))
                   .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                   .andExpect(header().string("Content-Type", containsString(MediaType.APPLICATION_JSON_VALUE)))
                   .andExpect(jsonPath("$", hasSize(2)));
        }
    }

    @Nested
    @DisplayName("PUT /api/drivers/{id}")
    class Update {
        @Test
        @DisplayName("updates existing driver and returns DTO")
        void updatesDriver() throws Exception {
            Driver existing = driverRepository.save(Driver.builder().name("Old").licenseNumber("OLD").build());
            DriverRequest patch = new DriverRequest();
            patch.setName("New");

            mockMvc.perform(put("/api/drivers/" + existing.getId()).contentType(MediaType.APPLICATION_JSON)
                                                                   .content(objectMapper.writeValueAsString(patch)))
                   .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                   .andExpect(jsonPath("$.id").value(existing.getId()))
                   .andExpect(jsonPath("$.name").value("New"));

            Driver updated = driverRepository.findById(existing.getId()).orElseThrow();
            assertThat(updated.getName()).isEqualTo("New");
        }
    }

    @Nested
    @DisplayName("DELETE /api/drivers/{id}")
    class Delete {
        @Test
        @DisplayName("returns 204 and removes entity")
        void deletesDriver() throws Exception {
            Driver existing = driverRepository.save(Driver.builder().name("X").licenseNumber("Y").build());

            mockMvc.perform(delete("/api/drivers/" + existing.getId()))
                   .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isNoContent())
                   .andExpect(content().string(""));

            assertThat(driverRepository.findById(existing.getId())).isEmpty();
        }
    }

    @Nested
    @DisplayName("Errors")
    class Errors {
        @Test
        @DisplayName("GET /api/drivers/{id} not found -> 404 JSON error body")
        void getById_NotFound() throws Exception {
            mockMvc.perform(get("/api/drivers/999999"))
                   .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isNotFound())
                   .andExpect(jsonPath("$.status").value(404))
                   .andExpect(jsonPath("$.error").value("Not Found"));
        }

        @Test
        @DisplayName("POST /api/drivers validation error -> 400")
        void create_BadRequest() throws Exception {
            DriverRequest req = new DriverRequest();
            // missing required fields

            mockMvc.perform(post("/api/drivers").contentType(MediaType.APPLICATION_JSON)
                                                   .content(objectMapper.writeValueAsString(req)))
                   .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest())
                   .andExpect(header().string("Content-Type", containsString(MediaType.APPLICATION_JSON_VALUE)))
                   .andExpect(jsonPath("$.status").value(400))
                   .andExpect(jsonPath("$.error").value("Bad Request"));
        }
    }
}
