package com.fleetops.repository;

import com.fleetops.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    Optional<Vehicle> findByLicensePlate(String licensePlate);

    @Query("select v from Vehicle v where exists (select i from Inspection i where i.vehicle = v and lower(i.status) = lower(:status))")
    List<Vehicle> findByInspectionStatus(@Param("status") String status);

    boolean existsByLicensePlateAndIdNot(String licensePlate, Long id);

    boolean existsByLicensePlate(String licensePlate);
}
