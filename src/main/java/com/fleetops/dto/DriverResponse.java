package com.fleetops.dto;

import lombok.*;

/**
 * Response model representing a Driver returned by the API.
 * <p>
 * Mirrors a subset of the domain fields intended for client consumption.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverResponse {
    private Long id;
    private String name;
    private String licenseNumber;
}
