package com.fleetops.controller;

import com.fleetops.exception.LicensePlateAlreadyExistsException;
import com.fleetops.exception.NotFoundExceptionBase;
import com.fleetops.exception.ServiceException;
import jakarta.validation.Valid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalControllerExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ThrowingController())
                .setControllerAdvice(new GlobalControllerExceptionHandler())
                .build();
    }

    @Test
    void notFound_IsMappedTo404Json() throws Exception {
        mockMvc.perform(get("/throw/notfound"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message", containsString("missing")))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").exists());
    }

    @Test
    void conflict_IsMappedTo409Json() throws Exception {
        mockMvc.perform(get("/throw/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").exists());
    }

    @Test
    void runtime_IsMappedTo500Json() throws Exception {
        mockMvc.perform(get("/throw/runtime"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").exists());
    }

    @Test
    void methodArgumentNotValid_IsMappedTo400Json() throws Exception {
        // name is required in DTO below; send empty name to trigger MethodArgumentNotValidException
        String json = "{\"name\":\"\"}";
        mockMvc.perform(post("/throw/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.fieldErrors").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").exists());
    }

    @Test
    void constraintViolation_IsMappedTo400Json() throws Exception {
        mockMvc.perform(get("/throw/constraint/-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Constraint Violation"))
                .andExpect(jsonPath("$.violations").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").exists());
    }

    @Test
    void malformedJson_IsMappedTo400Json() throws Exception {
        String badJson = "{ this is not json }";
        mockMvc.perform(post("/throw/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").exists());
    }

    @Test
    void serviceException_IsMappedTo500Json() throws Exception {
        mockMvc.perform(get("/throw/service"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").exists());
    }

    @Test
    void dataIntegrityViolation_IsMappedTo409Json() throws Exception {
        mockMvc.perform(get("/throw/dataintegrity"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").exists());
    }

    // Minimal controller solely for triggering exceptions handled by the advice
    @RestController
    @Validated
    private static class ThrowingController {
        @GetMapping("/throw/notfound")
        public String notFound() { throw new NotFoundExceptionBase("Resource missing") {}; }

        @GetMapping("/throw/conflict")
        public String conflict() { throw new LicensePlateAlreadyExistsException("exists"); }

        @GetMapping("/throw/runtime")
        public String runtime() { throw new RuntimeException("boom"); }

        @PostMapping(value = "/throw/validate", consumes = MediaType.APPLICATION_JSON_VALUE)
        public String validate(@Valid @RequestBody SampleDto dto) { return "ok"; }

        @GetMapping("/throw/constraint/{id}")
        public String constraint(@PathVariable long id) {
            throw new jakarta.validation.ConstraintViolationException("invalid", java.util.Collections.emptySet());
        }

        @GetMapping("/throw/service")
        public String service() { throw new ServiceException("Service failed", new RuntimeException("Database error")); }

        @GetMapping("/throw/dataintegrity")
        public String dataIntegrity() { throw new DataIntegrityViolationException("Data constraint violated"); }
    }

    private static class SampleDto {
        @jakarta.validation.constraints.NotBlank
        public String name;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}
