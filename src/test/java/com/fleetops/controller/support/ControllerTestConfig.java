package com.fleetops.controller.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fleetops.service.*;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@TestConfiguration
public class ControllerTestConfig {

    @Bean
    @Primary
    public DriverService driverServiceMock() {
        return Mockito.mock(DriverService.class);
    }

    @Bean
    @Primary
    public VehicleService vehicleServiceMock() {
        return Mockito.mock(VehicleService.class);
    }

    @Bean
    @Primary
    public InspectionService inspectionServiceMock() {
        return Mockito.mock(InspectionService.class);
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        // Align date serialization with Spring Boot defaults: ISO-8601 strings (not arrays)
        return Jackson2ObjectMapperBuilder.json()
                                          .modules(new JavaTimeModule())
                                          .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                                          .build();
    }
}
