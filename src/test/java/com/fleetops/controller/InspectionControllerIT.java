package com.fleetops.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetops.dto.InspectionRequest;
import com.fleetops.entity.Inspection;
import com.fleetops.entity.Vehicle;
import com.fleetops.repository.InspectionRepository;
import com.fleetops.repository.VehicleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InspectionControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InspectionRepository inspectionRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @AfterEach
    void cleanup() {
        inspectionRepository.deleteAll();
        vehicleRepository.deleteAll();
    }

    @Nested
    @DisplayName("POST /api/inspections")
    class Create {
        @Test
        @DisplayName("201 Created + Location and persists entity")
        void createsInspection() throws Exception {
            Vehicle v = vehicleRepository.save(Vehicle.builder().licensePlate("ABC123").make("Toyota").model("Corolla").build());
            InspectionRequest req = new InspectionRequest();
            req.setInspectionDate(LocalDate.of(2025, 3, 3));
            req.setStatus("PENDING");
            req.setVehicleId(v.getId());

            String response = mockMvc.perform(post("/api/inspections")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", matchesPattern(".*/api/inspections/\\d+")))
                    .andExpect(header().string("Content-Type", containsString(MediaType.APPLICATION_JSON_VALUE)))
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.status").value("PENDING"))
                    .andReturn().getResponse().getContentAsString();

            com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(response);
            Long id = node.get("id").asLong();
            Inspection saved = inspectionRepository.findById(id).orElse(null);
            assertThat(saved).isNotNull();
            assertThat(saved.getStatus()).isEqualTo("PENDING");
            assertThat(saved.getVehicle().getId()).isEqualTo(v.getId());
        }
    }

    @Nested
    @DisplayName("GET /api/inspections/{id}")
    class GetById {
        @Test
        @DisplayName("returns DTO with ISO date and nested vehicle")
        void returnsDto() throws Exception {
            Vehicle v = vehicleRepository.save(Vehicle.builder().licensePlate("V1").make("M").model("X").build());
            Inspection i = inspectionRepository.save(Inspection.builder().inspectionDate(LocalDate.of(2024,1,1)).status("PASS").vehicle(v).build());

            mockMvc.perform(get("/api/inspections/" + i.getId()))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", containsString(MediaType.APPLICATION_JSON_VALUE)))
                    .andExpect(jsonPath("$.id").value(i.getId()))
                    .andExpect(jsonPath("$.inspectionDate").value("2024-01-01"))
                    .andExpect(jsonPath("$.status").value("PASS"))
                    .andExpect(jsonPath("$.vehicle.id").value(v.getId()));
        }
    }

    @Nested
    @DisplayName("PUT /api/inspections/{id}")
    class Update {
        @Test
        @DisplayName("updates fields and returns DTO")
        void updatesInspection() throws Exception {
            Vehicle v = vehicleRepository.save(Vehicle.builder().licensePlate("V1").make("M").model("X").build());
            Inspection i = inspectionRepository.save(Inspection.builder().inspectionDate(LocalDate.of(2024,1,1)).status("PENDING").vehicle(v).build());

            com.fasterxml.jackson.databind.node.ObjectNode patch = objectMapper.createObjectNode();
            patch.put("status", "DONE");

            mockMvc.perform(put("/api/inspections/" + i.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(patch.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(i.getId()))
                    .andExpect(jsonPath("$.status").value("DONE"));

            Inspection updated = inspectionRepository.findById(i.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo("DONE");
        }
    }

    @Nested
    @DisplayName("DELETE /api/inspections/{id}")
    class Delete {
        @Test
        @DisplayName("returns 204 and removes entity")
        void deletesInspection() throws Exception {
            Vehicle v = vehicleRepository.save(Vehicle.builder().licensePlate("V1").make("M").model("X").build());
            Inspection i = inspectionRepository.save(Inspection.builder().inspectionDate(LocalDate.now()).status("PENDING").vehicle(v).build());

            mockMvc.perform(delete("/api/inspections/" + i.getId()))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            assertThat(inspectionRepository.findById(i.getId())).isEmpty();
        }
    }

    @Nested
    @DisplayName("Errors")
    class Errors {
        @Test
        @DisplayName("GET /api/inspections/{id} not found -> 404 JSON error body")
        void getById_NotFound() throws Exception {
            mockMvc.perform(get("/api/inspections/999999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"));
        }

        @Test
        @DisplayName("POST /api/inspections validation error -> 400")
        void create_BadRequest() throws Exception {
            com.fasterxml.jackson.databind.node.ObjectNode bad = objectMapper.createObjectNode();
            // missing required fields
            mockMvc.perform(post("/api/inspections")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(bad.toString()))
                    .andExpect(status().isBadRequest())
                    .andExpect(header().string("Content-Type", containsString(MediaType.APPLICATION_JSON_VALUE)))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"));
        }
    }
}
