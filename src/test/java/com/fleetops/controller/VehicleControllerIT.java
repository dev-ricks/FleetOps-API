package com.fleetops.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetops.dto.VehicleRequest;
import com.fleetops.entity.Vehicle;
import com.fleetops.repository.VehicleRepository;
import com.fleetops.test.TestAuth;
import org.junit.jupiter.api.*;
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
class VehicleControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VehicleRepository vehicleRepository;

    @AfterEach
    void cleanup() {
        vehicleRepository.deleteAll();
    }

    @Nested
    @DisplayName("POST /api/vehicles")
    class Create {
        @Test
        @DisplayName("201 Created + Location and persists entity")
        void createsVehicle() throws Exception {
            VehicleRequest req = new VehicleRequest();
            req.setLicensePlate("NEW-123");
            req.setMake("Tesla");
            req.setModel("3");

            String response = mockMvc.perform(post("/api/vehicles")
                            .with(TestAuth.auth())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", matchesPattern(".*/api/vehicles/\\d+")))
                    .andExpect(header().string("Content-Type", containsString(MediaType.APPLICATION_JSON_VALUE)))
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.licensePlate").value("NEW-123"))
                    .andReturn().getResponse().getContentAsString();

            com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(response);
            Long id = node.get("id").asLong();
            Vehicle saved = vehicleRepository.findById(id).orElse(null);
            assertThat(saved).isNotNull();
            assertThat(saved.getLicensePlate()).isEqualTo("NEW-123");
        }
    }

    @Nested
    @DisplayName("GET /api/vehicles/{id}")
    class GetById {
        @Test
        @DisplayName("returns DTO for existing vehicle")
        void returnsDto() throws Exception {
            Vehicle v = vehicleRepository.save(Vehicle.builder().licensePlate("ABC").make("Toyota").model("Corolla").build());

            mockMvc.perform(get("/api/vehicles/" + v.getId())
                    .with(TestAuth.auth()))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", containsString(MediaType.APPLICATION_JSON_VALUE)))
                    .andExpect(jsonPath("$.id").value(v.getId()))
                    .andExpect(jsonPath("$.licensePlate").value("ABC"))
                    .andExpect(jsonPath("$.make").value("Toyota"))
                    .andExpect(jsonPath("$.model").value("Corolla"));
        }
    }

    @Nested
    @DisplayName("GET /api/vehicles/list")
    class ListAll {
        @Test
        @DisplayName("returns array of vehicles")
        void returnsArray() throws Exception {
            vehicleRepository.save(Vehicle.builder().licensePlate("A").make("M1").model("X1").build());
            vehicleRepository.save(Vehicle.builder().licensePlate("B").make("M2").model("X2").build());

            mockMvc.perform(get("/api/vehicles/list")
                    .with(TestAuth.auth()))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", containsString(MediaType.APPLICATION_JSON_VALUE)))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].licensePlate").value("A"))
                    .andExpect(jsonPath("$[1].licensePlate").value("B"));
        }
    }

    @Nested
    @DisplayName("PUT /api/vehicles/{id}")
    class Update {

        @Test
        @DisplayName("updates existing vehicle and returns DTO")
        void updatesVehicle() throws Exception {
            Vehicle existing = vehicleRepository.save(Vehicle.builder().licensePlate("X").make("Old").model("M").build());
            VehicleRequest patch = new VehicleRequest();
            patch.setMake("New");

            mockMvc.perform(put("/api/vehicles/" + existing.getId())
                            .with(TestAuth.auth())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(patch)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(existing.getId()))
                    .andExpect(jsonPath("$.make").value("New"));

            Vehicle updated = vehicleRepository.findById(existing.getId()).orElseThrow();
            assertThat(updated.getMake()).isEqualTo("New");
        }
    }

    @Nested
    @DisplayName("DELETE /api/vehicles/{id}")
    class Delete {

        @Test
        @DisplayName("returns 204 and removes entity")
        void deletesVehicle() throws Exception {
            Vehicle existing = vehicleRepository.save(Vehicle.builder().licensePlate("Z").make("Make").model("Model").build());

            mockMvc.perform(delete("/api/vehicles/" + existing.getId())
                    .with(TestAuth.auth()))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            assertThat(vehicleRepository.findById(existing.getId())).isEmpty();
        }
    }

    @Nested
    @DisplayName("Errors")
    class Errors {

        @Test
        @DisplayName("GET /api/vehicles/{id} not found -> 404 JSON error body")
        void getById_NotFound() throws Exception {
            mockMvc.perform(get("/api/vehicles/999999")
                    .with(TestAuth.auth()))
                     .andExpect(status().isNotFound())
                     .andExpect(jsonPath("$.status").value(404))
                     .andExpect(jsonPath("$.error").value("Not Found"));
        }

        @Test
        @DisplayName("POST /api/vehicles validation error -> 400")
        void create_BadRequest() throws Exception {
            VehicleRequest req = new VehicleRequest();

            mockMvc.perform(post("/api/vehicles")
                            .with(TestAuth.auth())
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(objectMapper.writeValueAsString(req)))
                     .andExpect(status().isBadRequest())
                     .andExpect(header().string("Content-Type", containsString(MediaType.APPLICATION_JSON_VALUE)))
                     .andExpect(jsonPath("$.status").value(400))
                     .andExpect(jsonPath("$.error").value("Bad Request"));
        }

        @Test
        @DisplayName("POST /api/vehicles duplicate licensePlate -> 409 Conflict")
        void create_Conflict_DuplicateLicensePlate() throws Exception {
            vehicleRepository.save(Vehicle.builder().licensePlate("DUP-123").make("M").model("X").build());

            VehicleRequest req = new VehicleRequest();
            req.setLicensePlate("DUP-123");
            req.setMake("Tesla");
            req.setModel("3");

            mockMvc.perform(post("/api/vehicles")
                            .with(TestAuth.auth())
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(objectMapper.writeValueAsString(req)))
                     .andExpect(status().isConflict())
                     .andExpect(header().string("Content-Type", containsString(MediaType.APPLICATION_JSON_VALUE)))
                     .andExpect(jsonPath("$.status").value(409))
                     .andExpect(jsonPath("$.error").value("Conflict"))
                     .andExpect(jsonPath("$.message", not(emptyOrNullString())));
        }

        @Test
        @DisplayName("PUT /api/vehicles/{id} duplicate licensePlate -> 409 Conflict")
        void update_Conflict_DuplicateLicensePlate() throws Exception {
            Vehicle existing = vehicleRepository.save(Vehicle.builder().licensePlate("A-1").make("M").model("X").build());
            vehicleRepository.save(Vehicle.builder().licensePlate("DUP-PLT").make("N").model("Y").build());

            VehicleRequest patch = new VehicleRequest();
            patch.setLicensePlate("DUP-PLT");

            mockMvc.perform(put("/api/vehicles/" + existing.getId())
                            .with(TestAuth.auth())
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(objectMapper.writeValueAsString(patch)))
                     .andExpect(status().isConflict())
                     .andExpect(header().string("Content-Type", containsString(MediaType.APPLICATION_JSON_VALUE)))
                     .andExpect(jsonPath("$.status").value(409))
                     .andExpect(jsonPath("$.error").value("Conflict"))
                     .andExpect(jsonPath("$.message", not(emptyOrNullString())));
        }
    }
}
