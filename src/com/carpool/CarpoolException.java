package com.carpool.exception;

/**
 * Base exception for carpooling platform.
 */
public class CarpoolException extends Exception {
    
    public CarpoolException(String message) {
        super(message);
    }
    
    public CarpoolException(String message, Throwable cause) {
        super(message, cause);
    }
}
