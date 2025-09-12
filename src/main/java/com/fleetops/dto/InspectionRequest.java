package com.fleetops.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class InspectionRequest {
    @NotNull
    private LocalDate inspectionDate;

    @NotBlank
    private String status;

    @NotNull
    private Long vehicleId;

    public LocalDate getInspectionDate() { return inspectionDate; }
    public void setInspectionDate(LocalDate inspectionDate) { this.inspectionDate = inspectionDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }
}
