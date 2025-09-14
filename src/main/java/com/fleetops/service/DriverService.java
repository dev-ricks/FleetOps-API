package com.fleetops.service;

import com.fleetops.entity.Driver;
import com.fleetops.exception.DriverNotFoundException;
import com.fleetops.repository.DriverRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class DriverService {

    private final DriverRepository driverRepository;

    public DriverService(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    public Driver getById(Long id) {
        Objects.requireNonNull(id, "Driver id must not be null");
        return driverRepository.findById(id).orElseThrow(() -> new DriverNotFoundException("Driver not found"));
    }

    public List<Driver> getAll() {
        return driverRepository.findAll();
    }

    @Transactional
    public Driver create(Driver driver) {
        Objects.requireNonNull(driver, "Driver must not be null");
        return driverRepository.save(driver);
    }

    @Transactional
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
    public void delete(Long id) {
        Objects.requireNonNull(id, "Driver id must not be null");
        if (!driverRepository.existsById(id)) {
            throw new DriverNotFoundException("Driver not found for deletion");
        }
        driverRepository.deleteById(id);
    }
}
