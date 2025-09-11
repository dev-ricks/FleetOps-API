package com.fleetops.service;

import com.fleetops.entity.Inspection;
import com.fleetops.exception.InspectionNotFoundException;
import com.fleetops.repository.InspectionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InspectionService {

    private final InspectionRepository inspectionRepository;

    public InspectionService(InspectionRepository inspectionRepository) {
        this.inspectionRepository = inspectionRepository;
    }

    public List<Inspection> getAll() {
        return inspectionRepository.findAll();
    }

    public Inspection create(Inspection inspection) {
        return inspectionRepository.save(inspection);
    }

    public Inspection getById(Long id) {
        return inspectionRepository.findById(id).orElseThrow(() -> new InspectionNotFoundException("Inspection not found"));
    }

    public Inspection update(Long id, Inspection inspection) {
        Inspection existing = getById(id);
        existing.setInspectionDate(inspection.getInspectionDate());
        existing.setStatus(inspection.getStatus());
        existing.setVehicle(inspection.getVehicle());
        return inspectionRepository.save(existing);
    }

    public void delete(Long id) {
        inspectionRepository.deleteById(id);
    }
}
