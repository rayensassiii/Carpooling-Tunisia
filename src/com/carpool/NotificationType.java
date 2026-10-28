package com.carpool.model;

/**
 * Represents the type of notification delivery.
 */
public enum NotificationType {
    EMAIL("Email"),
    SMS("SMS"),
    BOTH("Email & SMS");

    private final String displayName;

    NotificationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
