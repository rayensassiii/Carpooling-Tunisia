package com.carpool.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a notification sent to a user.
 * Supports Email and SMS notification types.
 */
public class Notification {
    
    private final String id;
    private final User recipient;
    private final String title;
    private final String message;
    private final NotificationType type;
    private boolean read;
    private final LocalDateTime sentAt;
    private LocalDateTime readAt;
    private boolean emailSent;
    private boolean smsSent;
    
    /**
     * Creates a new Notification.
     */
    public Notification(User recipient, String title, String message, NotificationType type) {
        this(UUID.randomUUID().toString(), recipient, title, message, type, false, LocalDateTime.now(), null, false, false);
    }

    /**
     * Creates a notification with a fixed identity and timestamps.
     */
    public Notification(String id, User recipient, String title, String message, NotificationType type,
                        boolean read, LocalDateTime sentAt, LocalDateTime readAt,
                        boolean emailSent, boolean smsSent) {
        this.id = Objects.requireNonNull(id, "Id cannot be null");
        this.recipient = Objects.requireNonNull(recipient, "Recipient cannot be null");
        this.title = Objects.requireNonNull(title, "Title cannot be null");
        this.message = Objects.requireNonNull(message, "Message cannot be null");
        this.type = type != null ? type : NotificationType.EMAIL;
        this.read = read;
        this.sentAt = Objects.requireNonNull(sentAt, "Sent time cannot be null");
        this.readAt = readAt;
        this.emailSent = emailSent;
        this.smsSent = smsSent;
    }
    
    // ==================== Notification Operations ====================
    
    /**
     * Sends the notification via SMS.
     * In a real system, this would integrate with an SMS provider.
     */
    public boolean sendSMS() {
        if (type == NotificationType.SMS || type == NotificationType.BOTH) {
            // Simulate SMS sending
            System.out.println("[SMS] To: " + recipient.getPhoneNumber());
            System.out.println("[SMS] Title: " + title);
            System.out.println("[SMS] Message: " + message);
            this.smsSent = true;
            return true;
        }
        return false;
    }
    
    /**
     * Sends the notification via Email.
     * In a real system, this would integrate with an email provider.
     */
    public boolean sendEmail() {
        if (type == NotificationType.EMAIL || type == NotificationType.BOTH) {
            // Simulate email sending
            System.out.println("[EMAIL] To: " + recipient.getEmail());
            System.out.println("[EMAIL] Subject: " + title);
            System.out.println("[EMAIL] Body: " + message);
            this.emailSent = true;
            return true;
        }
        return false;
    }
    
    /**
     * Sends the notification via configured channels.
     */
    public boolean send() {
        boolean success = false;
        
        switch (type) {
            case EMAIL:
                success = sendEmail();
                break;
            case SMS:
                success = sendSMS();
                break;
            case BOTH:
                boolean emailOk = sendEmail();
                boolean smsOk = sendSMS();
                success = emailOk || smsOk;
                break;
        }
        
        return success;
    }
    
    /**
     * Marks the notification as read.
     */
    public void markAsRead() {
        if (!this.read) {
            this.read = true;
            this.readAt = LocalDateTime.now();
        }
    }
    
    /**
     * Marks the notification as unread.
     */
    public void markAsUnread() {
        this.read = false;
        this.readAt = null;
    }
    
    // ==================== Getters ====================
    
    public String getId() {
        return id;
    }
    
    public User getRecipient() {
        return recipient;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getMessage() {
        return message;
    }
    
    public NotificationType getType() {
        return type;
    }
    
    public boolean isRead() {
        return read;
    }
    
    public LocalDateTime getSentAt() {
        return sentAt;
    }
    
    public LocalDateTime getReadAt() {
        return readAt;
    }
    
    public boolean isEmailSent() {
        return emailSent;
    }
    
    public boolean isSmsSent() {
        return smsSent;
    }
    
    /**
     * Returns a preview of the message (first 50 chars).
     */
    public String getPreview() {
        if (message == null || message.length() <= 50) {
            return message;
        }
        return message.substring(0, 47) + "...";
    }
    
    /**
     * Returns time since notification was sent.
     */
    public String getTimeSince() {
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(sentAt, now).toMinutes();
        
        if (minutes < 1) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + " min ago";
        } else if (minutes < 1440) {
            return (minutes / 60) + " hours ago";
        } else {
            return (minutes / 1440) + " days ago";
        }
    }
    
    // ==================== Object Methods ====================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification that = (Notification) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Notification{" +
                "id='" + id + '\'' +
                ", to='" + recipient.getEmail() + '\'' +
                ", title='" + title + '\'' +
                ", type=" + type +
                ", read=" + read +
                ", sent=" + sentAt +
                '}';
    }
}
