package com.carpool.model;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a carpooling trip.
 * Implements Refundable for handling cancellation refunds.
 */
public class Trip implements Refundable {
    
    private final String id;
    private final Driver driver;
    private final Vehicle vehicle;
    private String departureCity;
    private String arrivalCity;
    private String departureAddress;
    private String arrivalAddress;
    private LocalDateTime departureTime;
    private LocalDateTime estimatedArrivalTime;
    private int totalSeats;
    private int availableSeats;
    private double pricePerSeat;
    private TripStatus status;
    private final List<Reservation> reservations;
    private String description;
    private final LocalDateTime createdAt;
    private boolean smokingAllowed;
    private boolean petsAllowed;
    private boolean luggageAllowed;
    
    /**
     * Creates a new Trip.
     */
    public Trip(Driver driver, Vehicle vehicle, String departureCity, 
                String arrivalCity, LocalDateTime departureTime, 
                int availableSeats, double pricePerSeat) {
        this.id = UUID.randomUUID().toString();
        this.driver = Objects.requireNonNull(driver, "Driver cannot be null");
        this.vehicle = Objects.requireNonNull(vehicle, "Vehicle cannot be null");
        this.departureCity = Objects.requireNonNull(departureCity, "Departure city cannot be null");
        this.arrivalCity = Objects.requireNonNull(arrivalCity, "Arrival city cannot be null");
        this.departureTime = Objects.requireNonNull(departureTime, "Departure time cannot be null");
        this.totalSeats = availableSeats;
        this.availableSeats = availableSeats;
        this.pricePerSeat = pricePerSeat;
        this.status = TripStatus.OPEN;
        this.reservations = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.smokingAllowed = false;
        this.petsAllowed = false;
        this.luggageAllowed = true;
    }
    
    // ==================== Trip Lifecycle ====================
    
    /**
     * Creates the trip (sets it as open).
     */
    public void createTrip() {
        this.status = TripStatus.OPEN;
    }
    
    /**
     * Closes the trip (no more reservations accepted).
     */
    public void closeTrip() {
        if (this.status == TripStatus.OPEN || this.status == TripStatus.FULL) {
            this.status = TripStatus.IN_PROGRESS;
        }
    }
    
    /**
     * Marks the trip as completed.
     */
    public void completeTrip() {
        this.status = TripStatus.COMPLETED;
        // Mark all confirmed reservations as completed
        for (Reservation r : reservations) {
            if (r.getStatus() == ReservationStatus.CONFIRMED) {
                r.complete();
            }
        }
    }
    
    /**
     * Cancels the trip if allowed.
     * @return true if cancelled successfully, false if not allowed
     */
    public boolean cancelTrip() {
        if (!canCancel()) {
            return false;
        }
        this.status = TripStatus.CANCELLED;
        return true;
    }
    
    // ==================== Passenger Management ====================
    
    /**
     * Adds a passenger via reservation.
     * @param reservation The reservation to add
     * @return true if added successfully
     */
    public boolean addPassenger(Reservation reservation) {
        if (reservation == null) {
            return false;
        }
        
        if (status != TripStatus.OPEN) {
            return false;
        }
        
        if (reservation.getSeatsBooked() > availableSeats) {
            return false;
        }
        
        reservations.add(reservation);
        availableSeats -= reservation.getSeatsBooked();
        
        // Check if trip is now full
        if (availableSeats == 0) {
            status = TripStatus.FULL;
        }
        
        return true;
    }

    /**
     * Restores a reservation from persistent storage without changing trip capacity.
     */
    public void restoreReservation(Reservation reservation) {
        if (reservation != null && !reservations.contains(reservation)) {
            reservations.add(reservation);
        }
    }
    
    /**
     * Removes a passenger via reservation.
     * @param reservation The reservation to remove
     * @return true if removed successfully
     */
    public boolean removePassenger(Reservation reservation) {
        if (reservation == null || !reservations.contains(reservation)) {
            return false;
        }
        
        reservations.remove(reservation);
        availableSeats += reservation.getSeatsBooked();
        
        // Reopen trip if it was full
        if (status == TripStatus.FULL && availableSeats > 0) {
            status = TripStatus.OPEN;
        }
        
        return true;
    }
    
    // ==================== Business Rules ====================
    
    /**
     * Checks if the trip has any reservations.
     */
    public boolean hasReservations() {
        return !reservations.isEmpty();
    }
    
    /**
     * Checks if the trip has any confirmed (non-cancelled) reservations.
     */
    public boolean hasActiveReservations() {
        for (Reservation r : reservations) {
            if (r.getStatus() == ReservationStatus.CONFIRMED || 
                r.getStatus() == ReservationStatus.PENDING) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Driver can cancel only if no reservations exist.
     */
    public boolean canCancel() {
        return !hasActiveReservations() && 
               (status == TripStatus.OPEN || status == TripStatus.FULL);
    }
    
    /**
     * Returns hours until departure.
     */
    public long getHoursUntilDeparture() {
        return ChronoUnit.HOURS.between(LocalDateTime.now(), departureTime);
    }
    
    /**
     * Checks if departure is within 24 hours.
     */
    public boolean isWithin24Hours() {
        return getHoursUntilDeparture() < 24;
    }
    
    /**
     * Calculates refund based on time before departure.
     * Business rule:
     * - > 24h before: 100% refund
     * - < 24h before: 50% refund
     */
    @Override
    public double calculateRefund(long hoursBeforeDeparture) {
        if (hoursBeforeDeparture >= 24) {
            return pricePerSeat; // Full refund
        } else {
            return pricePerSeat * 0.5; // Partial refund
        }
    }
    
    /**
     * Calculates compensation for passengers if driver cancels < 24h.
     * Business rule: 20% compensation per seat
     */
    public double calculateCompensation() {
        if (isWithin24Hours()) {
            return pricePerSeat * 0.2;
        }
        return 0;
    }
    
    // ==================== Getters ====================
    
    public String getId() {
        return id;
    }
    
    public Driver getDriver() {
        return driver;
    }
    
    public Vehicle getVehicle() {
        return vehicle;
    }
    
    public String getDepartureCity() {
        return departureCity;
    }
    
    public String getArrivalCity() {
        return arrivalCity;
    }
    
    public String getDepartureAddress() {
        return departureAddress;
    }
    
    public String getArrivalAddress() {
        return arrivalAddress;
    }
    
    public LocalDateTime getDepartureTime() {
        return departureTime;
    }
    
    public LocalDateTime getEstimatedArrivalTime() {
        return estimatedArrivalTime;
    }
    
    public int getTotalSeats() {
        return totalSeats;
    }
    
    public int getAvailableSeats() {
        return availableSeats;
    }
    
    public int getBookedSeats() {
        return totalSeats - availableSeats;
    }
    
    public double getPricePerSeat() {
        return pricePerSeat;
    }
    
    public TripStatus getStatus() {
        return status;
    }
    
    /**
     * Returns a defensive copy of reservations.
     */
    public List<Reservation> getReservations() {
        return new ArrayList<>(reservations);
    }
    
    /**
     * Returns confirmed reservations only.
     */
    public List<Reservation> getConfirmedReservations() {
        List<Reservation> confirmed = new ArrayList<>();
        for (Reservation r : reservations) {
            if (r.getStatus() == ReservationStatus.CONFIRMED) {
                confirmed.add(r);
            }
        }
        return confirmed;
    }
    
    public String getDescription() {
        return description;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public boolean isSmokingAllowed() {
        return smokingAllowed;
    }
    
    public boolean isPetsAllowed() {
        return petsAllowed;
    }
    
    public boolean isLuggageAllowed() {
        return luggageAllowed;
    }
    
    public boolean isFull() {
        return availableSeats == 0 || status == TripStatus.FULL;
    }
    
    public boolean isOpen() {
        return status == TripStatus.OPEN;
    }
    
    public boolean isCancelled() {
        return status == TripStatus.CANCELLED;
    }
    
    public boolean isCompleted() {
        return status == TripStatus.COMPLETED;
    }
    
    /**
     * Returns route description.
     */
    public String getRoute() {
        return departureCity + " → " + arrivalCity;
    }
    
    // ==================== Setters ====================
    
    public void setDepartureCity(String departureCity) {
        this.departureCity = departureCity;
    }
    
    public void setArrivalCity(String arrivalCity) {
        this.arrivalCity = arrivalCity;
    }
    
    public void setDepartureAddress(String departureAddress) {
        this.departureAddress = departureAddress;
    }
    
    public void setArrivalAddress(String arrivalAddress) {
        this.arrivalAddress = arrivalAddress;
    }
    
    public void setDepartureTime(LocalDateTime departureTime) {
        this.departureTime = departureTime;
    }
    
    public void setEstimatedArrivalTime(LocalDateTime estimatedArrivalTime) {
        this.estimatedArrivalTime = estimatedArrivalTime;
    }
    
    public void setPricePerSeat(double pricePerSeat) {
        this.pricePerSeat = Math.max(0, pricePerSeat);
    }
    
    public void setStatus(TripStatus status) {
        this.status = status;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setSmokingAllowed(boolean smokingAllowed) {
        this.smokingAllowed = smokingAllowed;
    }
    
    public void setPetsAllowed(boolean petsAllowed) {
        this.petsAllowed = petsAllowed;
    }
    
    public void setLuggageAllowed(boolean luggageAllowed) {
        this.luggageAllowed = luggageAllowed;
    }
    
    // ==================== Object Methods ====================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Trip trip = (Trip) o;
        return Objects.equals(id, trip.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Trip{" +
                "id='" + id + '\'' +
                ", route='" + getRoute() + '\'' +
                ", departure=" + departureTime +
                ", seats=" + availableSeats + "/" + totalSeats +
                ", price=" + pricePerSeat + "€" +
                ", status=" + status +
                '}';
    }
}
