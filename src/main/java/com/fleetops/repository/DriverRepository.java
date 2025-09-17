package com.fleetops.repository;

import com.fleetops.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data repository for {@link Driver} aggregates.
 * <p>
 * Extends {@link JpaRepository} to inherit standard CRUD operations and declares
 * simple finder methods used by the service layer.
 */
public interface DriverRepository extends JpaRepository<Driver, Long> {

    /** Find a driver by id. */
    Optional<Driver> findById(long id);

    /** Find a driver by exact name. */
    Optional<Driver> findByName(String name);

    /** Find a driver by license number. */
    Optional<Driver> findByLicenseNumber(String licenseNumber);
}

