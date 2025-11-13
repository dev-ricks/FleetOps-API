package com.fleetops.controller;

import com.fleetops.dto.*;
import com.fleetops.entity.Inspection;
import com.fleetops.entity.Vehicle;
import com.fleetops.service.InspectionService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * REST controller for managing inspection resources.
 * <p>
 * Exposes CRUD endpoints under {@code /api/inspections}. Requests are validated and
 * responses are JSON. Errors/violations are handled centrally by the global exception handler.
 */
@RestController
@RequestMapping(value = "/api/inspections", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class InspectionController {

    private final InspectionService service;

    /**
     * Constructs an instance with the required service dependency.
     *
     * @param service inspection service instance
     */
    public InspectionController(InspectionService service) {
        this.service = service;
    }

    /**
     * Retrieve an inspection by id.
     *
     * @param id inspection identifier
     * @return HTTP 200 with {@link InspectionResponse} or mapped to HTTP 404 if not found
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<InspectionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(service.getById(id)));
    }

    /**
     * List all inspections.
     *
     * @return a JSON array of {@link InspectionResponse}
     */
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public List<InspectionResponse> list() {
        return service.getAll().stream().map(this::toResponse).toList();
    }

    /**
     * Create an inspection.
     *
     * @param request validated inspection creation payload
     * @return HTTP 201 with Location header and created {@link InspectionResponse}
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<InspectionResponse> create(@Valid @RequestBody InspectionRequest request) {
        Inspection saved = service.create(request);
        URI location = org.springframework.web.util.UriComponentsBuilder
                .newInstance()
                .path("/api/inspections/{id}")
                .buildAndExpand(saved.getId())
                .toUri();
        return ResponseEntity.created(location)
                             .contentType(MediaType.APPLICATION_JSON)
                             .body(toResponse(saved));
    }

    /**
     * Update an inspection with non-null fields from the request payload.
     *
     * @param id      inspection identifier
     * @param request partial update payload
     * @return HTTP 200 with updated {@link InspectionResponse}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<InspectionResponse> update(@PathVariable Long id,
                                                     @Valid @RequestBody InspectionUpdateRequest request) {
        Inspection patch = new Inspection();
        if (request.getInspectionDate() != null) {
            patch.setInspectionDate(request.getInspectionDate());
        }
        if (request.getStatus() != null) {
            patch.setStatus(request.getStatus());
        }
        if (request.getVehicleId() != null) {
            Vehicle v = new Vehicle();
            v.setId(request.getVehicleId());
            patch.setVehicle(v);
        }
        Inspection updated = service.update(id, patch);
        return ResponseEntity.ok(toResponse(updated));
    }

    /**
     * Delete an inspection by id.
     *
     * @param id inspection identifier
     * @return HTTP 204 on success
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Map a domain {@link Inspection} to {@link InspectionResponse} including nested vehicle summary.
     *
     * @param i domain inspection entity
     * @return API response model, or null if input is null
     */
    private InspectionResponse toResponse(Inspection i) {
        if (i == null) {
            return null;
        }
        VehicleResponse v = null;
        if (i.getVehicle() != null) {
            v = new VehicleResponse(
                    i.getVehicle().getId(),
                    i.getVehicle().getLicensePlate(),
                    i.getVehicle().getMake(),
                    i.getVehicle().getModel()
            );
        }
        return new InspectionResponse(i.getId(), i.getInspectionDate(), i.getStatus(), v);
    }
}
