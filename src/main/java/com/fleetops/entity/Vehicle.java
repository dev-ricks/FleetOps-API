package com.fleetops.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * JPA entity representing a vehicle in the fleet.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // surrogate primary key

    private String licensePlate; // normalized uppercase string
    private String make;         // manufacturer
    private String model;        // product model
}
