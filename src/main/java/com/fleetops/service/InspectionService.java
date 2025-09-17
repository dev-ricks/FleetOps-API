package com.fleetops.service;

import com.fleetops.dto.InspectionRequest;
import com.fleetops.entity.Inspection;
import com.fleetops.entity.Vehicle;
import com.fleetops.exception.VehicleNotFoundException;
import com.fleetops.exception.ServiceException;
import com.fleetops.repository.VehicleRepository;
import org.springframework.dao.DataAccessException;
import com.fleetops.exception.InspectionNotFoundException;
import com.fleetops.repository.InspectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Application service responsible for inspection domain operations.
 * <p>
 * Provides CRUD behaviors, including overloads for creating inspections from
 * API request models. Wraps data-access errors as {@link com.fleetops.exception.ServiceException}.
 */
@Service
@Transactional(readOnly = true)
public class InspectionService {

    private final InspectionRepository inspectionRepository;
    private final VehicleRepository vehicleRepository;

    public InspectionService(InspectionRepository inspectionRepository, VehicleRepository vehicleRepository) {
        this.inspectionRepository = inspectionRepository;
        this.vehicleRepository = vehicleRepository;
    }

    /**
     * Retrieve all inspections.
     */
    public List<Inspection> getAll() {
        return inspectionRepository.findAll();
    }

    /**
     * Find an inspection by id or throw {@link com.fleetops.exception.InspectionNotFoundException}.
     *
     * @param id inspection identifier (must not be null)
     */
    public Inspection getById(Long id) {
        Objects.requireNonNull(id, "Inspection Id must not be null");
        return inspectionRepository.findById(id).orElseThrow(() -> new InspectionNotFoundException("Inspection not found"));
    }

    @Transactional
    /**
     * Create an inspection entity directly.
     *
     * @param inspection new inspection (id must be null)
     * @return persisted inspection
     */
    public Inspection create(Inspection inspection) {
        Objects.requireNonNull(inspection, "Inspection must not be null");
        if (inspection.getId() != null) {
            throw new IllegalArgumentException("Inspection id must be null on create");
        }
        return inspectionRepository.save(inspection);
    }

    @Transactional
    /**
     * Create an inspection from a request payload, connecting it to the referenced vehicle.
     *
     * @param request API request model containing date, status, and vehicleId (must not be null)
     * @return persisted inspection
     */
    public Inspection create(InspectionRequest request) {
        Objects.requireNonNull(request, "InspectionRequest must not be null");
        try {
            if (request.getVehicleId() == null) {
                throw new IllegalArgumentException("vehicleId must not be null");
            }
            Vehicle vehicleRef = vehicleRepository.getReferenceById(request.getVehicleId());
            Inspection inspection = new Inspection();
            inspection.setInspectionDate(request.getInspectionDate());
            inspection.setStatus(normalizeStatus(request.getStatus()));
            inspection.setVehicle(vehicleRef);
            return inspectionRepository.save(inspection);
        } catch (VehicleNotFoundException | IllegalArgumentException e) {
            throw e;
        } catch (DataAccessException dae) {
            throw new ServiceException("Error creating inspection", dae);
        }
    }

    @Transactional
    /**
     * Partially update an inspection by applying non-null fields.
     *
     * @param id         identifier of the inspection to update (must not be null)
     * @param inspection partial inspection
     * @return updated inspection
     */
    public Inspection update(Long id, Inspection inspection) {
        Objects.requireNonNull(inspection, "Inspection must not be null");
        Objects.requireNonNull(id, "Id must not be null");
        Inspection existing = getById(id);
        // Only update fields that are explicitly provided (non-null) to preserve existing values
        if (inspection.getInspectionDate() != null) {
            existing.setInspectionDate(inspection.getInspectionDate());
        }
        if (inspection.getStatus() != null) {
            existing.setStatus(inspection.getStatus());
        }
        if (inspection.getVehicle() != null) {
            existing.setVehicle(inspection.getVehicle());
        }
        return inspectionRepository.save(existing);
    }

    @Transactional
    /**
     * Delete an inspection by id.
     *
     * @param id identifier to delete (must not be null)
     */
    public void delete(Long id) {
        Objects.requireNonNull(id, "Inspection Id must not be null");
        Inspection existing = inspectionRepository.findById(id)
                .orElseThrow(() -> new InspectionNotFoundException("Inspection not found for deletion"));
        inspectionRepository.delete(existing);
    }

    /**
     * Demonstrates an atomic multistep operation that should fully roll back on failure.
     * Steps:
     *  1) Create a new inspection
     *  2) Update an existing inspection
     *  3) Delete another inspection
     *  4) Throw an exception to force rollback
     *
     * Intended for use in integration tests that assert the database state is unchanged
     * after this method throws.
     */
    /**
     * Demonstrates an atomic multi-step flow meant for transactional testing.
     * Always throws an exception at the end to trigger rollback.
     */
    public void compositeCreateUpdateDeleteThenFail(Inspection toCreate, Long idToUpdate, Long idToDelete) {
        java.util.Objects.requireNonNull(toCreate, "toCreate must not be null");
        java.util.Objects.requireNonNull(idToUpdate, "idToUpdate must not be null");
        java.util.Objects.requireNonNull(idToDelete, "idToDelete must not be null");
        // 1) Create
        create(toCreate);
        // 2) Update via service method
        Inspection patch = new Inspection();
        patch.setInspectionDate(toCreate.getInspectionDate());
        patch.setStatus(toCreate.getStatus());
        patch.setVehicle(toCreate.getVehicle());
        update(idToUpdate, patch);
        // 3) Delete via service method
        delete(idToDelete);
        // 4) Force failure to validate transactional rollback
        throw new RuntimeException("Intentional failure to validate transactional rollback");
    }

    /**
     * Normalize status by trimming and uppercasing.
     */
    private String normalizeStatus(String status) {
        return status == null ? null : status.trim().toUpperCase();
    }

}
