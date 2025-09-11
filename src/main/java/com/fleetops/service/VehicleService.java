package com.fleetops.service;

import com.fleetops.entity.Vehicle;
import com.fleetops.exception.LicensePlateAlreadyExistsException;
import com.fleetops.exception.VehicleNotFoundException;
import com.fleetops.repository.VehicleRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class VehicleService {

    private final VehicleRepository repo;

    public VehicleService(VehicleRepository repo) {
        this.repo = repo;
    }

    public List<Vehicle> getAll() {
        return repo.findAll();
    }

    public Vehicle getById(Long id) {
        Objects.requireNonNull(id, "Vehicle id must not be null");
        return repo.findById(id).orElseThrow(() -> new VehicleNotFoundException("Vehicle not found"));
    }

    @Transactional
    public Vehicle create(Vehicle v) {
        Objects.requireNonNull(v, "Vehicle must not be null");
        try {
            return repo.save(v);
        } catch (DataIntegrityViolationException dive) {
            throw new LicensePlateAlreadyExistsException(
                    "Vehicle with license plate " + v.getLicensePlate() + " already exists.");
        } catch (Exception e) {
            throw new RuntimeException("Error creating vehicle: " + e.getMessage());
        }
    }

    @Transactional
    public Vehicle update(Long id, Vehicle v) {
        Objects.requireNonNull(id, "Vehicle id must not be null");
        Objects.requireNonNull(v, "Vehicle must not be null");
        try {
            Vehicle existing = getById(id);
            if (v.getLicensePlate() != null) {
                existing.setLicensePlate(v.getLicensePlate());
            }
            if (v.getMake() != null) {
                existing.setMake(v.getMake());
            }
            if (v.getModel() != null) {
                existing.setModel(v.getModel());
            }
            return repo.save(existing);
        } catch (DataIntegrityViolationException dive) {
            throw new LicensePlateAlreadyExistsException(
                    "Vehicle with license plate " + v.getLicensePlate() + " already exists.");
        } catch (VehicleNotFoundException vnfe) {
            throw vnfe;
        } catch (Exception e) {
            throw new RuntimeException("Error updating vehicle: " + e.getMessage());
        }
    }

    @Transactional
    public void delete(Long id) {
        Objects.requireNonNull(id, "Vehicle id must not be null");
        if (!repo.existsById(id)) {
            throw new VehicleNotFoundException("Vehicle not found for deletion");
        }
        repo.deleteById(id);
    }
}
