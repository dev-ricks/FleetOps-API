package com.fleetops.service;

import com.fleetops.entity.Driver;
import com.fleetops.exception.DriverNotFoundException;
import com.fleetops.repository.DriverRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Application service for managing {@link com.fleetops.entity.Driver} domain objects.
 * <p>
 * Provides CRUD operations with null-safety and not-found semantics. All read operations
 * run in a read-only transaction; mutating operations declare transactional boundaries.
 */
@Service
@Transactional(readOnly = true)
public class DriverService {

    private final DriverRepository driverRepository;

    public DriverService(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    /**
     * Retrieve a driver by id or throw when not found.
     *
     * @param id driver identifier (must not be null)
     * @return the driver entity
     * @throws com.fleetops.exception.DriverNotFoundException when not found
     */
    public Driver getById(Long id) {
        Objects.requireNonNull(id, "Driver id must not be null");
        return driverRepository.findById(id).orElseThrow(() -> new DriverNotFoundException("Driver not found"));
    }

    /**
     * Retrieve all drivers.
     *
     * @return list of drivers
     */
    public List<Driver> getAll() {
        return driverRepository.findAll();
    }

    @Transactional
    /**
     * Create a driver.
     *
     * @param driver new driver entity (must not be null)
     * @return persisted driver
     */
    public Driver create(Driver driver) {
        Objects.requireNonNull(driver, "Driver must not be null");
        return driverRepository.save(driver);
    }

    @Transactional
    /**
     * Partially update a driver by applying non-null fields from the provided entity.
     *
     * @param id     identifier of the driver to update (must not be null)
     * @param driver partial driver with fields to update (must not be null)
     * @return updated driver
     */
    public Driver update(Long id, Driver driver) {
        Objects.requireNonNull(id, "Driver id must not be null");
        Objects.requireNonNull(driver, "Driver must not be null");
        Driver existing = getById(id);
        if (driver.getName() != null) {
            existing.setName(driver.getName());
        }
        if (driver.getLicenseNumber() != null) {
            existing.setLicenseNumber(driver.getLicenseNumber());
        }
        return driverRepository.save(existing);
    }

    @Transactional
    /**
     * Delete a driver by id.
     *
     * @param id identifier to delete (must not be null)
     * @throws com.fleetops.exception.DriverNotFoundException when the entity does not exist
     */
    public void delete(Long id) {
        Objects.requireNonNull(id, "Driver id must not be null");
        if (!driverRepository.existsById(id)) {
            throw new DriverNotFoundException("Driver not found for deletion");
        }
        driverRepository.deleteById(id);
    }
}
