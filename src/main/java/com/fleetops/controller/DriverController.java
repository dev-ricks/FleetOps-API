package com.fleetops.controller;

import com.fleetops.dto.DriverRequest;
import com.fleetops.dto.DriverResponse;
import com.fleetops.entity.Driver;
import com.fleetops.service.DriverService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * REST controller for managing driver resources.
 * <p>
 * Exposes CRUD endpoints under the base path {@code /api/drivers} and produces JSON responses.
 * Validation is applied to request payloads where applicable. Errors and constraint violations
 * are handled centrally by {@code GlobalControllerExceptionHandler}.
 */
@RestController
@RequestMapping(value = "/api/drivers", produces = MediaType.APPLICATION_JSON_VALUE)
public class DriverController {

    private final DriverService service;

    public DriverController(DriverService service) {
        this.service = service;
    }

    /**
     * Retrieve a driver by its unique identifier.
     *
     * @param id the driver identifier (Long)
     * @return HTTP 200 with {@link DriverResponse} when found, otherwise mapped to HTTP 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<DriverResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(service.getById(id)));
    }

    /**
     * List all drivers.
     *
     * @return a JSON array of {@link DriverResponse}
     */
    @GetMapping("/list")
    public List<DriverResponse> list() {
        return service.getAll().stream().map(this::toResponse).toList();
    }

    /**
     * Create a new driver.
     *
     * @param request the validated driver request payload
     * @return HTTP 201 with Location header and created {@link DriverResponse}
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DriverResponse> create(@Valid @RequestBody DriverRequest request) {
        Driver toSave = new Driver();
        toSave.setName(request.getName());
        toSave.setLicenseNumber(request.getLicenseNumber());
        Driver saved = service.create(toSave);
        URI location = org.springframework.web.util.UriComponentsBuilder
                .newInstance()
                .path("/api/drivers/{id}")
                .buildAndExpand(saved.getId())
                .toUri();
        return ResponseEntity.created(location)
                             .contentType(MediaType.APPLICATION_JSON)
                             .body(toResponse(saved));
    }

    /**
     * Update an existing driver by applying non-null fields from the request.
     *
     * @param id      the identifier of the driver to update
     * @param request a partial update payload
     * @return HTTP 200 with updated {@link DriverResponse}
     */
    @PutMapping("/{id}")
    public ResponseEntity<DriverResponse> update(@PathVariable Long id, @RequestBody DriverRequest request) {
        Driver patch = new Driver();
        if (request.getName() != null) {
            patch.setName(request.getName());
        }
        if (request.getLicenseNumber() != null) {
            patch.setLicenseNumber(request.getLicenseNumber());
        }
        Driver updated = service.update(id, patch);
        return ResponseEntity.ok(toResponse(updated));
    }

    /**
     * Delete a driver by id.
     *
     * @param id the identifier of the driver to delete
     * @return HTTP 204 when deletion succeeds
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Map a domain {@link Driver} to its API response representation.
     *
     * @param d the domain driver entity
     * @return the corresponding {@link DriverResponse} or null if input is null
     */
    private DriverResponse toResponse(Driver d) {
        if (d == null) {
            return null;
        }
        return new DriverResponse(d.getId(), d.getName(), d.getLicenseNumber());
    }
}
