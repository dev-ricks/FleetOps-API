package com.fleetops.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * JPA entity representing a driver in the fleet domain.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Driver {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // surrogate primary key

    private String name; // driver full name
    private String licenseNumber; // external license identifier
}
