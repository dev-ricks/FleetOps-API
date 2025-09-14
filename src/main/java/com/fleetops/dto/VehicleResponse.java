package com.fleetops.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleResponse {
    private Long id;
    private String licensePlate;
    private String make;
    private String model;
}
