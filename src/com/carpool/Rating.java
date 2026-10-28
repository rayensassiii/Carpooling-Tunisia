package com.carpool.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a rating given by a passenger to a driver after a trip.
 */
public class Rating {
    
    private final String id;
    private final Passenger passenger;
    private final Driver driver;
    private final Trip trip;
    private final int score;
    private final String comment;
    private final LocalDateTime createdAt;
    
    private static final int MIN_SCORE = 1;
    private static final int MAX_SCORE = 5;
    
    /**
     * Creates a new Rating.
     */
    public Rating(Passenger passenger, Driver driver, Trip trip, int score, String comment) {
        this(UUID.randomUUID().toString(), passenger, driver, trip, score, comment, LocalDateTime.now());
    }

    /**
     * Creates a rating with a fixed identity and creation time.
     */
    public Rating(String id, Passenger passenger, Driver driver, Trip trip, int score, String comment,
                  LocalDateTime createdAt) {
        this.id = Objects.requireNonNull(id, "Id cannot be null");
        this.passenger = Objects.requireNonNull(passenger, "Passenger cannot be null");
        this.driver = Objects.requireNonNull(driver, "Driver cannot be null");
        this.trip = Objects.requireNonNull(trip, "Trip cannot be null");
        this.score = Math.max(MIN_SCORE, Math.min(MAX_SCORE, score));
        this.comment = comment;
        this.createdAt = Objects.requireNonNull(createdAt, "Created time cannot be null");
    }
    
    // ==================== Rating Operations ====================
    
    /**
     * Rates the driver (adds this rating to the driver's list).
     */
    public void rateDriver() {
        driver.addRating(this);
    }
    
    /**
     * Validates if this rating is valid.
     */
    public boolean isValid() {
        // Check if passenger was on this trip
        boolean wasOnTrip = false;
        for (Reservation r : trip.getReservations()) {
            if (r.getPassenger().equals(passenger) && 
                (r.getStatus() == ReservationStatus.COMPLETED || 
                 r.getStatus() == ReservationStatus.CONFIRMED)) {
                wasOnTrip = true;
                break;
            }
        }
        
        // Check if driver is the trip's driver
        boolean isCorrectDriver = trip.getDriver().equals(driver);
        
        return wasOnTrip && isCorrectDriver;
    }
    
    /**
     * Returns stars representation of the score.
     */
    public String getStars() {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < score; i++) {
            stars.append("★");
        }
        for (int i = score; i < MAX_SCORE; i++) {
            stars.append("☆");
        }
        return stars.toString();
    }
    
    // ==================== Getters ====================
    
    public String getId() {
        return id;
    }
    
    public Passenger getPassenger() {
        return passenger;
    }
    
    public Driver getDriver() {
        return driver;
    }
    
    public Trip getTrip() {
        return trip;
    }
    
    public int getScore() {
        return score;
    }
    
    public String getComment() {
        return comment;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    /**
     * Returns the route of the trip this rating is for.
     */
    public String getTripRoute() {
        return trip.getRoute();
    }
    
    /**
     * Returns formatted creation date.
     */
    public String getFormattedDate() {
        return createdAt.toLocalDate().toString();
    }
    
    // ==================== Object Methods ====================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rating rating = (Rating) o;
        return Objects.equals(id, rating.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Rating{" +
                "id='" + id + '\'' +
                ", passenger=" + passenger.getFullName() +
                ", driver=" + driver.getFullName() +
                ", score=" + getStars() +
                ", trip=" + trip.getRoute() +
                '}';
    }
}
