package com.fleetops.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request model for creating or updating a Vehicle via the API.
 * Uses Bean Validation to enforce non-blank fields and maximum lengths.
 */
@Data
public class VehicleRequest {
    @NotBlank
    @Size(max = 20)
    private String licensePlate;

    @NotBlank
    @Size(max = 50)
    private String make;

    @NotBlank
    @Size(max = 50)
    private String model;
}
