package com.fleetops.repository;

import com.fleetops.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DriverRepository extends JpaRepository<Driver, Long> {

    Optional<Driver> findById(long id);

    Optional<Driver> findByName(String name);

    Optional<Driver> findByLicenseNumber(String licenseNumber);
}

