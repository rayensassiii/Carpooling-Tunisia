package com.carpool.service;

import com.carpool.exception.*;
import com.carpool.model.*;
import com.carpool.repository.*;

/**
 * Service for authentication and user management.
 * Handles login, registration, and account operations.
 */
public class AuthService {
    
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private User currentUser;
    
    private static final int MAX_FAILED_ATTEMPTS = 3;
    
    public AuthService(UserRepository userRepository, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.currentUser = null;
    }
    
    /**
     * Registers a new passenger.
     */
    public Passenger registerPassenger(String email, String password, String firstName, 
                                        String lastName, String phone) throws AuthenticationException {
        validateRegistration(email, password, firstName, lastName);
        
        Passenger passenger = new Passenger(email, password, firstName, lastName, phone);
        passenger.createAccount();
        userRepository.save(passenger);
        
        // Send welcome notification
        notificationService.sendEmail(passenger, "Welcome to CarPool!", 
            "Hello " + firstName + ", welcome to our carpooling platform!");
        
        return passenger;
    }
    
    /**
     * Registers a new driver.
     */
    public Driver registerDriver(String email, String password, String firstName, 
                                  String lastName, String phone, String licenseNumber) 
                                  throws AuthenticationException {
        validateRegistration(email, password, firstName, lastName);
        
        if (licenseNumber == null || licenseNumber.trim().isEmpty()) {
            throw new AuthenticationException("License number is required for drivers");
        }
        
        Driver driver = new Driver(email, password, firstName, lastName, phone, licenseNumber);
        driver.createAccount();
        userRepository.save(driver);
        
        // Send welcome notification
        notificationService.sendEmail(driver, "Welcome to CarPool!", 
            "Hello " + firstName + ", welcome to our carpooling platform! " +
            "Your driver account is pending verification.");
        
        return driver;
    }
    
    /**
     * Registers a new admin (typically done by super admin).
     */
    public Admin registerAdmin(String email, String password, String firstName, 
                               String lastName, String phone) throws AuthenticationException {
        validateRegistration(email, password, firstName, lastName);
        
        Admin admin = new Admin(email, password, firstName, lastName, phone);
        admin.createAccount();
        userRepository.save(admin);
        
        return admin;
    }
    
    /**
     * Validates registration data.
     */
    private void validateRegistration(String email, String password, 
                                       String firstName, String lastName) 
                                       throws AuthenticationException {
        if (email == null || email.trim().isEmpty()) {
            throw new AuthenticationException("Email is required");
        }
        
        if (!email.contains("@")) {
            throw new AuthenticationException("Invalid email format");
        }
        
        if (userRepository.existsByEmail(email)) {
            throw AuthenticationException.emailAlreadyExists();
        }
        
        if (password == null || password.length() < 6) {
            throw new AuthenticationException("Password must be at least 6 characters");
        }
        
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new AuthenticationException("First name is required");
        }
        
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new AuthenticationException("Last name is required");
        }
    }
    
    /**
     * Logs in a user.
     */
    public User login(String email, String password) throws AuthenticationException {
        if (email == null || password == null) {
            throw AuthenticationException.invalidCredentials();
        }
        
        User user = userRepository.findByEmail(email);
        
        if (user == null) {
            throw AuthenticationException.invalidCredentials();
        }
        
        if (user.isBlocked()) {
            throw AuthenticationException.accountBlocked();
        }
        
        if (user.isSuspended()) {
            throw AuthenticationException.accountSuspended();
        }
        
        if (user.authenticate(password)) {
            this.currentUser = user;
            userRepository.save(user); // Save updated login state
            return user;
        } else {
            userRepository.save(user); // Save failed attempt count
            
            if (user.isBlocked()) {
                notificationService.sendEmail(user, "Account Blocked", 
                    "Your account has been blocked due to too many failed login attempts. " +
                    "Please contact support.");
                throw AuthenticationException.accountBlocked();
            }
            
            throw AuthenticationException.invalidCredentials();
        }
    }
    
    /**
     * Logs out the current user.
     */
    public void logout() {
        if (currentUser != null) {
            currentUser.logout();
            userRepository.save(currentUser);
            currentUser = null;
        }
    }
    
    /**
     * Returns the currently logged in user.
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Checks if a user is currently logged in.
     */
    public boolean isLoggedIn() {
        return currentUser != null && currentUser.isLoggedIn();
    }
    
    /**
     * Changes the password for the current user.
     */
    public boolean changePassword(String oldPassword, String newPassword) 
            throws AuthenticationException {
        if (currentUser == null) {
            throw AuthenticationException.notLoggedIn();
        }
        
        if (!currentUser.authenticate(oldPassword)) {
            throw new AuthenticationException("Current password is incorrect");
        }
        
        if (newPassword == null || newPassword.length() < 6) {
            throw new AuthenticationException("New password must be at least 6 characters");
        }
        
        currentUser.setPassword(newPassword);
        userRepository.save(currentUser);
        
        notificationService.sendEmail(currentUser, "Password Changed", 
            "Your password has been successfully changed.");
        
        return true;
    }
    
    /**
     * Resets password (sends reset link - simulated).
     */
    public boolean resetPassword(String email) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            // In real system, would generate reset token and send link
            String tempPassword = "Reset123"; // Simulated
            user.setPassword(tempPassword);
            userRepository.save(user);
            
            notificationService.sendEmail(user, "Password Reset", 
                "Your temporary password is: " + tempPassword + 
                "\nPlease change it after logging in.");
            
            return true;
        }
        return false;
    }
    
    /**
     * Gets user by ID.
     */
    public User getUserById(String id) {
        return userRepository.findById(id);
    }
    
    /**
     * Gets user by email.
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
