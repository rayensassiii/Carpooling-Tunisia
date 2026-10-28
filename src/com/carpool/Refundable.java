package com.carpool.model;

/**
 * Interface for entities that support refunds.
 */
public interface Refundable {
    /**
     * Calculates the refund amount based on hours before departure.
     * @param hoursBeforeDeparture Hours remaining before trip departure
     * @return The refund amount
     */
    double calculateRefund(long hoursBeforeDeparture);
}
