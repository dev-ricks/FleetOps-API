package com.fleetops.service;

import com.fleetops.entity.Vehicle;
import com.fleetops.exception.LicensePlateAlreadyExistsException;
import com.fleetops.exception.VehicleNotFoundException;
import com.fleetops.repository.VehicleRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VehicleService {

    private final VehicleRepository repo;

    public VehicleService(VehicleRepository repo) {
        this.repo = repo;
    }

    public List<Vehicle> getAll() {
        return repo.findAll();
    }

    public Vehicle create(Vehicle v) {
        try {
            return repo.save(v);
        } catch (DataIntegrityViolationException dive) {
            throw new LicensePlateAlreadyExistsException(
                    "Vehicle with license plate " + v.getLicensePlate() + " already exists.");
        } catch (Exception e) {
            throw new RuntimeException("Error creating vehicle: " + e.getMessage());
        }
    }

    public Vehicle getById(Long id) {
        return repo.findById(id).orElseThrow(() -> new VehicleNotFoundException("Vehicle not found"));
    }

    public Vehicle update(Long id, Vehicle v) {
        try {
            Vehicle existing = getById(id);
            existing.setLicensePlate(v.getLicensePlate());
            existing.setMake(v.getMake());
            existing.setModel(v.getModel());
            return repo.save(existing);
        } catch (DataIntegrityViolationException dive) {
            throw new LicensePlateAlreadyExistsException(
                    "Vehicle with license plate " + v.getLicensePlate() + " already exists.");
        } catch (Exception e) {
            throw new RuntimeException("Error creating vehicle: " + e.getMessage());
        }
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}
