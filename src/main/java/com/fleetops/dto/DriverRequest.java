package com.fleetops.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request model for creating or updating a Driver via the API.
 * <p>
 * Fields are validated using Bean Validation annotations (e.g., {@link NotBlank}, {@link Size}).
 * In update scenarios, only non-null fields are applied by the controller/service layer.
 */
@Data
public class DriverRequest {
    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank
    @Size(max = 50)
    private String licenseNumber;
}
