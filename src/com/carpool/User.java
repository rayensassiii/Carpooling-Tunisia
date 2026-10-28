package com.carpool.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Abstract base class for all users in the carpooling system.
 * Implements Notifiable and Authenticable interfaces.
 * Uses encapsulation with private fields and defensive copying.
 */
public abstract class User implements Notifiable, Authenticable {
    
    private final String id;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private UserStatus status;
    private int failedLoginAttempts;
    private final LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private boolean loggedIn;
    private final List<Notification> notifications;
    
    private static final int MAX_FAILED_ATTEMPTS = 3;
    
    /**
     * Creates a new User with the given details.
     */
    protected User(String email, String password, String firstName, 
                   String lastName, String phoneNumber) {
        this.id = UUID.randomUUID().toString();
        this.email = Objects.requireNonNull(email, "Email cannot be null");
        this.password = Objects.requireNonNull(password, "Password cannot be null");
        this.firstName = Objects.requireNonNull(firstName, "First name cannot be null");
        this.lastName = Objects.requireNonNull(lastName, "Last name cannot be null");
        this.phoneNumber = phoneNumber;
        this.status = UserStatus.ACTIVE;
        this.failedLoginAttempts = 0;
        this.createdAt = LocalDateTime.now();
        this.loggedIn = false;
        this.notifications = new ArrayList<>();
    }
    
    // ==================== Account Management ====================
    
    /**
     * Creates the user account (marks it as ready for use).
     */
    public void createAccount() {
        this.status = UserStatus.ACTIVE;
    }
    
    /**
     * Updates the user's account information.
     */
    public void updateAccount(String firstName, String lastName, String phoneNumber) {
        if (firstName != null && !firstName.trim().isEmpty()) {
            this.firstName = firstName;
        }
        if (lastName != null && !lastName.trim().isEmpty()) {
            this.lastName = lastName;
        }
        if (phoneNumber != null) {
            this.phoneNumber = phoneNumber;
        }
    }
    
    /**
     * Suspends the user account.
     */
    public void suspendAccount() {
        this.status = UserStatus.SUSPENDED;
        this.loggedIn = false;
    }
    
    /**
     * Blocks the user account (typically after too many failed login attempts).
     */
    public void blockUser() {
        this.status = UserStatus.BLOCKED;
        this.loggedIn = false;
    }
    
    /**
     * Activates a suspended or blocked account.
     */
    public void activateAccount() {
        this.status = UserStatus.ACTIVE;
        this.failedLoginAttempts = 0;
    }
    
    // ==================== Authentication ====================
    
    @Override
    public boolean authenticate(String password) {
        if (this.status == UserStatus.BLOCKED) {
            return false;
        }
        
        if (this.status == UserStatus.SUSPENDED) {
            return false;
        }
        
        if (this.password.equals(password)) {
            resetFailedAttempts();
            this.loggedIn = true;
            this.lastLoginAt = LocalDateTime.now();
            return true;
        } else {
            incrementFailedAttempts();
            return false;
        }
    }
    
    @Override
    public void logout() {
        this.loggedIn = false;
    }
    
    /**
     * Increments failed login attempts and blocks user if threshold reached.
     */
    public void incrementFailedAttempts() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= MAX_FAILED_ATTEMPTS) {
            blockUser();
        }
    }
    
    /**
     * Resets failed login attempts counter.
     */
    public void resetFailedAttempts() {
        this.failedLoginAttempts = 0;
    }
    
    // ==================== Notifications ====================
    
    @Override
    public void receiveNotification(Notification notification) {
        if (notification != null) {
            this.notifications.add(notification);
        }
    }
    
    /**
     * Returns a defensive copy of notifications.
     */
    public List<Notification> getNotifications() {
        return new ArrayList<>(notifications);
    }
    
    /**
     * Returns unread notifications.
     */
    public List<Notification> getUnreadNotifications() {
        List<Notification> unread = new ArrayList<>();
        for (Notification n : notifications) {
            if (!n.isRead()) {
                unread.add(n);
            }
        }
        return unread;
    }
    
    /**
     * Returns the count of unread notifications.
     */
    public int getUnreadCount() {
        return getUnreadNotifications().size();
    }
    
    // ==================== Getters ====================
    
    public String getId() {
        return id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public UserStatus getStatus() {
        return status;
    }
    
    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }
    
    public boolean isLoggedIn() {
        return loggedIn;
    }
    
    public boolean isBlocked() {
        return status == UserStatus.BLOCKED;
    }
    
    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }
    
    public boolean isSuspended() {
        return status == UserStatus.SUSPENDED;
    }
    
    // ==================== Setters ====================
    
    /**
     * Sets the user's password.
     */
    public void setPassword(String password) {
        this.password = Objects.requireNonNull(password, "Password cannot be null");
    }
    
    /**
     * Sets the user's email.
     */
    public void setEmail(String email) {
        this.email = Objects.requireNonNull(email, "Email cannot be null");
    }
    
    /**
     * Sets the user's status.
     */
    public void setStatus(UserStatus status) {
        this.status = status;
    }
    
    /**
     * Gets the password (for authentication).
     */
    public String getPassword() {
        return password;
    }
    
    // ==================== Abstract Methods ====================
    
    /**
     * Returns the role of the user.
     */
    public abstract String getRole();
    
    // ==================== Object Methods ====================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return getRole() + "{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", name='" + getFullName() + '\'' +
                ", status=" + status +
                '}';
    }
}
