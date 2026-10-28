package com.carpool.model;

/**
 * Represents the status of a reservation.
 */
public enum ReservationStatus {
    PENDING("Pending confirmation"),
    CONFIRMED("Confirmed"),
    CANCELLED("Cancelled"),
    COMPLETED("Completed");

    private final String displayName;

    ReservationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
