package com.carpool.repository;

import com.carpool.model.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository for Notification entities with SQLite persistence.
 */
public class NotificationRepository {
    
    private final Map<String, Notification> notifications;
    
    public NotificationRepository() {
        this.notifications = new HashMap<>();
        DatabaseInitializer.initialize();
    }
    
    /**
     * Saves a notification.
     */
    public void save(Notification notification) {
        if (notification != null) {
            notifications.put(notification.getId(), notification);
            upsertNotification(notification);
        }
    }
    
    /**
     * Finds a notification by ID.
     */
    public Notification findById(String id) {
        return notifications.get(id);
    }
    
    /**
     * Returns all notifications.
     */
    public List<Notification> findAll() {
        return new ArrayList<>(notifications.values());
    }
    
    /**
     * Finds notifications by user.
     */
    public List<Notification> findByUser(User user) {
        List<Notification> result = new ArrayList<>();
        for (Notification n : notifications.values()) {
            if (n.getRecipient().equals(user)) {
                result.add(n);
            }
        }
        result.sort((a, b) -> b.getSentAt().compareTo(a.getSentAt()));
        return result;
    }
    
    /**
     * Finds unread notifications by user.
     */
    public List<Notification> findUnreadByUser(User user) {
        List<Notification> result = new ArrayList<>();
        for (Notification n : notifications.values()) {
            if (n.getRecipient().equals(user) && !n.isRead()) {
                result.add(n);
            }
        }
        result.sort((a, b) -> b.getSentAt().compareTo(a.getSentAt()));
        return result;
    }
    
    /**
     * Returns count of unread notifications for user.
     */
    public int countUnreadByUser(User user) {
        return findUnreadByUser(user).size();
    }
    
    /**
     * Marks all notifications as read for user.
     */
    public void markAllAsRead(User user) {
        for (Notification n : notifications.values()) {
            if (n.getRecipient().equals(user)) {
                n.markAsRead();
                upsertNotification(n);
            }
        }
    }
    
    /**
     * Returns count of notifications.
     */
    public int count() {
        return notifications.size();
    }
    
    /**
     * Clears all notifications.
     */
    public void clear() {
        notifications.clear();
    }

    public void loadFromDatabase(UserRepository userRepository) {
        notifications.clear();
        String sql = """
            SELECT n.external_id, n.title, n.message, n.type, n.is_read, n.sent_at, n.read_at,
                   n.email_sent, n.sms_sent, u.external_id AS recipient_external_id
            FROM NOTIFICATIONS n
            JOIN USERS u ON n.recipient_id = u.id
            """;
            try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                try {
                    User recipient = userRepository.findById(resultSet.getString("recipient_external_id"));
                    if (recipient == null) {
                        continue;
                    }

                    Notification notification = new Notification(
                        resultSet.getString("external_id"),
                        recipient,
                        resultSet.getString("title"),
                        resultSet.getString("message"),
                        parseType(resultSet.getString("type")),
                        resultSet.getInt("is_read") == 1,
                        LocalDateTime.parse(resultSet.getString("sent_at")),
                        parseDateTime(resultSet.getString("read_at")),
                        resultSet.getInt("email_sent") == 1,
                        resultSet.getInt("sms_sent") == 1
                    );

                    notifications.put(notification.getId(), notification);
                    recipient.receiveNotification(notification);
                } catch (RuntimeException e) {
                    System.err.println("Skipping invalid notification row: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to load notifications from database, using in-memory data only: " + e.getMessage());
        }
    }

    private void upsertNotification(Notification notification) {
        String sql = """
            INSERT INTO NOTIFICATIONS (
                external_id, recipient_id, title, message, type, is_read, sent_at, read_at, email_sent, sms_sent
            )
            VALUES (
                ?,
                (SELECT id FROM USERS WHERE external_id = ?),
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?
            )
            ON CONFLICT(external_id) DO UPDATE SET
                recipient_id=(SELECT id FROM USERS WHERE external_id = ?),
                title=excluded.title,
                message=excluded.message,
                type=excluded.type,
                is_read=excluded.is_read,
                sent_at=excluded.sent_at,
                read_at=excluded.read_at,
                email_sent=excluded.email_sent,
                sms_sent=excluded.sms_sent
            """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, notification.getId());
            statement.setString(2, notification.getRecipient().getId());
            statement.setString(3, notification.getTitle());
            statement.setString(4, notification.getMessage());
            statement.setString(5, notification.getType().name());
            statement.setInt(6, notification.isRead() ? 1 : 0);
            statement.setString(7, notification.getSentAt().toString());
            statement.setString(8, notification.getReadAt() != null ? notification.getReadAt().toString() : null);
            statement.setInt(9, notification.isEmailSent() ? 1 : 0);
            statement.setInt(10, notification.isSmsSent() ? 1 : 0);
            statement.setString(11, notification.getRecipient().getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to persist notification, continuing in-memory only: " + e.getMessage());
        }
    }

    private NotificationType parseType(String rawType) {
        if (rawType == null || rawType.isEmpty()) {
            return NotificationType.EMAIL;
        }
        try {
            return NotificationType.valueOf(rawType);
        } catch (IllegalArgumentException e) {
            return NotificationType.EMAIL;
        }
    }

    private LocalDateTime parseDateTime(String rawDateTime) {
        if (rawDateTime == null || rawDateTime.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(rawDateTime);
    }
}
