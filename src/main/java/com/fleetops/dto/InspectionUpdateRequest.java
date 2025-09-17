package com.fleetops.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * Partial update payload for an existing inspection.
 * <p>
 * All fields are optional; only non-null values are applied by the service layer.
 */
@Data
public class InspectionUpdateRequest {
    private LocalDate inspectionDate;
    private String status;
    private Long vehicleId;
}
