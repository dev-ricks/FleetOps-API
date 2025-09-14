package com.fleetops.controller;

import com.fleetops.dto.*;
import com.fleetops.entity.Inspection;
import com.fleetops.entity.Vehicle;
import com.fleetops.service.InspectionService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "/api/inspections", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class InspectionController {

    private final InspectionService service;

    public InspectionController(InspectionService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ResponseEntity<InspectionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(service.getById(id)));
    }

    @GetMapping("/list")
    public List<InspectionResponse> list() {
        return service.getAll().stream().map(this::toResponse).toList();
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
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

    @PutMapping("/{id}")
    public ResponseEntity<InspectionResponse> update(@PathVariable Long id,
                                                     @RequestBody InspectionUpdateRequest request) {
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

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
