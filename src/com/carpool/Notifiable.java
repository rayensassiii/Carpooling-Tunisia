package com.carpool.model;

/**
 * Interface for entities that can receive notifications.
 */
public interface Notifiable {
    /**
     * Receives a notification.
     * @param notification The notification to receive
     */
    void receiveNotification(Notification notification);
}
