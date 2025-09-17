package com.fleetops.dto;

import lombok.*;

/**
 * Response model representing a Vehicle returned by the API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleResponse {
    private Long id;
    private String licensePlate;
    private String make;
    private String model;
}
