package com.carpool.exception;

/**
 * Exception thrown for payment-related errors.
 */
public class PaymentException extends CarpoolException {
    
    public PaymentException(String message) {
        super(message);
    }
    
    public PaymentException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static PaymentException notFound() {
        return new PaymentException("Payment not found");
    }
    
    public static PaymentException authorizationFailed() {
        return new PaymentException("Payment authorization failed");
    }
    
    public static PaymentException captureFailed() {
        return new PaymentException("Payment capture failed");
    }
    
    public static PaymentException refundFailed() {
        return new PaymentException("Refund failed");
    }
    
    public static PaymentException invalidPaymentMethod() {
        return new PaymentException("Invalid or expired payment method");
    }
    
    public static PaymentException insufficientFunds() {
        return new PaymentException("Insufficient funds");
    }
    
    public static PaymentException alreadyRefunded() {
        return new PaymentException("Payment has already been refunded");
    }
}
