package com.fleetops.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverResponse {
    private Long id;
    private String name;
    private String licenseNumber;
}
