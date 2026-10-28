package com.carpool.model;

/**
 * Represents the type of vehicle.
 */
public enum VehicleType {
    SEDAN("Sedan", 4),
    SUV("SUV", 5),
    MINIVAN("Minivan", 7),
    COUPE("Coupe", 2),
    HATCHBACK("Hatchback", 4),
    PICKUP("Pickup", 4);

    private final String displayName;
    private final int typicalSeats;

    VehicleType(String displayName, int typicalSeats) {
        this.displayName = displayName;
        this.typicalSeats = typicalSeats;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getTypicalSeats() {
        return typicalSeats;
    }
}
