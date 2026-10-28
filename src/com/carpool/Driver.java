package com.carpool.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a driver in the carpooling system.
 * A driver can create trips, manage vehicles, and receive ratings.
 */
 public class Driver extends Passenger {
    
    private String licenseNumber;
    private final List<Vehicle> vehicles;
    private final List<Trip> trips;
    private final List<Rating> ratings;
    private boolean verified;
    
    /**
     * Creates a new Driver.
     */
    public Driver(String email, String password, String firstName, 
                  String lastName, String phoneNumber, String licenseNumber) {
        super(email, password, firstName, lastName, phoneNumber);
        this.licenseNumber = licenseNumber;
        this.vehicles = new ArrayList<>();
        this.trips = new ArrayList<>();
        this.ratings = new ArrayList<>();
        this.verified = false;
    }
    
    // ==================== Vehicle Management ====================
    
    /**
     * Adds a vehicle to the driver's fleet.
     */
    public void addVehicle(Vehicle vehicle) {
        if (vehicle != null && !vehicles.contains(vehicle)) {
            vehicle.setOwner(this);
            vehicles.add(vehicle);
        }
    }
    
    /**
     * Removes a vehicle from the driver's fleet.
     */
    public void removeVehicle(Vehicle vehicle) {
        vehicles.remove(vehicle);
    }
    
    /**
     * Returns a defensive copy of vehicles.
     */
    public List<Vehicle> getVehicles() {
        return new ArrayList<>(vehicles);
    }
    
    /**
     * Returns the primary (first) vehicle.
     */
    public Vehicle getPrimaryVehicle() {
        return vehicles.isEmpty() ? null : vehicles.get(0);
    }
    
    // ==================== Trip Management ====================
    
    /**
     * Adds a trip created by this driver.
     */
    public void addTrip(Trip trip) {
        if (trip != null && !trips.contains(trip)) {
            trips.add(trip);
        }
    }
    
    /**
     * Removes a trip.
     */
    public void removeTrip(Trip trip) {
        trips.remove(trip);
    }
    
    /**
     * Returns a defensive copy of trips.
     */
    public List<Trip> getTrips() {
        return new ArrayList<>(trips);
    }
    
    /**
     * Returns active (open or in progress) trips.
     */
    public List<Trip> getActiveTrips() {
        List<Trip> active = new ArrayList<>();
        for (Trip t : trips) {
            if (t.getStatus() == TripStatus.OPEN || 
                t.getStatus() == TripStatus.FULL ||
                t.getStatus() == TripStatus.IN_PROGRESS) {
                active.add(t);
            }
        }
        return active;
    }
    
    /**
     * Returns completed trips.
     */
    public List<Trip> getCompletedTrips() {
        List<Trip> completed = new ArrayList<>();
        for (Trip t : trips) {
            if (t.getStatus() == TripStatus.COMPLETED) {
                completed.add(t);
            }
        }
        return completed;
    }
    
    // ==================== Rating Management ====================
    
    /**
     * Adds a rating received by this driver.
     */
    public void addRating(Rating rating) {
        if (rating != null && !ratings.contains(rating)) {
            ratings.add(rating);
        }
    }
    
    /**
     * Returns a defensive copy of ratings.
     */
    public List<Rating> getRatings() {
        return new ArrayList<>(ratings);
    }
    
    /**
     * Calculates the average rating score.
     */
    public double getAverageRating() {
        if (ratings.isEmpty()) {
            return 0.0;
        }
        double sum = 0;
        for (Rating r : ratings) {
            sum += r.getScore();
        }
        return sum / ratings.size();
    }
    
    /**
     * Returns the total number of ratings received.
     */
    public int getRatingCount() {
        return ratings.size();
    }
    
    // ==================== Verification ====================
    
    /**
     * Returns whether the driver is verified.
     */
    public boolean isVerified() {
        return verified;
    }
    
    /**
     * Verifies the driver (typically done by admin).
     */
    public void verify() {
        this.verified = true;
    }
    
    /**
     * Revokes verification.
     */
    public void revokeVerification() {
        this.verified = false;
    }
    
    // ==================== Getters and Setters ====================
    
    public String getLicenseNumber() {
        return licenseNumber;
    }
    
    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }
    
    // ==================== User Role ====================
    
    @Override
    public String getRole() {
        return "Driver";
    }
    
    /**
     * Returns the total earnings from completed trips.
     */
    public double getTotalEarnings() {
        double total = 0;
        for (Trip t : trips) {
            if (t.getStatus() == TripStatus.COMPLETED) {
                for (Reservation r : t.getReservations()) {
                    if (r.getStatus() == ReservationStatus.COMPLETED && r.getPayment() != null) {
                        total += r.getPayment().getAmount();
                    }
                }
            }
        }
        return total;
    }
    
    /**
     * Returns completed trips count.
     */
    public int getCompletedTripsCount() {
        return getCompletedTrips().size();
    }
    
    @Override
    public String toString() {
        return "Driver{" +
                "id='" + getId() + '\'' +
                ", name='" + getFullName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", license='" + licenseNumber + '\'' +
                ", verified=" + verified +
                ", rating=" + String.format("%.1f", getAverageRating()) +
                ", vehicles=" + vehicles.size() +
                '}';
    }
}
