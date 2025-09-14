package com.fleetops.service;

import com.fleetops.entity.Vehicle;
import com.fleetops.exception.*;
import com.fleetops.repository.VehicleRepository;
import org.springframework.dao.DataAccessException;
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
            String normalizedPlate = normalizeLicensePlate(v.getLicensePlate());
            if (normalizedPlate == null || normalizedPlate.isEmpty()) {
                throw new IllegalArgumentException("License plate must not be null or empty");
            }
            if (repo.existsByLicensePlate(normalizedPlate)) {
                throw new LicensePlateAlreadyExistsException(
                        "Vehicle with license plate " + normalizedPlate + " already exists.");
            }
            v.setLicensePlate(normalizedPlate);
            return repo.save(v);
        } catch (LicensePlateAlreadyExistsException | VehicleNotFoundException | IllegalArgumentException e) {
            throw e;
        } catch (DataAccessException dataAccessException) {
            throw new ServiceException("Error creating vehicle", dataAccessException);
        }
    }

    @Transactional
    public Vehicle update(Long id, Vehicle patch) {
        Objects.requireNonNull(id, "Vehicle id must not be null");
        Objects.requireNonNull(patch, "Vehicle must not be null");
        try {
            Vehicle existing = repo.findById(id).orElseThrow(() -> new VehicleNotFoundException("Vehicle not found"));
            if (patch.getLicensePlate() != null) {
                String normalizedPlate = normalizeLicensePlate(patch.getLicensePlate());
                if (repo.existsByLicensePlateAndIdNot(normalizedPlate, id)) {
                    throw new LicensePlateAlreadyExistsException(
                            "Vehicle with license plate " + normalizedPlate + " already exists.");
                }
                existing.setLicensePlate(normalizedPlate);
            }
            if (patch.getMake() != null) {
                existing.setMake(normalizeGeneralString(patch.getMake()));
            }
            if (patch.getModel() != null) {
                existing.setModel(normalizeGeneralString(patch.getModel()));
            }
            return repo.save(existing);
        } catch (LicensePlateAlreadyExistsException | VehicleNotFoundException | IllegalArgumentException e) {
            throw e; // let intended exceptions proceed
        } catch (DataAccessException dae) {
            throw new ServiceException("Error updating vehicle", dae);
        }
    }

    @Transactional
    public void delete(Long id) {
        Objects.requireNonNull(id, "Vehicle id must not be null");
        if (!repo.existsById(id)) {
            throw new VehicleNotFoundException("Vehicle not found for deletion");
        }
        try {
            repo.deleteById(id);
        } catch (VehicleNotFoundException e) {
            throw e;
        } catch (DataAccessException dae) {
            throw new ServiceException("Error deleting vehicle", dae);
        }
    }

    private String normalizeLicensePlate(String plate) {
        return plate == null ? null : plate.trim().toUpperCase();
    }

    private String normalizeGeneralString(String dataValue) {
        return dataValue == null ? null : dataValue.trim();
    }
}
