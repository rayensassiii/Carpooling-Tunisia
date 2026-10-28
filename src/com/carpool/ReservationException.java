package com.carpool.exception;

/**
 * Exception thrown for reservation-related errors.
 */
public class ReservationException extends CarpoolException {
    
    public ReservationException(String message) {
        super(message);
    }
    
    public ReservationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static ReservationException notFound() {
        return new ReservationException("Reservation not found");
    }
    
    public static ReservationException alreadyConfirmed() {
        return new ReservationException("Reservation is already confirmed");
    }
    
    public static ReservationException alreadyCancelled() {
        return new ReservationException("Reservation is already cancelled");
    }
    
    public static ReservationException cannotCancel() {
        return new ReservationException("Cannot cancel this reservation");
    }
    
    public static ReservationException invalidSeats() {
        return new ReservationException("Invalid number of seats requested");
    }
    
    public static ReservationException paymentRequired() {
        return new ReservationException("Payment is required to confirm reservation");
    }
    
    public static ReservationException duplicateReservation() {
        return new ReservationException("You already have a reservation for this trip");
    }
}
