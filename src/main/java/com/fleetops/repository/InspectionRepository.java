package com.fleetops.repository;

import com.fleetops.entity.Inspection;
import com.fleetops.entity.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InspectionRepository extends JpaRepository<Inspection, Long> {

    List<Inspection> findByVehicle(Vehicle vehicle);

    @Query("select i from Inspection i where i.vehicle.id = :vehicleId order by i.inspectionDate desc")
    Page<Inspection> findByVehicleIdOrderByDateDesciption(@Param("vehicleId") Long vehicleId, Pageable pageable);

    @Query("select i from Inspection i where i.inspectionDate = (select max(i2.inspectionDate) from Inspection i2 where i2.vehicle = i.vehicle) and i.vehicle.id = :vehicleId")
    Optional<Inspection> findLatestByVehicleId(@Param("vehicleId") Long vehicleId);

    long countByVehicleId(Long vehicleId);
}
