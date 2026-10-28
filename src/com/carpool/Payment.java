package com.carpool.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a payment for a reservation.
 * Supports authorization, capture, and refund workflows.
 */
public class Payment {
    
    private final String id;
    private final Reservation reservation;
    private final PaymentMethod paymentMethod;
    private final double amount;
    private PaymentStatus status;
    private LocalDateTime authorizedAt;
    private LocalDateTime capturedAt;
    private LocalDateTime refundedAt;
    private double refundAmount;
    private String transactionId;
    private String errorMessage;
    
    /**
     * Creates a new Payment.
     */
    public Payment(Reservation reservation, PaymentMethod paymentMethod, double amount) {
        this.id = UUID.randomUUID().toString();
        this.reservation = Objects.requireNonNull(reservation, "Reservation cannot be null");
        this.paymentMethod = Objects.requireNonNull(paymentMethod, "Payment method cannot be null");
        this.amount = amount;
        this.status = PaymentStatus.AUTHORIZED;
        this.refundAmount = 0;
        this.transactionId = generateTransactionId();
    }
    
    // ==================== Payment Operations ====================
    
    /**
     * Authorizes the payment (holds the funds).
     */
    public boolean authorize() {
        if (!paymentMethod.isValid()) {
            this.status = PaymentStatus.FAILED;
            this.errorMessage = "Invalid payment method";
            return false;
        }
        
        this.status = PaymentStatus.AUTHORIZED;
        this.authorizedAt = LocalDateTime.now();
        return true;
    }
    
    /**
     * Captures the authorized payment (transfers the funds).
     */
    public boolean capture() {
        if (this.status != PaymentStatus.AUTHORIZED) {
            this.errorMessage = "Payment must be authorized before capture";
            return false;
        }
        
        this.status = PaymentStatus.CAPTURED;
        this.capturedAt = LocalDateTime.now();
        return true;
    }
    
    /**
     * Performs both authorization and capture (pay).
     */
    public boolean pay() {
        if (authorize()) {
            return capture();
        }
        return false;
    }
    
    /**
     * Refunds the full payment amount.
     */
    public boolean refund(double refundAmount) {
        if (this.status != PaymentStatus.CAPTURED && this.status != PaymentStatus.PARTIAL_REFUND) {
            this.errorMessage = "Can only refund captured payments";
            return false;
        }
        
        if (refundAmount > (amount - this.refundAmount)) {
            refundAmount = amount - this.refundAmount;
        }
        
        this.refundAmount += refundAmount;
        
        if (this.refundAmount >= amount) {
            this.status = PaymentStatus.REFUNDED;
        } else {
            this.status = PaymentStatus.PARTIAL_REFUND;
        }
        
        this.refundedAt = LocalDateTime.now();
        return true;
    }
    
    /**
     * Performs a partial refund.
     */
    public boolean partialRefund(double amount) {
        return refund(amount);
    }
    
    /**
     * Refunds the full amount.
     */
    public boolean fullRefund() {
        return refund(this.amount);
    }
    
    // ==================== Getters ====================
    
    public String getId() {
        return id;
    }
    
    public Reservation getReservation() {
        return reservation;
    }
    
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public PaymentStatus getStatus() {
        return status;
    }
    
    public LocalDateTime getAuthorizedAt() {
        return authorizedAt;
    }
    
    public LocalDateTime getCapturedAt() {
        return capturedAt;
    }
    
    public LocalDateTime getRefundedAt() {
        return refundedAt;
    }
    
    public double getRefundAmount() {
        return refundAmount;
    }
    
    public double getNetAmount() {
        return amount - refundAmount;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public boolean isAuthorized() {
        return status == PaymentStatus.AUTHORIZED || 
               status == PaymentStatus.CAPTURED;
    }
    
    public boolean isCaptured() {
        return status == PaymentStatus.CAPTURED || 
               status == PaymentStatus.PARTIAL_REFUND;
    }
    
    public boolean isRefunded() {
        return status == PaymentStatus.REFUNDED;
    }
    
    public boolean isPartiallyRefunded() {
        return status == PaymentStatus.PARTIAL_REFUND;
    }
    
    public boolean isFailed() {
        return status == PaymentStatus.FAILED;
    }
    
    // ==================== Helper Methods ====================
    
    /**
     * Generates a unique transaction ID.
     */
    private String generateTransactionId() {
        return "TXN-" + System.currentTimeMillis() + "-" + 
               id.substring(0, 8).toUpperCase();
    }
    
    // ==================== Object Methods ====================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payment payment = (Payment) o;
        return Objects.equals(id, payment.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Payment{" +
                "id='" + id + '\'' +
                ", amount=" + amount + "€" +
                ", status=" + status +
                ", refund=" + refundAmount + "€" +
                ", txn='" + transactionId + '\'' +
                '}';
    }
}
