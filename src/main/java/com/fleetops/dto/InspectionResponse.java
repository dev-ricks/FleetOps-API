package com.fleetops.dto;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InspectionResponse {
    private Long id;
    private LocalDate inspectionDate;
    private String status;
    private VehicleResponse vehicle;
}
