package com.fleetops.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetops.controller.support.ControllerTestConfig;
import com.fleetops.dto.*;
import com.fleetops.entity.Driver;
import com.fleetops.entity.Inspection;
import com.fleetops.exception.DriverNotFoundException;
import com.fleetops.service.DriverService;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
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

    @BeforeEach
    void setUp() {
        Mockito.reset(driverService);
    }

    @Nested
    @DisplayName("CRUD Operations")
    class CrudTests {
        @Test
        @DisplayName("GET /api/drivers/{id} returns a driver")
        void getById() throws Exception {
            Driver d = Driver.builder().id(1L).name("John Doe").licenseNumber("LIC123").build();
            given(driverService.getById(1L)).willReturn(d);
            mockMvc.perform(get("/api/drivers/1"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", containsString(MediaType.APPLICATION_JSON_VALUE)))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("John Doe"))
                    .andExpect(jsonPath("$.licenseNumber").value("LIC123"));
            verify(driverService).getById(1L);
        }

        @Test
        @DisplayName("GET /api/drivers/{id} not found maps to 404 JSON body")
        void getById_NotFound() throws Exception {
            given(driverService.getById(404L)).willThrow(new DriverNotFoundException("Driver not found"));
            mockMvc.perform(get("/api/drivers/404"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value(containsString("Driver not found")));
        }

        @Test
        @DisplayName("GET /api/drivers/list returns list of drivers")
        void list() throws Exception {
            List<Driver> drivers = List.of(
                    Driver.builder().id(NumberUtils.LONG_ONE).name("A").licenseNumber("L1").build(),
                    Driver.builder().id(2L).name("B").licenseNumber("L2").build()
            );
            given(driverService.getAll()).willReturn(drivers);
            mockMvc.perform(get("/api/drivers/list"))
                    .andExpect(status().isOk())
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
            Driver saved = Driver.builder().id(10L).name("New").licenseNumber("NL").build();
            given(driverService.create(any(Driver.class))).willReturn(saved);
            mockMvc.perform(post("/api/drivers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isCreated())
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
            DriverUpdateRequest patch = new DriverUpdateRequest();
            patch.setName("Updated");
            Driver updated = Driver.builder().id(5L).name("Updated").licenseNumber("X").build();
            given(driverService.update(eq(5L), any(Driver.class))).willReturn(updated);
            mockMvc.perform(put("/api/drivers/5")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(patch)))
                    .andExpect(status().isOk())
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
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));
            verify(driverService).delete(7L);
        }

        @Test
        @DisplayName("PUT /api/drivers/{id} returns 404 when driver not found")
        void update_NotFound() throws Exception {
            DriverRequest patch = new DriverRequest();
            patch.setName("Updated");
            given(driverService.update(eq(404L), any(Driver.class))).willThrow(new DriverNotFoundException("Driver not found"));
            mockMvc.perform(put("/api/drivers/404")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(patch)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value(containsString("Driver not found")));
        }

        @Test
        @DisplayName("PUT /api/drivers/{id} returns 400 when invalid payload")
        void update_InvalidPayload() throws Exception {
            String invalidJson = "{}";
            mockMvc.perform(put("/api/drivers/5")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("DELETE /api/drivers/{id} returns 404 when driver not found")
        void deleteDriver_NotFound() throws Exception {
            doNothing().when(driverService).delete(404L); // Simulate not found by throwing exception
            // Actually, should throw exception:
            org.mockito.Mockito.doThrow(new DriverNotFoundException("Driver not found")).when(driverService).delete(404L);
            mockMvc.perform(delete("/api/drivers/404"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value(containsString("Driver not found")));
        }
    }

    @Nested
    @DisplayName("Authentication & Authorization")
    class AuthTests {
        @Test
        @DisplayName("getDriver_shouldReturn401_whenNoJwtProvided")
        void getDriver_shouldReturn401_whenNoJwtProvided() throws Exception {
            // Arrange: No JWT token
            // Act: Perform GET request to /api/drivers/1
            mockMvc.perform(get("/api/drivers/1"))
                // Assert: Should return 401 Unauthorized
                .andExpect(status().isUnauthorized());
        }
        @Test
        @DisplayName("getDriver_shouldReturn403_whenJwtLacksRole")
        @org.springframework.security.test.context.support.WithMockUser(roles = "USER") // Assuming ADMIN is required
        void getDriver_shouldReturn403_whenJwtLacksRole() throws Exception {
            mockMvc.perform(get("/api/drivers/1"))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Input Validation")
    class InputValidationTests {
        @Test
        @DisplayName("createDriver_shouldReturn400_whenInvalidPayload")
        void createDriver_shouldReturn400_whenInvalidPayload() throws Exception {
            // Arrange: Invalid payload (missing required fields)
            String invalidJson = "{}";
            // Act: Perform POST request
            mockMvc.perform(post("/api/drivers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                // Assert: Should return 400 Bad Request
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandlingTests {
        @Test
        @DisplayName("getDriver_shouldReturn404_whenDriverNotFound")
        void getDriver_shouldReturn404_whenDriverNotFound() throws Exception {
            // Arrange: Mock service to throw not found
            given(driverService.getById(999L)).willThrow(new com.fleetops.exception.DriverNotFoundException("Not found"));
            // Act: Perform GET request
            mockMvc.perform(get("/api/drivers/999"))
                // Assert: Should return 404 Not Found
                .andExpect(status().isNotFound());
        }
    }
}
