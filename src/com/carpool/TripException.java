package com.carpool.exception;

/**
 * Exception thrown for trip-related errors.
 */
public class TripException extends CarpoolException {
    
    public TripException(String message) {
        super(message);
    }
    
    public TripException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static TripException notFound() {
        return new TripException("Trip not found");
    }
    
    public static TripException noSeatsAvailable() {
        return new TripException("No seats available on this trip");
    }
    
    public static TripException tripNotOpen() {
        return new TripException("This trip is not accepting reservations");
    }
    
    public static TripException tripAlreadyCompleted() {
        return new TripException("This trip has already been completed");
    }
    
    public static TripException tripCancelled() {
        return new TripException("This trip has been cancelled");
    }
    
    public static TripException cannotCancel() {
        return new TripException("Cannot cancel trip with existing reservations");
    }
    
    public static TripException invalidDepartureTime() {
        return new TripException("Departure time must be in the future");
    }
    
    public static TripException driverNotVerified() {
        return new TripException("Driver must be verified to create trips");
    }
}
