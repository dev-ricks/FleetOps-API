package com.fleetops.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class InspectionUpdateRequest {
    private LocalDate inspectionDate;
    private String status;
    private Long vehicleId;
}
