package com.carpool.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a passenger in the carpooling system.
 * A passenger can make reservations and manage payment methods.
 */
public class Passenger extends User {
    
    private final List<Reservation> reservations;
    private final List<PaymentMethod> paymentMethods;
    
    /**
     * Creates a new Passenger.
     */
    public Passenger(String email, String password, String firstName, 
                     String lastName, String phoneNumber) {
        super(email, password, firstName, lastName, phoneNumber);
        this.reservations = new ArrayList<>();
        this.paymentMethods = new ArrayList<>();
    }
    
    // ==================== Reservation Management ====================
    
    /**
     * Adds a reservation to the passenger's list.
     */
    public void addReservation(Reservation reservation) {
        if (reservation != null && !reservations.contains(reservation)) {
            reservations.add(reservation);
        }
    }
    
    /**
     * Removes a reservation from the passenger's list.
     */
    public void removeReservation(Reservation reservation) {
        reservations.remove(reservation);
    }
    
    /**
     * Returns a defensive copy of reservations.
     */
    public List<Reservation> getReservations() {
        return new ArrayList<>(reservations);
    }
    
    /**
     * Returns active (non-cancelled) reservations.
     */
    public List<Reservation> getActiveReservations() {
        List<Reservation> active = new ArrayList<>();
        for (Reservation r : reservations) {
            if (r.getStatus() != ReservationStatus.CANCELLED) {
                active.add(r);
            }
        }
        return active;
    }
    
    /**
     * Returns completed reservations for rating purposes.
     */
    public List<Reservation> getCompletedReservations() {
        List<Reservation> completed = new ArrayList<>();
        for (Reservation r : reservations) {
            if (r.getStatus() == ReservationStatus.COMPLETED) {
                completed.add(r);
            }
        }
        return completed;
    }
    
    // ==================== Payment Method Management ====================
    
    /**
     * Adds a payment method.
     */
    public void addPaymentMethod(PaymentMethod method) {
        if (method != null && !paymentMethods.contains(method)) {
            method.setOwner(this);
            // If this is the first payment method, make it default
            if (paymentMethods.isEmpty()) {
                method.setDefault(true);
            }
            paymentMethods.add(method);
        }
    }

    /**
     * Restores a payment method from persistent storage without changing defaults.
     */
    public void restorePaymentMethod(PaymentMethod method) {
        if (method != null && !paymentMethods.contains(method)) {
            method.setOwner(this);
            paymentMethods.add(method);
        }
    }
    
    /**
     * Removes a payment method.
     */
    public void removePaymentMethod(PaymentMethod method) {
        if (paymentMethods.remove(method)) {
            // If removed method was default and there are others, set first as default
            if (method.isDefault() && !paymentMethods.isEmpty()) {
                paymentMethods.get(0).setDefault(true);
            }
        }
    }
    
    /**
     * Returns a defensive copy of payment methods.
     */
    public List<PaymentMethod> getPaymentMethods() {
        return new ArrayList<>(paymentMethods);
    }
    
    /**
     * Returns the default payment method.
     */
    public PaymentMethod getDefaultPaymentMethod() {
        for (PaymentMethod pm : paymentMethods) {
            if (pm.isDefault()) {
                return pm;
            }
        }
        return paymentMethods.isEmpty() ? null : paymentMethods.get(0);
    }
    
    /**
     * Sets a payment method as default.
     */
    public void setDefaultPaymentMethod(PaymentMethod method) {
        if (paymentMethods.contains(method)) {
            for (PaymentMethod pm : paymentMethods) {
                pm.setDefault(pm.equals(method));
            }
        }
    }
    
    // ==================== User Role ====================
    
    @Override
    public String getRole() {
        return "Passenger";
    }
    
    /**
     * Returns the total amount spent on completed trips.
     */
    public double getTotalSpent() {
        double total = 0;
        for (Reservation r : reservations) {
            if (r.getStatus() == ReservationStatus.COMPLETED && r.getPayment() != null) {
                total += r.getPayment().getAmount();
            }
        }
        return total;
    }
    
    /**
     * Returns the count of completed trips.
     */
    public int getCompletedTripsCount() {
        return getCompletedReservations().size();
    }
}
