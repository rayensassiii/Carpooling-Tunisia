package com.carpool.model;

/**
 * Represents the status of a user account.
 */
public enum UserStatus {
    ACTIVE("Active"),
    SUSPENDED("Suspended"),
    BLOCKED("Blocked");

    private final String displayName;

    UserStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
