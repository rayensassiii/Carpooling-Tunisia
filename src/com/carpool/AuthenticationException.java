package com.carpool.exception;

/**
 * Exception thrown for authentication-related errors.
 */
public class AuthenticationException extends CarpoolException {
    
    public AuthenticationException(String message) {
        super(message);
    }
    
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static AuthenticationException invalidCredentials() {
        return new AuthenticationException("Invalid email or password");
    }
    
    public static AuthenticationException accountBlocked() {
        return new AuthenticationException("Account is blocked. Please contact support.");
    }
    
    public static AuthenticationException accountSuspended() {
        return new AuthenticationException("Account is suspended. Please contact support.");
    }
    
    public static AuthenticationException emailAlreadyExists() {
        return new AuthenticationException("An account with this email already exists");
    }
    
    public static AuthenticationException notLoggedIn() {
        return new AuthenticationException("You must be logged in to perform this action");
    }
}
