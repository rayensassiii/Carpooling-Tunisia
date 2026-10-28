package com.carpool.service;

import com.carpool.exception.*;
import com.carpool.model.*;
import com.carpool.repository.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for trip management.
 * Handles creation, search, and lifecycle of trips.
 */
public class TripService {
    
    private final TripRepository tripRepository;
    private final ReservationRepository reservationRepository;
    private final NotificationService notificationService;
    
    public TripService(TripRepository tripRepository, ReservationRepository reservationRepository,
                       NotificationService notificationService) {
        this.tripRepository = tripRepository;
        this.reservationRepository = reservationRepository;
        this.notificationService = notificationService;
    }
    
    /**
     * Creates a new trip.
     */
    public Trip createTrip(Driver driver, Vehicle vehicle, String departureCity, 
                           String arrivalCity, LocalDateTime departureTime, 
                           int availableSeats, double pricePerSeat) throws TripException {
        
        // Validate driver
        if (driver == null) {
            throw new TripException("Driver is required");
        }

        if (!driver.isVerified()) {
            throw TripException.driverNotVerified();
        }
        
        // Validate vehicle
        if (vehicle == null) {
            throw new TripException("Vehicle is required");
        }
        
        // Validate departure time is in the future
        if (departureTime == null || departureTime.isBefore(LocalDateTime.now())) {
            throw TripException.invalidDepartureTime();
        }
        
        // Validate cities
        if (departureCity == null || departureCity.trim().isEmpty()) {
            throw new TripException("Departure city is required");
        }
        
        if (arrivalCity == null || arrivalCity.trim().isEmpty()) {
            throw new TripException("Arrival city is required");
        }
        
        // Validate seats
        if (availableSeats <= 0) {
            throw new TripException("At least 1 seat must be available");
        }
        
        if (availableSeats > vehicle.getSeats()) {
            throw new TripException("Cannot offer more seats than vehicle capacity");
        }
        
        // Validate price
        if (pricePerSeat < 0) {
            throw new TripException("Price cannot be negative");
        }
        
        // Create trip
        Trip trip = new Trip(driver, vehicle, departureCity, arrivalCity, 
                            departureTime, availableSeats, pricePerSeat);
        trip.createTrip();
        
        // Add to driver's trips
        driver.addTrip(trip);
        
        // Save
        tripRepository.save(trip);
        
        return trip;
    }
    
    /**
     * Gets all trips.
     */
    public List<Trip> getTrips() {
        return tripRepository.findAll();
    }
    
    /**
     * Gets trip by ID.
     */
    public Trip getTripById(String id) throws TripException {
        Trip trip = tripRepository.findById(id);
        if (trip == null) {
            throw TripException.notFound();
        }
        return trip;
    }
    
    /**
     * Searches trips by route and date.
     */
    public List<Trip> searchTrips(String departureCity, String arrivalCity, LocalDate date) {
        return tripRepository.search(departureCity, arrivalCity, date);
    }
    
    /**
     * Filters trips by price and seats.
     */
    public List<Trip> filterTrips(double maxPrice, int minSeats) {
        return tripRepository.filter(maxPrice, minSeats);
    }
    
    /**
     * Gets available (open) trips.
     */
    public List<Trip> getAvailableTrips() {
        List<Trip> available = tripRepository.findAvailable();
        // Sort by departure time
        available.sort(Comparator.comparing(Trip::getDepartureTime));
        return available;
    }
    
    /**
     * Gets trips by driver.
     */
    public List<Trip> getDriverTrips(Driver driver) {
        return tripRepository.findByDriver(driver);
    }
    
    /**
     * Closes a trip (no more reservations).
     */
    public void closeTrip(Trip trip) throws TripException {
        if (trip == null) {
            throw TripException.notFound();
        }
        
        if (trip.isCancelled()) {
            throw TripException.tripCancelled();
        }
        
        trip.closeTrip();
        tripRepository.save(trip);
        
        // Notify passengers
        for (Reservation r : trip.getConfirmedReservations()) {
            notificationService.sendNotification(r.getPassenger(), 
                "Trip Starting Soon", 
                "Your trip from " + trip.getDepartureCity() + " to " + 
                trip.getArrivalCity() + " is starting soon!",
                NotificationType.BOTH);
        }
    }
    
    /**
     * Cancels a trip (only if no reservations).
     */
    public boolean cancelTrip(Trip trip) throws TripException {
        if (trip == null) {
            throw TripException.notFound();
        }
        
        if (!trip.canCancel()) {
            throw TripException.cannotCancel();
        }
        
        boolean cancelled = trip.cancelTrip();
        
        if (cancelled) {
            tripRepository.save(trip);
            
            // Notify driver
            notificationService.sendEmail(trip.getDriver(), 
                "Trip Cancelled", 
                "Your trip from " + trip.getDepartureCity() + " to " + 
                trip.getArrivalCity() + " has been cancelled.");
        }
        
        return cancelled;
    }
    
    /**
     * Force cancels a trip with passenger compensation.
     * Used when driver cancels with existing reservations (< 24h).
     */
    public void forceCancelTrip(Trip trip, ReservationService reservationService) 
            throws TripException, ReservationException {
        if (trip == null) {
            throw TripException.notFound();
        }
        
        // Calculate compensation if within 24 hours
        double compensation = trip.calculateCompensation();
        
        // Cancel all reservations with refund
        for (Reservation r : trip.getReservations()) {
            if (r.getStatus() == ReservationStatus.CONFIRMED || 
                r.getStatus() == ReservationStatus.PENDING) {
                
                // Full refund + compensation
                double refundAmount = r.getTotalPrice();
                if (trip.isWithin24Hours()) {
                    refundAmount += compensation * r.getSeatsBooked();
                }
                
                r.cancelReservation("Trip cancelled by driver");
                r.refundReservation(r.getTotalPrice());
                reservationRepository.save(r);
                
                // Notify passenger about cancellation and compensation
                String message = "The trip from " + trip.getDepartureCity() + " to " + 
                    trip.getArrivalCity() + " has been cancelled by the driver. " +
                    "You will receive a full refund of " + r.getTotalPrice() + "€";
                
                if (trip.isWithin24Hours()) {
                    message += " plus " + (compensation * r.getSeatsBooked()) + 
                        "€ compensation for late cancellation.";
                }
                
                notificationService.sendNotification(r.getPassenger(), 
                    "Trip Cancelled - Refund Issued", message, NotificationType.BOTH);
            }
        }
        
        trip.setStatus(TripStatus.CANCELLED);
        tripRepository.save(trip);
    }
    
    /**
     * Completes a trip.
     */
    public void completeTrip(Trip trip) throws TripException {
        if (trip == null) {
            throw TripException.notFound();
        }
        
        if (trip.isCancelled()) {
            throw TripException.tripCancelled();
        }

        if (trip.isCompleted()) {
            throw TripException.tripAlreadyCompleted();
        }
        
        trip.completeTrip();
        for (Reservation r : trip.getReservations()) {
            reservationRepository.save(r);
        }
        tripRepository.save(trip);
        
        // Notify all passengers
        for (Reservation r : trip.getConfirmedReservations()) {
            notificationService.sendEmail(r.getPassenger(), 
                "Trip Completed", 
                "Your trip from " + trip.getDepartureCity() + " to " + 
                trip.getArrivalCity() + " is complete. Thank you for using CarPool! " +
                "Please rate your driver.");
        }
        
        // Notify driver
        notificationService.sendEmail(trip.getDriver(), 
            "Trip Completed", 
            "Your trip from " + trip.getDepartureCity() + " to " + 
            trip.getArrivalCity() + " is complete. " +
            "Earnings: " + (trip.getPricePerSeat() * trip.getBookedSeats()) + "€");
    }
    
    /**
     * Updates trip details.
     */
    public void updateTrip(Trip trip, String departureCity, String arrivalCity,
                           LocalDateTime departureTime, double pricePerSeat) 
            throws TripException {
        if (trip == null) {
            throw TripException.notFound();
        }
        
        if (trip.hasActiveReservations()) {
            throw new TripException("Cannot modify trip with existing reservations");
        }
        
        if (departureCity != null && !departureCity.isEmpty()) {
            trip.setDepartureCity(departureCity);
        }
        
        if (arrivalCity != null && !arrivalCity.isEmpty()) {
            trip.setArrivalCity(arrivalCity);
        }
        
        if (departureTime != null) {
            if (departureTime.isBefore(LocalDateTime.now())) {
                throw TripException.invalidDepartureTime();
            }
            trip.setDepartureTime(departureTime);
        }
        
        if (pricePerSeat >= 0) {
            trip.setPricePerSeat(pricePerSeat);
        }
        
        tripRepository.save(trip);
    }
}
