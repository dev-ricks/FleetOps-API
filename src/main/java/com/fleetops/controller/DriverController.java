package com.fleetops.controller;

import com.fleetops.entity.Driver;
import com.fleetops.service.DriverService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {

    private final DriverService service;

    public DriverController(DriverService service) {
        this.service = service;
    }

    @GetMapping("/status")
    public String getStatus() {
        return "<font color=\"white\">Driver service is running.</font>";
    }

    @GetMapping("/{id}")
    public Driver getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping("/list")
    public List<Driver> list() {
        return service.getAll();
    }

    @PostMapping
    public Driver create(@RequestBody Driver driver) {
        return service.create(driver);
    }

    @PutMapping("/{id}")
    public Driver update(@PathVariable Long id, @RequestBody Driver driver) {
        return service.update(id, driver);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
