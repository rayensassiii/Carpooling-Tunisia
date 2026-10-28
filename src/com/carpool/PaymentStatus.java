package com.carpool.model;

/**
 * Represents the status of a payment.
 */
public enum PaymentStatus {
    AUTHORIZED("Payment authorized"),
    CAPTURED("Payment captured"),
    REFUNDED("Fully refunded"),
    PARTIAL_REFUND("Partially refunded"),
    FAILED("Payment failed");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
