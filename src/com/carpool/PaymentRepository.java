package com.carpool.repository;

import com.carpool.model.*;
import java.util.*;

/**
 * In-memory repository for Payment entities.
 */
public class PaymentRepository {
    
    private final Map<String, Payment> payments;
    
    public PaymentRepository() {
        this.payments = new HashMap<>();
    }
    
    /**
     * Saves a payment.
     */
    public void save(Payment payment) {
        if (payment != null) {
            payments.put(payment.getId(), payment);
        }
    }
    
    /**
     * Finds a payment by ID.
     */
    public Payment findById(String id) {
        return payments.get(id);
    }
    
    /**
     * Returns all payments.
     */
    public List<Payment> findAll() {
        return new ArrayList<>(payments.values());
    }
    
    /**
     * Finds payment by reservation.
     */
    public Payment findByReservation(Reservation reservation) {
        for (Payment p : payments.values()) {
            if (p.getReservation().equals(reservation)) {
                return p;
            }
        }
        return null;
    }
    
    /**
     * Finds payments by status.
     */
    public List<Payment> findByStatus(PaymentStatus status) {
        List<Payment> result = new ArrayList<>();
        for (Payment p : payments.values()) {
            if (p.getStatus() == status) {
                result.add(p);
            }
        }
        return result;
    }
    
    /**
     * Returns total captured amount.
     */
    public double getTotalCaptured() {
        double total = 0;
        for (Payment p : payments.values()) {
            if (p.isCaptured()) {
                total += p.getNetAmount();
            }
        }
        return total;
    }
    
    /**
     * Returns count of payments.
     */
    public int count() {
        return payments.size();
    }
    
    /**
     * Clears all payments.
     */
    public void clear() {
        payments.clear();
    }
}
