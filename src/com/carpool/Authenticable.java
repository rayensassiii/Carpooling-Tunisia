package com.carpool.model;

/**
 * Interface for entities that can authenticate.
 */
public interface Authenticable {
    /**
     * Authenticates the user with the given password.
     * @param password The password to verify
     * @return true if authentication successful, false otherwise
     */
    boolean authenticate(String password);
    
    /**
     * Logs out the user.
     */
    void logout();
}
