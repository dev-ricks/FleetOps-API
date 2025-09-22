package com.fleetops.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetops.controller.support.ControllerTestConfig;
import com.fleetops.dto.InspectionRequest;
import com.fleetops.dto.InspectionUpdateRequest;
import com.fleetops.entity.Inspection;
import com.fleetops.entity.Vehicle;
import com.fleetops.exception.InspectionNotFoundException;
import com.fleetops.service.InspectionService;
import com.fleetops.test.TestAuth;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = InspectionController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import({GlobalControllerExceptionHandler.class, ControllerTestConfig.class, com.fleetops.config.SecurityConfig.class})
class InspectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InspectionService inspectionService;

    @BeforeEach
    void setUp() {
        Mockito.reset(inspectionService);
    }

    @Nested
    @DisplayName("CRUD Operations")
    class CrudTests {

        @Test
        @DisplayName("GET /api/inspections/{id} returns an inspection")
        void getById() throws Exception {
            Vehicle v = Vehicle.builder().id(1L).licensePlate("ABC123").make("Toyota").model("Corolla").build();
            Inspection i = Inspection.builder().id(3L).inspectionDate(java.time.LocalDate.of(2024, 1, 1)).status("PASS")
                                     .vehicle(v).build();
            given(inspectionService.getById(3L)).willReturn(i);

            mockMvc.perform(get("/api/inspections/3").with(TestAuth.auth())).andExpect(status().isOk())
                   .andExpect(header().string("Content-Type", containsString(MediaType.APPLICATION_JSON_VALUE)))
                   .andExpect(jsonPath("$.id").value(3)).andExpect(jsonPath("$.inspectionDate").value("2024-01-01"))
                   .andExpect(jsonPath("$.status").value("PASS")).andExpect(jsonPath("$.vehicle.id").value(1));

            verify(inspectionService).getById(3L);
        }

        @Test
        @DisplayName("GET /api/inspections/{id} not found -> 404 JSON body")
        void getById_NotFound() throws Exception {
            given(inspectionService.getById(3L)).willThrow(new InspectionNotFoundException("Inspection not found"));

            mockMvc.perform(get("/api/inspections/3").with(TestAuth.auth())).andExpect(status().isNotFound())
                   .andExpect(jsonPath("$.status").value(404)).andExpect(jsonPath("$.error").value("Not Found"))
                   .andExpect(jsonPath("$.message").value(containsString("Inspection not found")));

            verify(inspectionService).getById(3L);
        }

        @Test
        @DisplayName("GET /api/inspections/list returns list of inspections")
        void list() throws Exception {
            List<Inspection> inspections = List.of(
                    Inspection.builder().id(1L).inspectionDate(LocalDate.of(2024, 1, 1)).status("PASS").build(),
                    Inspection.builder().id(2L).inspectionDate(LocalDate.of(2024, 2, 2)).status("FAIL").build());
            given(inspectionService.getAll()).willReturn(inspections);

            mockMvc.perform(get("/api/inspections/list").with(TestAuth.auth())).andExpect(status().isOk())
                   .andExpect(header().string("Content-Type", containsString(MediaType.APPLICATION_JSON_VALUE)))
                   .andExpect(jsonPath("$", hasSize(2))).andExpect(jsonPath("$[0].id").value(1))
                   .andExpect(jsonPath("$[1].id").value(2));

            verify(inspectionService).getAll();
        }

        @Test
        @DisplayName("POST /api/inspections creates an inspection")
        void create() throws Exception {
            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("inspectionDate", "2025-03-03");
            payload.put("status", "PENDING");
            payload.put("vehicleId", 1);
            Inspection saved = Inspection.builder().id(10L).inspectionDate(LocalDate.of(2025, 3, 3)).status("PENDING")
                                         .build();
            given(inspectionService.create(any(InspectionRequest.class))).willReturn(saved);

            mockMvc.perform(
                           post("/api/inspections").with(TestAuth.auth()).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                                                   .content(objectMapper.writeValueAsString(payload)))
                   .andExpect(status().isCreated())
                   .andExpect(header().string("Location", endsWith("/api/inspections/10")))
                   .andExpect(header().string("Content-Type", containsString(MediaType.APPLICATION_JSON_VALUE)))
                   .andExpect(jsonPath("$.id").value(10)).andExpect(jsonPath("$.status").value("PENDING"));

            ArgumentCaptor<InspectionRequest> createCaptor = ArgumentCaptor.forClass(InspectionRequest.class);
            verify(inspectionService).create(createCaptor.capture());
            InspectionRequest sent = createCaptor.getValue();
            org.assertj.core.api.Assertions.assertThat(sent.getVehicleId()).isEqualTo(1L);
            org.assertj.core.api.Assertions.assertThat(sent.getStatus()).isEqualTo("PENDING");
            org.assertj.core.api.Assertions.assertThat(sent.getInspectionDate()).isEqualTo(LocalDate.of(2025, 3, 3));
        }

        @Test
        @DisplayName("PUT /api/inspections/{id} updates an inspection")
        void update() throws Exception {
            InspectionUpdateRequest patch = new InspectionUpdateRequest();
            patch.setStatus("UPDATED");
            Inspection updated = Inspection.builder().id(5L).inspectionDate(LocalDate.of(2025, 1, 1)).status("UPDATED")
                                           .build();
            given(inspectionService.update(eq(5L), any(Inspection.class))).willReturn(updated);

            mockMvc.perform(
                           put("/api/inspections/5").with(TestAuth.auth()).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                                                    .content(objectMapper.writeValueAsString(patch)))
                   .andExpect(status().isOk())
                   .andExpect(header().string("Content-Type", containsString(MediaType.APPLICATION_JSON_VALUE)))
                   .andExpect(jsonPath("$.id").value(5)).andExpect(jsonPath("$.status").value("UPDATED"));
            ArgumentCaptor<Inspection> updateCaptor = ArgumentCaptor.forClass(Inspection.class);
            verify(inspectionService).update(eq(5L), updateCaptor.capture());
            org.assertj.core.api.Assertions.assertThat(updateCaptor.getValue().getStatus()).isEqualTo("UPDATED");
        }

        @Test
        @DisplayName("PUT /api/inspections/{id} returns 404 when inspection not found")
        void update_NotFound() throws Exception {
            InspectionUpdateRequest patch = new InspectionUpdateRequest();
            patch.setStatus("UPDATED");
            given(inspectionService.update(eq(404L), any(Inspection.class))).willThrow(
                    new InspectionNotFoundException("Inspection not found"));
            mockMvc.perform(put("/api/inspections/404").with(TestAuth.auth()).with(csrf())
                                                       .contentType(MediaType.APPLICATION_JSON)
                                                       .content(objectMapper.writeValueAsString(patch)))
                   .andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404))
                   .andExpect(jsonPath("$.error").value("Not Found"))
                   .andExpect(jsonPath("$.message").value(containsString("Inspection not found")));
        }

        @Test
        @DisplayName("PUT /api/inspections/{id} returns 400 when invalid payload")
        void update_InvalidPayload() throws Exception {
            String invalidJson = "{}";
            mockMvc.perform(
                    put("/api/inspections/5").with(TestAuth.auth()).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                                             .content(invalidJson)).andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("DELETE /api/inspections/{id} deletes an inspection")
        void deleteInspection() throws Exception {
            doNothing().when(inspectionService).delete(7L);
            mockMvc.perform(delete("/api/inspections/7").with(TestAuth.auth()).with(csrf()))
                   .andExpect(status().isNoContent()).andExpect(content().string(""));
            verify(inspectionService).delete(7L);
        }

        @Test
        @DisplayName("DELETE /api/inspections/{id} returns 404 when inspection not found")
        void deleteInspection_NotFound() throws Exception {
            org.mockito.Mockito.doThrow(new InspectionNotFoundException("Inspection not found")).when(inspectionService)
                               .delete(404L);
            mockMvc.perform(delete("/api/inspections/404").with(TestAuth.auth()).with(csrf()))
                   .andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404))
                   .andExpect(jsonPath("$.error").value("Not Found"))
                   .andExpect(jsonPath("$.message").value(containsString("Inspection not found")));
        }
    }

    @Nested
    @DisplayName("Authentication & Authorization")
    class AuthTests {
        @Test
        @DisplayName("getInspection_shouldReturn401_whenNoJwtProvided")
        void getInspection_shouldReturn401_whenNoJwtProvided() throws Exception {
            mockMvc.perform(get("/api/inspections/1")).andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("getInspection_shouldReturn403_whenJwtLacksRole")
        void getInspection_shouldReturn403_whenJwtLacksRole() throws Exception {
            // Arrange: make service return something if controller is executed
            given(inspectionService.getById(1L)).willReturn(Inspection.builder().id(1L).status("PASS").build());

            mockMvc.perform(get("/api/inspections/1").with(TestAuth.auth("ROLE_RESTRICTED")))
                   .andExpect(status().isForbidden());

            // ensure service wasn't called when access denied
            Mockito.verify(inspectionService, Mockito.never()).getById(1L);
        }

        @Test
        @DisplayName("getInspection_shouldReturn200_whenJwtHasUserRole")
        void getInspection_shouldReturn200_whenJwtHasUserRole() throws Exception {
            mockMvc.perform(get("/api/inspections/1").with(TestAuth.auth())).andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Input Validation")
    class InputValidationTests {
        @Test
        @DisplayName("createInspection_shouldReturn400_whenInvalidPayload")
        void createInspection_shouldReturn400_whenInvalidPayload() throws Exception {
            // Arrange: Invalid payload (missing required fields)
            String invalidJson = "{}";
            // Act: Perform POST request
            mockMvc.perform(post("/api/inspections").with(TestAuth.auth()).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(invalidJson))
                   // Assert: Should return 400 Bad Request
                   .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandlingTests {
        @Test
        @DisplayName("getInspection_shouldReturn404_whenInspectionNotFound")
        void getInspection_shouldReturn404_whenInspectionNotFound() throws Exception {
            // Arrange: Mock service to throw not found
            given(inspectionService.getById(999L)).willThrow(
                    new com.fleetops.exception.InspectionNotFoundException("Not found"));
            // Act: Perform GET request
            mockMvc.perform(get("/api/inspections/999").with(TestAuth.auth()))
                   // Assert: Should return 404 Not Found
                   .andExpect(status().isNotFound());
        }
    }
}
