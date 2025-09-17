package com.fleetops.controller;

import com.fleetops.dto.VehicleRequest;
import com.fleetops.dto.VehicleResponse;
import com.fleetops.entity.Vehicle;
import com.fleetops.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * REST controller for managing vehicle resources.
 * <p>
 * Provides CRUD endpoints under {@code /api/vehicles}. Payloads are validated where applicable and
 * errors are mapped by {@code GlobalControllerExceptionHandler}. Responses are JSON.
 */
@RestController
@RequestMapping(value = "/api/vehicles", produces = MediaType.APPLICATION_JSON_VALUE)
public class VehicleController {

    private final VehicleService service;

    public VehicleController(VehicleService service) {
        this.service = service;
    }

    /**
     * Retrieve a vehicle by its id.
     *
     * @param id vehicle identifier
     * @return HTTP 200 with {@link VehicleResponse} when found, otherwise mapped to HTTP 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponse> getById(@PathVariable Long id) {
        Vehicle vehicle = service.getById(id);
        return ResponseEntity.ok(toResponse(vehicle));
    }

    /**
     * List all vehicles.
     *
     * @return a JSON array of {@link VehicleResponse}
     */
    @GetMapping("/list")
    public List<VehicleResponse> list() {
        return service.getAll().stream().map(this::toResponse).toList();
    }

    /**
     * Create a vehicle.
     *
     * @param request the validated vehicle request
     * @return HTTP 201 with Location header and created {@link VehicleResponse}
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VehicleResponse> create(@Valid @RequestBody VehicleRequest request) {
        Vehicle toSave = new Vehicle();
        toSave.setLicensePlate(request.getLicensePlate());
        toSave.setMake(request.getMake());
        toSave.setModel(request.getModel());
        Vehicle saved = service.create(toSave);
        // Build Location without using request-derived host to avoid SAST warnings about user-controlled input
        URI location = UriComponentsBuilder
                .newInstance()
                .path("/api/vehicles/{id}")
                .buildAndExpand(saved.getId())
                .toUri();
        return ResponseEntity
                .created(location)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(toResponse(saved));
    }

    /**
     * Update a vehicle by applying non-null fields from the request payload.
     *
     * @param id      identifier of the vehicle to update
     * @param request partial update payload
     * @return HTTP 200 with updated {@link VehicleResponse}
     */
    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponse> update(@PathVariable Long id, @RequestBody VehicleRequest request) {
        Vehicle patch = new Vehicle();
        // Allow partial update: only set fields that are not null in request
        if (request.getLicensePlate() != null) {
            patch.setLicensePlate(request.getLicensePlate());
        }
        if (request.getMake() != null) {
            patch.setMake(request.getMake());
        }
        if (request.getModel() != null) {
            patch.setModel(request.getModel());
        }
        Vehicle updated = service.update(id, patch);
        return ResponseEntity.ok(toResponse(updated));
    }

    /**
     * Delete a vehicle by id.
     *
     * @param id identifier of the vehicle to delete
     * @return HTTP 204 on success
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Map a domain {@link Vehicle} to {@link VehicleResponse}.
     *
     * @param v domain vehicle
     * @return API response model, or null if input is null
     */
    private VehicleResponse toResponse(Vehicle v) {
        if (v == null) {
            return null;
        }
        return new VehicleResponse(v.getId(), v.getLicensePlate(), v.getMake(), v.getModel());
    }
}
