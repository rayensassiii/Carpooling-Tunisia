package com.carpool.service;

import com.carpool.model.*;
import com.carpool.repository.*;
import java.util.List;

/**
 * Service for rating management.
 * Handles driver ratings by passengers.
 */
public class RatingService {
    
    private final RatingRepository ratingRepository;
    private final NotificationService notificationService;
    
    public RatingService(RatingRepository ratingRepository, 
                         NotificationService notificationService) {
        this.ratingRepository = ratingRepository;
        this.notificationService = notificationService;
    }
    
    /**
     * Creates a rating for a driver.
     */
    public Rating rateDriver(Passenger passenger, Driver driver, Trip trip, 
                             int score, String comment) throws Exception {
        
        // Validate inputs
        if (passenger == null) {
            throw new Exception("Passenger is required");
        }
        
        if (driver == null) {
            throw new Exception("Driver is required");
        }
        
        if (trip == null) {
            throw new Exception("Trip is required");
        }
        
        // Check if already rated
        if (hasPassengerRatedTrip(passenger, trip)) {
            throw new Exception("You have already rated this trip");
        }
        
        // Check if trip is completed
        if (trip.getStatus() != TripStatus.COMPLETED) {
            throw new Exception("Can only rate completed trips");
        }
        
        // Check if passenger was on this trip
        boolean wasOnTrip = false;
        for (Reservation r : trip.getReservations()) {
            if (r.getPassenger().equals(passenger) && 
                (r.getStatus() == ReservationStatus.COMPLETED ||
                 (trip.getStatus() == TripStatus.COMPLETED && r.getStatus() == ReservationStatus.CONFIRMED))) {
                wasOnTrip = true;
                break;
            }
        }
        
        if (!wasOnTrip) {
            throw new Exception("You were not a passenger on this trip");
        }
        
        // Create rating
        Rating rating = new Rating(passenger, driver, trip, score, comment);
        
        // Add to driver
        rating.rateDriver();
        
        // Save
        ratingRepository.save(rating);
        
        // Notify driver
        String message = passenger.getFirstName() + " rated your trip from " +
            trip.getDepartureCity() + " to " + trip.getArrivalCity() + ": " +
            rating.getStars();
        
        if (comment != null && !comment.isEmpty()) {
            message += "\nComment: \"" + comment + "\"";
        }
        
        notificationService.sendEmail(driver, "New Rating Received", message);
        
        return rating;
    }
    
    /**
     * Gets all ratings for a driver.
     */
    public List<Rating> getDriverRatings(Driver driver) {
        return ratingRepository.findByDriver(driver);
    }
    
    /**
     * Gets average rating for a driver.
     */
    public double getDriverAverageRating(Driver driver) {
        return ratingRepository.getAverageRating(driver);
    }
    
    /**
     * Checks if passenger has already rated a trip.
     */
    public boolean hasPassengerRatedTrip(Passenger passenger, Trip trip) {
        return ratingRepository.hasRated(passenger, trip);
    }
    
    /**
     * Gets a specific rating.
     */
    public Rating getRatingById(String id) {
        return ratingRepository.findById(id);
    }
    
    /**
     * Gets all ratings.
     */
    public List<Rating> getAllRatings() {
        return ratingRepository.findAll();
    }
}
