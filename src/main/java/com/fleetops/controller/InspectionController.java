package com.fleetops.controller;

import com.fleetops.entity.Inspection;
import com.fleetops.service.InspectionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inspections")
public class InspectionController {

    private final InspectionService service;

    public InspectionController(InspectionService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public Inspection getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping("/list")
    public List<Inspection> list() {
        return service.getAll();
    }

    @PostMapping
    public Inspection create(@RequestBody Inspection driver) {
        return service.create(driver);
    }

    @PutMapping("/{id}")
    public Inspection update(@PathVariable Long id, @RequestBody Inspection inspection) {
        return service.update(id, inspection);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
