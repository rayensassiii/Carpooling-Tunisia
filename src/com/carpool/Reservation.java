package com.carpool.model;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a reservation made by a passenger for a trip.
 */
public class Reservation {
    
    private final String id;
    private final Passenger passenger;
    private final Trip trip;
    private final int seatsBooked;
    private final double totalPrice;
    private ReservationStatus status;
    private Payment payment;
    private final LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    
    /**
     * Creates a new Reservation.
     */
    public Reservation(Passenger passenger, Trip trip, int seatsBooked) {
        this.id = UUID.randomUUID().toString();
        this.passenger = Objects.requireNonNull(passenger, "Passenger cannot be null");
        this.trip = Objects.requireNonNull(trip, "Trip cannot be null");
        this.seatsBooked = seatsBooked > 0 ? seatsBooked : 1;
        this.totalPrice = trip.getPricePerSeat() * this.seatsBooked;
        this.status = ReservationStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }
    
    // ==================== Reservation Lifecycle ====================
    
    /**
     * Creates the reservation (sets it as pending).
     */
    public void createReservation() {
        this.status = ReservationStatus.PENDING;
    }
    
    /**
     * Confirms the reservation.
     */
    public void confirmReservation() {
        if (this.status == ReservationStatus.PENDING) {
            this.status = ReservationStatus.CONFIRMED;
            this.confirmedAt = LocalDateTime.now();
        }
    }
    
    /**
     * Cancels the reservation.
     */
    public void cancelReservation() {
        if (this.status != ReservationStatus.COMPLETED) {
            this.status = ReservationStatus.CANCELLED;
            this.cancelledAt = LocalDateTime.now();
        }
    }
    
    /**
     * Cancels with a reason.
     */
    public void cancelReservation(String reason) {
        cancelReservation();
        this.cancellationReason = reason;
    }
    
    /**
     * Completes the reservation (after trip is completed).
     */
    public void complete() {
        if (this.status == ReservationStatus.CONFIRMED) {
            this.status = ReservationStatus.COMPLETED;
        }
    }
    
    /**
     * Refunds the reservation.
     */
    public void refundReservation(double refundAmount) {
        if (payment != null && refundAmount > 0) {
            if (refundAmount >= totalPrice) {
                payment.refund(totalPrice);
            } else {
                payment.partialRefund(refundAmount);
            }
        }
    }
    
    // ==================== Business Rules ====================
    
    /**
     * Calculates the refund amount based on cancellation time.
     * Business rules:
     * - > 24h before departure: 100% refund
     * - < 24h before departure: 50% refund
     */
    public double calculateRefundAmount() {
        long hoursUntilDeparture = getHoursUntilDeparture();
        
        if (hoursUntilDeparture >= 24) {
            return totalPrice; // Full refund
        } else if (hoursUntilDeparture >= 0) {
            return totalPrice * 0.5; // 50% refund
        } else {
            return 0; // No refund after departure
        }
    }
    
    /**
     * Returns hours until trip departure.
     */
    public long getHoursUntilDeparture() {
        return ChronoUnit.HOURS.between(LocalDateTime.now(), trip.getDepartureTime());
    }
    
    /**
     * Checks if cancellation is within 24 hours of departure.
     */
    public boolean isWithin24Hours() {
        return getHoursUntilDeparture() < 24;
    }
    
    // ==================== Getters ====================
    
    public String getId() {
        return id;
    }
    
    public Passenger getPassenger() {
        return passenger;
    }
    
    public Trip getTrip() {
        return trip;
    }
    
    public int getSeatsBooked() {
        return seatsBooked;
    }
    
    public double getTotalPrice() {
        return totalPrice;
    }
    
    public ReservationStatus getStatus() {
        return status;
    }
    
    public Payment getPayment() {
        return payment;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }
    
    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }
    
    public String getCancellationReason() {
        return cancellationReason;
    }
    
    public boolean isConfirmed() {
        return status == ReservationStatus.CONFIRMED;
    }
    
    public boolean isCancelled() {
        return status == ReservationStatus.CANCELLED;
    }
    
    public boolean isPending() {
        return status == ReservationStatus.PENDING;
    }
    
    public boolean isCompleted() {
        return status == ReservationStatus.COMPLETED;
    }
    
    // ==================== Setters ====================
    
    public void setPayment(Payment payment) {
        this.payment = payment;
    }
    
    public void setStatus(ReservationStatus status) {
        this.status = status;
    }
    
    // ==================== Object Methods ====================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Reservation{" +
                "id='" + id + '\'' +
                ", passenger=" + passenger.getFullName() +
                ", trip=" + trip.getRoute() +
                ", seats=" + seatsBooked +
                ", price=" + totalPrice + "€" +
                ", status=" + status +
                '}';
    }
}
