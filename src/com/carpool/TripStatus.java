package com.carpool.model;

/**
 * Represents the status of a trip.
 */
public enum TripStatus {
    OPEN("Open - Accepting reservations"),
    FULL("Full - No seats available"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    private final String displayName;

    TripStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
