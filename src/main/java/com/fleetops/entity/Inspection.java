package com.fleetops.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * JPA entity representing an inspection performed on a vehicle.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inspection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // surrogate primary key

    private LocalDate inspectionDate; // date the inspection occurred
    private String status;            // normalized status value (e.g., PASSED/FAILED)

    @ManyToOne
    private Vehicle vehicle;          // owning vehicle
}
