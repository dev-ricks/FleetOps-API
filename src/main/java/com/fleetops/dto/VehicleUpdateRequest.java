package com.fleetops.dto;

import com.fleetops.validation.annotation.AtLeastOneFieldNotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Partial update payload for an existing vehicle.
 * <p>
 * All fields are optional; but at least one field needs to be populated; the service layer applies only non-null values.
 */
@Data
@AtLeastOneFieldNotNull
public class VehicleUpdateRequest {

    @Size(max = 20)
    private String licensePlate;

    @Size(max = 50)
    private String make;

    @Size(max = 50)
    private String model;
}
