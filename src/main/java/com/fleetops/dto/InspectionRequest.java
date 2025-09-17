package com.fleetops.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * Request model for creating an inspection.
 * <p>
 * Requires an {@link #inspectionDate}, a {@link #status} string, and a {@link #vehicleId} reference.
 */
@Data
public class InspectionRequest {
    @NotNull
    private LocalDate inspectionDate;

    @NotBlank
    private String status;

    @NotNull
    private Long vehicleId;
}
