package com.carpool.service;

import com.carpool.exception.*;
import com.carpool.model.*;
import com.carpool.repository.*;
import java.util.List;

/**
 * Service for payment operations.
 * Handles authorization, capture, and refunds.
 */
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    
    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }
    
    /**
     * Authorizes a payment (holds funds without capturing).
     */
    public Payment authorizePayment(Reservation reservation, PaymentMethod paymentMethod) 
            throws PaymentException {
        
        if (reservation == null) {
            throw new PaymentException("Reservation is required");
        }
        
        if (paymentMethod == null) {
            throw PaymentException.invalidPaymentMethod();
        }
        
        if (!paymentMethod.isValid()) {
            throw PaymentException.invalidPaymentMethod();
        }
        
        Payment payment = new Payment(reservation, paymentMethod, reservation.getTotalPrice());
        
        if (!payment.authorize()) {
            throw PaymentException.authorizationFailed();
        }
        
        paymentRepository.save(payment);
        
        return payment;
    }
    
    /**
     * Captures an authorized payment.
     */
    public boolean capturePayment(Payment payment) throws PaymentException {
        if (payment == null) {
            throw PaymentException.notFound();
        }
        
        if (!payment.isAuthorized()) {
            throw new PaymentException("Payment must be authorized before capture");
        }
        
        if (payment.isCaptured()) {
            return true; // Already captured
        }
        
        if (!payment.capture()) {
            throw PaymentException.captureFailed();
        }
        
        paymentRepository.save(payment);
        
        return true;
    }
    
    /**
     * Performs full payment (authorize + capture).
     */
    public Payment pay(Reservation reservation, PaymentMethod paymentMethod) 
            throws PaymentException {
        
        Payment payment = authorizePayment(reservation, paymentMethod);
        
        if (!capturePayment(payment)) {
            throw PaymentException.captureFailed();
        }
        
        return payment;
    }
    
    /**
     * Refunds a payment (full or partial).
     */
    public boolean refund(Payment payment, double amount) throws PaymentException {
        if (payment == null) {
            throw PaymentException.notFound();
        }
        
        if (payment.isRefunded()) {
            throw PaymentException.alreadyRefunded();
        }
        
        if (!payment.isCaptured() && !payment.isPartiallyRefunded()) {
            throw new PaymentException("Can only refund captured payments");
        }
        
        if (amount <= 0) {
            throw new PaymentException("Refund amount must be positive");
        }
        
        if (amount > payment.getNetAmount()) {
            amount = payment.getNetAmount();
        }
        
        if (!payment.refund(amount)) {
            throw PaymentException.refundFailed();
        }
        
        paymentRepository.save(payment);
        
        return true;
    }
    
    /**
     * Performs full refund.
     */
    public boolean fullRefund(Payment payment) throws PaymentException {
        return refund(payment, payment.getAmount());
    }
    
    /**
     * Gets payment by ID.
     */
    public Payment getPaymentById(String id) throws PaymentException {
        Payment payment = paymentRepository.findById(id);
        if (payment == null) {
            throw PaymentException.notFound();
        }
        return payment;
    }
    
    /**
     * Gets all payments.
     */
    public List<Payment> getPayments() {
        return paymentRepository.findAll();
    }
    
    /**
     * Gets payment for a reservation.
     */
    public Payment getPaymentForReservation(Reservation reservation) {
        return paymentRepository.findByReservation(reservation);
    }
    
    /**
     * Gets total captured revenue.
     */
    public double getTotalRevenue() {
        return paymentRepository.getTotalCaptured();
    }
    
    /**
     * Gets payments by status.
     */
    public List<Payment> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status);
    }
}
