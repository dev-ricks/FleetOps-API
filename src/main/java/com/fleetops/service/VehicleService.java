package com.fleetops.service;

import com.fleetops.entity.Vehicle;
import com.fleetops.exception.*;
import com.fleetops.repository.VehicleRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Application service encapsulating business logic for {@link com.fleetops.entity.Vehicle}.
 * <p>
 * Provides CRUD operations with validation, normalization, and conflict detection
 * (e.g., duplicate license plates). Data-access exceptions are wrapped as
 * {@link com.fleetops.exception.ServiceException} where appropriate.
 */
@Service
@Transactional(readOnly = true)
public class VehicleService {

    private final VehicleRepository repo;

    public VehicleService(VehicleRepository repo) {
        this.repo = repo;
    }

    /**
     * Retrieve all vehicles.
     *
     * @return list of vehicles
     */
    public List<Vehicle> getAll() {
        return repo.findAll();
    }

    /**
     * Find a vehicle by id or throw {@link com.fleetops.exception.VehicleNotFoundException}.
     *
     * @param id vehicle identifier (must not be null)
     * @return the vehicle entity
     */
    public Vehicle getById(Long id) {
        Objects.requireNonNull(id, "Vehicle id must not be null");
        return repo.findById(id).orElseThrow(() -> new VehicleNotFoundException("Vehicle not found"));
    }

    @Transactional
    /**
     * Create a new vehicle after normalizing and validating fields.
     *
     * @param v vehicle to create (must not be null)
     * @return persisted vehicle
     * @throws IllegalArgumentException            when inputs are invalid
     * @throws LicensePlateAlreadyExistsException  when license plate is already in use
     */
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
    /**
     * Apply a partial update to an existing vehicle. Only non-null fields in {@code patch}
     * are applied.
     *
     * @param id    identifier of the vehicle to update (must not be null)
     * @param patch partial vehicle containing fields to update (must not be null)
     * @return the updated vehicle
     */
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
    /**
     * Delete a vehicle by id. Throws {@link VehicleNotFoundException} if not present.
     *
     * @param id identifier to delete (must not be null)
     */
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

    /**
     * Normalize license plate by trimming and uppercasing.
     */
    private String normalizeLicensePlate(String plate) {
        return plate == null ? null : plate.trim().toUpperCase();
    }

    /**
     * Normalize string by trimming whitespace.
     */
    private String normalizeGeneralString(String dataValue) {
        return dataValue == null ? null : dataValue.trim();
    }
}
