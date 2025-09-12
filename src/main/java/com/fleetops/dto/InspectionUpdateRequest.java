package com.fleetops.dto;

import java.time.LocalDate;

public class InspectionUpdateRequest {
    private LocalDate inspectionDate;
    private String status;
    private Long vehicleId;

    public LocalDate getInspectionDate() { return inspectionDate; }
    public void setInspectionDate(LocalDate inspectionDate) { this.inspectionDate = inspectionDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }
}
