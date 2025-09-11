package com.fleetops.service;

import com.fleetops.entity.Driver;
import com.fleetops.exception.DriverNotFoundException;
import com.fleetops.repository.DriverRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DriverService {

    private final DriverRepository driverRepository;

    public DriverService(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    public Driver getById(Long id) {
        return driverRepository.findById(id).orElseThrow(() -> new DriverNotFoundException("Driver not found"));
    }

    public List<Driver> getAll() {
        return driverRepository.findAll();
    }

    public Driver create(Driver driver) {
        return driverRepository.save(driver);
    }

    public Driver update(Long id, Driver driver) {
        Driver existing = getById(id);
        existing.setName(driver.getName());
        existing.setLicenseNumber(driver.getLicenseNumber());
        return driverRepository.save(existing);
    }

    public void delete(Long id) {
        driverRepository.deleteById(id);
    }
}
