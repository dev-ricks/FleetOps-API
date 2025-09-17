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

@RestController
@RequestMapping(value = "/api/drivers", produces = MediaType.APPLICATION_JSON_VALUE)
public class DriverController {

    private final DriverService service;

    public DriverController(DriverService service) {
        this.service = service;
    }


    @GetMapping("/{id}")
    public ResponseEntity<DriverResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(service.getById(id)));
    }

    @GetMapping("/list")
    public List<DriverResponse> list() {
        return service.getAll().stream().map(this::toResponse).toList();
    }

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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    private DriverResponse toResponse(Driver d) {
        if (d == null) {
            return null;
        }
        return new DriverResponse(d.getId(), d.getName(), d.getLicenseNumber());
    }
}
