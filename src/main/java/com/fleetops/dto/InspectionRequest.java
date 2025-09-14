package com.fleetops.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class InspectionRequest {
    @NotNull
    private LocalDate inspectionDate;

    @NotBlank
    private String status;

    @NotNull
    private Long vehicleId;
}
