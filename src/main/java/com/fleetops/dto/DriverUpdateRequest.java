package com.fleetops.dto;

import com.fleetops.validation.annotation.AtLeastOneFieldNotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Partial update payload for an existing driver.
 * <p>
 * All fields are optional; but at least one field needs to be populated; the service layer applies only non-null values.
 */
@Data
@AtLeastOneFieldNotNull
public class DriverUpdateRequest {

    @Size(max = 100)
    private String name;

    @Size(max = 50)
    private String licenseNumber;
}
