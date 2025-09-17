package com.fleetops.repository;

import com.fleetops.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data repository for {@link Vehicle} aggregates.
 * <p>
 * Declares finder and existence checks used for business constraints (e.g.,
 * duplicate license plate detection) as well as example query methods.
 */
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    /** Find a vehicle by its license plate (case-sensitive). */
    Optional<Vehicle> findByLicensePlate(String licensePlate);

    @Query("select v from Vehicle v where exists (select i from Inspection i where i.vehicle = v and lower(i.status) = lower(:status))")
    List<Vehicle> findByInspectionStatus(@Param("status") String status);

    /** Check whether another vehicle (not the one with the given id) has the given license plate. */
    boolean existsByLicensePlateAndIdNot(String licensePlate, Long id);

    /** Check whether any vehicle exists with the given license plate. */
    boolean existsByLicensePlate(String licensePlate);
}
