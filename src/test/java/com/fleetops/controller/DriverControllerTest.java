package com.fleetops.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetops.controller.support.ControllerTestConfig;
import com.fleetops.dto.DriverRequest;
import com.fleetops.entity.Driver;
import com.fleetops.service.DriverService;
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

@WebMvcTest(controllers = DriverController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalControllerExceptionHandler.class, ControllerTestConfig.class})
class DriverControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DriverService driverService;

    @Test
    @DisplayName("GET /api/drivers/status returns running message")
    void status() throws Exception {
        mockMvc.perform(get("/api/drivers/status"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(header().string("Content-Type", containsString("text")))
                .andExpect(content().string(containsString("running")));
    }

    @Test
    @DisplayName("GET /api/drivers/{id} returns a driver")
    void getById() throws Exception {
        Driver d = Driver.builder().id(1L).name("John Doe").licenseNumber("LIC123").build();
        given(driverService.getById(1L)).willReturn(d);

        mockMvc.perform(get("/api/drivers/1"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(header().string("Content-Type", containsString(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.licenseNumber").value("LIC123"));

        verify(driverService).getById(1L);
    }

    @Test
    @DisplayName("GET /api/drivers/list returns list of drivers")
    void list() throws Exception {
        List<Driver> drivers = List.of(
                Driver.builder().id(1L).name("A").licenseNumber("L1").build(),
                Driver.builder().id(2L).name("B").licenseNumber("L2").build()
        );
        given(driverService.getAll()).willReturn(drivers);

        mockMvc.perform(get("/api/drivers/list"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(header().string("Content-Type", containsString(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(driverService).getAll();
    }

    @Test
    @DisplayName("POST /api/drivers creates a driver")
    void create() throws Exception {
        DriverRequest payload = new DriverRequest();
        payload.setName("New");
        payload.setLicenseNumber("NL");
        Driver saved = Driver.builder().id(Long.valueOf(10L)).name("New").licenseNumber("NL").build();
        given(driverService.create(any(Driver.class))).willReturn(saved);

        mockMvc.perform(post("/api/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/drivers/10")))
                .andExpect(header().string("Content-Type", containsString(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("New"))
                .andExpect(jsonPath("$.licenseNumber").value("NL"));

        ArgumentCaptor<Driver> captor = ArgumentCaptor.forClass(Driver.class);
        verify(driverService).create(captor.capture());
        Driver sent = captor.getValue();
        org.assertj.core.api.Assertions.assertThat(sent.getId()).isNull();
        org.assertj.core.api.Assertions.assertThat(sent.getName()).isEqualTo("New");
    }

    @Test
    @DisplayName("PUT /api/drivers/{id} updates a driver")
    void update() throws Exception {
        DriverRequest patch = new DriverRequest();
        patch.setName("Updated");
        Driver updated = Driver.builder().id(5L).name("Updated").licenseNumber("X").build();
        given(driverService.update(eq(5L), any(Driver.class))).willReturn(updated);

        mockMvc.perform(put("/api/drivers/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(header().string("Content-Type", containsString(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Updated"));

        ArgumentCaptor<Driver> updateCaptor = ArgumentCaptor.forClass(Driver.class);
        verify(driverService).update(eq(5L), updateCaptor.capture());
        org.assertj.core.api.Assertions.assertThat(updateCaptor.getValue().getName()).isEqualTo("Updated");
    }

    @Test
    @DisplayName("DELETE /api/drivers/{id} deletes a driver")
    void deleteDriver() throws Exception {
        doNothing().when(driverService).delete(7L);

        mockMvc.perform(delete("/api/drivers/7"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isNoContent())
                .andExpect(content().string(""));

        verify(driverService).delete(7L);
    }
}
