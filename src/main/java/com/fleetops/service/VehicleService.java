package com.fleetops.service;

import com.fleetops.entity.Vehicle;
import com.fleetops.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class VehicleService {

    private final VehicleRepository repo;

    public VehicleService(VehicleRepository repo) {
        this.repo = repo;
    }

    public List<Vehicle> getAll() {
        return repo.findAll();
    }

    public Vehicle create(Vehicle v) {
        return repo.save(v);
    }
}
