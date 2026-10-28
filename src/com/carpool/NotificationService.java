package com.carpool.service;

import com.carpool.model.*;
import com.carpool.repository.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for notification management.
 * Handles sending Email and SMS notifications.
 */
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    
    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }
    
    /**
     * Sends a notification via the specified channel.
     */
    public Notification sendNotification(User user, String title, String message, 
                                         NotificationType type) {
        if (user == null || title == null || message == null) {
            return null;
        }
        
        Notification notification = new Notification(user, title, message, type);
        notification.send();
        
        // Add to user's notifications
        user.receiveNotification(notification);
        
        // Save
        notificationRepository.save(notification);
        
        return notification;
    }
    
    /**
     * Sends SMS notification.
     */
    public Notification sendSMS(User user, String title, String message) {
        return sendNotification(user, title, message, NotificationType.SMS);
    }
    
    /**
     * Sends email notification.
     */
    public Notification sendEmail(User user, String title, String message) {
        return sendNotification(user, title, message, NotificationType.EMAIL);
    }
    
    /**
     * Sends notification via both channels.
     */
    public Notification sendBoth(User user, String title, String message) {
        return sendNotification(user, title, message, NotificationType.BOTH);
    }
    
    /**
     * Sends bulk notification to multiple users.
     */
    public List<Notification> sendBulkNotification(List<User> users, String title, 
                                                    String message, NotificationType type) {
        List<Notification> notifications = new ArrayList<>();
        
        for (User user : users) {
            Notification notification = sendNotification(user, title, message, type);
            if (notification != null) {
                notifications.add(notification);
            }
        }
        
        return notifications;
    }
    
    /**
     * Marks a notification as read.
     */
    public void markAsRead(Notification notification) {
        if (notification != null) {
            notification.markAsRead();
            notificationRepository.save(notification);
        }
    }
    
    /**
     * Marks all notifications as read for a user.
     */
    public void markAllAsRead(User user) {
        notificationRepository.markAllAsRead(user);
    }
    
    /**
     * Gets all notifications for a user.
     */
    public List<Notification> getUserNotifications(User user) {
        return notificationRepository.findByUser(user);
    }
    
    /**
     * Gets unread notifications for a user.
     */
    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findUnreadByUser(user);
    }
    
    /**
     * Gets unread count for a user.
     */
    public int getUnreadCount(User user) {
        return notificationRepository.countUnreadByUser(user);
    }
    
    // ==================== Specific Notification Templates ====================
    
    /**
     * Sends reservation confirmation notification.
     */
    public void notifyReservationConfirmed(Reservation reservation) {
        Trip trip = reservation.getTrip();
        Passenger passenger = reservation.getPassenger();
        
        String message = String.format(
            "Your reservation for %s → %s on %s has been confirmed!\n" +
            "Seats: %d | Total: %.2f€\n" +
            "Driver: %s | Vehicle: %s",
            trip.getDepartureCity(), trip.getArrivalCity(),
            trip.getDepartureTime().toLocalDate(),
            reservation.getSeatsBooked(), reservation.getTotalPrice(),
            trip.getDriver().getFullName(), trip.getVehicle().getShortDescription()
        );
        
        sendBoth(passenger, "Reservation Confirmed ✓", message);
    }
    
    /**
     * Sends trip reminder notification.
     */
    public void notifyTripReminder(Trip trip) {
        String message = String.format(
            "Reminder: Your trip from %s to %s departs tomorrow at %s.\n" +
            "Please arrive at the meeting point on time.",
            trip.getDepartureCity(), trip.getArrivalCity(),
            trip.getDepartureTime().toLocalTime()
        );
        
        // Notify all confirmed passengers
        for (Reservation r : trip.getConfirmedReservations()) {
            sendBoth(r.getPassenger(), "Trip Tomorrow!", message);
        }
        
        // Notify driver
        sendBoth(trip.getDriver(), "Trip Tomorrow!", 
            "Reminder: Your trip to " + trip.getArrivalCity() + " is tomorrow. " +
            "You have " + trip.getBookedSeats() + " passenger(s).");
    }
    
    /**
     * Sends rating request notification.
     */
    public void notifyRatingRequest(Reservation reservation) {
        Trip trip = reservation.getTrip();
        
        sendEmail(reservation.getPassenger(), 
            "Rate Your Trip", 
            "How was your trip from " + trip.getDepartureCity() + " to " + 
            trip.getArrivalCity() + " with " + trip.getDriver().getFullName() + "? " +
            "Please rate your experience!");
    }
}
