package com.fleetops.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetops.controller.support.ControllerTestConfig;
import com.fleetops.entity.Vehicle;
import com.fleetops.exception.LicensePlateAlreadyExistsException;
import com.fleetops.exception.VehicleNotFoundException;
import com.fleetops.service.VehicleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = VehicleController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalControllerExceptionHandler.class, ControllerTestConfig.class})
class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VehicleService vehicleService;

    @Test
    @DisplayName("GET /api/vehicles/{id} returns a vehicle")
    void getById() throws Exception {
        Vehicle v = Vehicle.builder().id(1L).licensePlate("ABC123").make("Toyota").model("Corolla").build();
        given(vehicleService.getById(1L)).willReturn(v);

        mockMvc.perform(get("/api/vehicles/1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.licensePlate").value("ABC123"))
                .andExpect(jsonPath("$.make").value("Toyota"))
                .andExpect(jsonPath("$.model").value("Corolla"));

        verify(vehicleService).getById(1L);
    }

    @Test
    @DisplayName("GET /api/vehicles/{id} not found maps to 404 JSON body")
    void getById_NotFound() throws Exception {
        given(vehicleService.getById(99L)).willThrow(new VehicleNotFoundException("Vehicle not found"));

        mockMvc.perform(get("/api/vehicles/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(containsString("Vehicle not found")));
    }

    @Test
    @DisplayName("GET /api/vehicles/list returns list of vehicles")
    void list() throws Exception {
        List<Vehicle> vehicles = List.of(
                Vehicle.builder().id(1L).licensePlate("A").make("M1").model("X1").build(),
                Vehicle.builder().id(2L).licensePlate("B").make("M2").model("X2").build()
        );
        given(vehicleService.getAll()).willReturn(vehicles);

        mockMvc.perform(get("/api/vehicles/list"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(vehicleService).getAll();
    }

    @Test
    @DisplayName("POST /api/vehicles creates a vehicle")
    void create() throws Exception {
        Vehicle payload = Vehicle.builder().licensePlate("NEW1").make("Tesla").model("3").build();
        Vehicle saved = Vehicle.builder().id(10L).licensePlate("NEW1").make("Tesla").model("3").build();
        given(vehicleService.create(any(Vehicle.class))).willReturn(saved);

        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/vehicles/10")))
                .andExpect(header().string("Content-Type", containsString(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.licensePlate").value("NEW1"));

        ArgumentCaptor<Vehicle> createCaptor = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleService).create(createCaptor.capture());
        org.assertj.core.api.Assertions.assertThat(createCaptor.getValue().getId()).isNull();
    }

    @Test
    @DisplayName("POST /api/vehicles conflict on duplicate license plate -> 409 JSON body")
    void create_Conflict() throws Exception {
        Vehicle payload = Vehicle.builder().licensePlate("DUPL").make("Ford").model("F150").build();
        given(vehicleService.create(any(Vehicle.class)))
                .willThrow(new LicensePlateAlreadyExistsException("Vehicle with license plate DUPL already exists."));

        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(containsString("DUPL")));
    }

    @Test
    @DisplayName("PUT /api/vehicles/{id} updates a vehicle")
    void update() throws Exception {
        Vehicle patch = Vehicle.builder().make("Updated").build();
        Vehicle updated = Vehicle.builder().id(5L).licensePlate("X").make("Updated").model("Y").build();
        given(vehicleService.update(eq(5L), any(Vehicle.class))).willReturn(updated);

        mockMvc.perform(put("/api/vehicles/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.make").value("Updated"));

        ArgumentCaptor<Vehicle> updateCaptor = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleService).update(eq(5L), updateCaptor.capture());
        org.assertj.core.api.Assertions.assertThat(updateCaptor.getValue().getMake()).isEqualTo("Updated");
    }

    @Test
    @DisplayName("DELETE /api/vehicles/{id} deletes a vehicle")
    void deleteVehicle() throws Exception {
        doNothing().when(vehicleService).delete(7L);

        mockMvc.perform(delete("/api/vehicles/7"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(vehicleService).delete(7L);
    }
}
