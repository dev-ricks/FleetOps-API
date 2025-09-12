package com.fleetops.dto;

import java.time.LocalDate;

public class InspectionResponse {
    private Long id;
    private LocalDate inspectionDate;
    private String status;
    private VehicleResponse vehicle;

    public InspectionResponse() {}

    public InspectionResponse(Long id, LocalDate inspectionDate, String status, VehicleResponse vehicle) {
        this.id = id;
        this.inspectionDate = inspectionDate;
        this.status = status;
        this.vehicle = vehicle;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getInspectionDate() { return inspectionDate; }
    public void setInspectionDate(LocalDate inspectionDate) { this.inspectionDate = inspectionDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public VehicleResponse getVehicle() { return vehicle; }
    public void setVehicle(VehicleResponse vehicle) { this.vehicle = vehicle; }
}
