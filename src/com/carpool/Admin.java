package com.carpool.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an administrator in the carpooling system.
 * Admins can manage users, verify drivers, and view system statistics.
 */
public class Admin extends User {
    
    private int adminLevel;
    
    /**
     * Creates a new Admin.
     */
    public Admin(String email, String password, String firstName, 
                 String lastName, String phoneNumber) {
        super(email, password, firstName, lastName, phoneNumber);
        this.adminLevel = 1;
    }
    
    /**
     * Creates a new Admin with specified level.
     */
    public Admin(String email, String password, String firstName, 
                 String lastName, String phoneNumber, int adminLevel) {
        super(email, password, firstName, lastName, phoneNumber);
        this.adminLevel = adminLevel;
    }
    
    // ==================== User Management ====================
    
    /**
     * Suspends a user account.
     */
    public void suspendUser(User user) {
        if (user != null && !(user instanceof Admin)) {
            user.suspendAccount();
        }
    }
    
    /**
     * Activates a suspended user account.
     */
    public void activateUser(User user) {
        if (user != null) {
            user.activateAccount();
        }
    }
    
    /**
     * Blocks a user account.
     */
    public void blockUser(User user) {
        if (user != null && !(user instanceof Admin)) {
            user.blockUser();
        }
    }
    
    /**
     * Verifies a driver.
     */
    public void verifyDriver(Driver driver) {
        if (driver != null) {
            driver.verify();
        }
    }
    
    /**
     * Revokes driver verification.
     */
    public void revokeDriverVerification(Driver driver) {
        if (driver != null) {
            driver.revokeVerification();
        }
    }
    
    // ==================== Statistics ====================
    
    /**
     * Gets system statistics from provided data.
     */
    public Map<String, Object> getSystemStatistics(List<User> users, 
                                                    List<Trip> trips, 
                                                    List<Reservation> reservations) {
        Map<String, Object> stats = new HashMap<>();
        
        int totalUsers = users != null ? users.size() : 0;
        int totalPassengers = 0;
        int totalDrivers = 0;
        int activeUsers = 0;
        int blockedUsers = 0;
        
        if (users != null) {
            for (User u : users) {
                if (u instanceof Passenger && !(u instanceof Driver)) totalPassengers++;
                if (u instanceof Driver) totalDrivers++;
                if (u.isActive()) activeUsers++;
                if (u.isBlocked()) blockedUsers++;
            }
        }
        
        int totalTrips = trips != null ? trips.size() : 0;
        int openTrips = 0;
        int completedTrips = 0;
        int cancelledTrips = 0;
        
        if (trips != null) {
            for (Trip t : trips) {
                if (t.getStatus() == TripStatus.OPEN || t.getStatus() == TripStatus.FULL) openTrips++;
                if (t.getStatus() == TripStatus.COMPLETED) completedTrips++;
                if (t.getStatus() == TripStatus.CANCELLED) cancelledTrips++;
            }
        }
        
        int totalReservations = reservations != null ? reservations.size() : 0;
        int confirmedReservations = 0;
        double totalRevenue = 0;
        
        if (reservations != null) {
            for (Reservation r : reservations) {
                if (r.getStatus() == ReservationStatus.CONFIRMED || 
                    r.getStatus() == ReservationStatus.COMPLETED) {
                    confirmedReservations++;
                    if (r.getPayment() != null && r.getPayment().isCaptured()) {
                        totalRevenue += r.getPayment().getAmount();
                    }
                }
            }
        }
        
        stats.put("totalUsers", totalUsers);
        stats.put("totalPassengers", totalPassengers);
        stats.put("totalDrivers", totalDrivers);
        stats.put("activeUsers", activeUsers);
        stats.put("blockedUsers", blockedUsers);
        stats.put("totalTrips", totalTrips);
        stats.put("openTrips", openTrips);
        stats.put("completedTrips", completedTrips);
        stats.put("cancelledTrips", cancelledTrips);
        stats.put("totalReservations", totalReservations);
        stats.put("confirmedReservations", confirmedReservations);
        stats.put("totalRevenue", totalRevenue);
        
        return stats;
    }
    
    // ==================== Admin Level ====================
    
    public int getAdminLevel() {
        return adminLevel;
    }
    
    public void setAdminLevel(int adminLevel) {
        this.adminLevel = Math.max(1, adminLevel);
    }
    
    public boolean isSuperAdmin() {
        return adminLevel >= 3;
    }
    
    // ==================== User Role ====================
    
    @Override
    public String getRole() {
        return "Admin" + (isSuperAdmin() ? " (Super)" : "");
    }
    
    @Override
    public String toString() {
        return "Admin{" +
                "id='" + getId() + '\'' +
                ", name='" + getFullName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", level=" + adminLevel +
                '}';
    }
}
