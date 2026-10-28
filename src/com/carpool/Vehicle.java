package com.carpool.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a vehicle that can be used for carpooling trips.
 */
public class Vehicle {
    
    private final String id;
    private String brand;
    private String model;
    private int year;
    private String licensePlate;
    private String color;
    private VehicleType type;
    private int seats;
    private Driver owner;
    private boolean active;
    
    /**
     * Creates a new Vehicle.
     */
    public Vehicle(String brand, String model, int year, String licensePlate, 
                   String color, VehicleType type, int seats) {
        this(UUID.randomUUID().toString(), brand, model, year, licensePlate, color, type, seats, true);
    }

    /**
     * Creates a vehicle with a fixed identity and active flag.
     */
    public Vehicle(String id, String brand, String model, int year, String licensePlate,
                   String color, VehicleType type, int seats, boolean active) {
        this.id = Objects.requireNonNull(id, "Id cannot be null");
        this.brand = Objects.requireNonNull(brand, "Brand cannot be null");
        this.model = Objects.requireNonNull(model, "Model cannot be null");
        this.year = year;
        this.licensePlate = Objects.requireNonNull(licensePlate, "License plate cannot be null");
        this.color = color;
        this.type = type;
        this.seats = seats > 0 ? seats : type.getTypicalSeats();
        this.active = active;
    }
    
    // ==================== Getters ====================
    
    public String getId() {
        return id;
    }
    
    public String getBrand() {
        return brand;
    }
    
    public String getModel() {
        return model;
    }
    
    public int getYear() {
        return year;
    }
    
    public String getLicensePlate() {
        return licensePlate;
    }
    
    public String getColor() {
        return color;
    }
    
    public VehicleType getType() {
        return type;
    }
    
    public int getSeats() {
        return seats;
    }
    
    public Driver getOwner() {
        return owner;
    }
    
    public boolean isActive() {
        return active;
    }
    
    /**
     * Returns a formatted description of the vehicle.
     */
    public String getFullDescription() {
        return String.format("%d %s %s (%s) - %s, %d seats", 
                year, brand, model, color, type.getDisplayName(), seats);
    }
    
    /**
     * Returns a short description.
     */
    public String getShortDescription() {
        return String.format("%s %s (%s)", brand, model, licensePlate);
    }
    
    // ==================== Setters ====================
    
    public void setBrand(String brand) {
        this.brand = brand;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public void setYear(int year) {
        this.year = year;
    }
    
    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }
    
    public void setColor(String color) {
        this.color = color;
    }
    
    public void setType(VehicleType type) {
        this.type = type;
    }
    
    public void setSeats(int seats) {
        this.seats = Math.max(1, seats);
    }
    
    public void setOwner(Driver owner) {
        this.owner = owner;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    // ==================== Object Methods ====================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vehicle vehicle = (Vehicle) o;
        return Objects.equals(id, vehicle.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Vehicle{" +
                "id='" + id + '\'' +
                ", " + getFullDescription() +
                ", plate='" + licensePlate + '\'' +
                '}';
    }
}
