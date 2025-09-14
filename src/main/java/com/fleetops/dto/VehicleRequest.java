package com.fleetops.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

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
