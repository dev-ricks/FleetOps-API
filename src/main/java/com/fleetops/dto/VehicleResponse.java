package com.fleetops.dto;

public class VehicleResponse {
    private Long id;
    private String licensePlate;
    private String make;
    private String model;

    public VehicleResponse() {}
    public VehicleResponse(Long id, String licensePlate, String make, String model) {
        this.id = id;
        this.licensePlate = licensePlate;
        this.make = make;
        this.model = model;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }
    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
}
