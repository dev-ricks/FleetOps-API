package com.fleetops.dto;

import lombok.*;

import java.time.LocalDate;

/**
 * Response model representing an Inspection, optionally embedding a {@link VehicleResponse}
 * summary of the associated vehicle.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InspectionResponse {
    private Long id;
    private LocalDate inspectionDate;
    private String status;
    private VehicleResponse vehicle;
}
