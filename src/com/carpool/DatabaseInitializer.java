package com.carpool.repository;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Creates required database tables on startup.
 */
public final class DatabaseInitializer {

    private DatabaseInitializer() {
    }

    public static void initialize() {
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute("""
                CREATE TABLE IF NOT EXISTS USERS (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    external_id TEXT UNIQUE,
                    first_name TEXT,
                    last_name TEXT,
                    name TEXT,
                    email TEXT UNIQUE,
                    password TEXT,
                    phone TEXT,
                    role TEXT,
                    status TEXT,
                    license_number TEXT,
                    verified INTEGER DEFAULT 0
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS TRIPS (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    external_id TEXT UNIQUE,
                    driver_id INTEGER,
                    departure TEXT,
                    destination TEXT,
                    date TEXT,
                    available_seats INTEGER,
                    total_seats INTEGER,
                    price_per_seat REAL,
                    status TEXT,
                    description TEXT,
                    pets_allowed INTEGER DEFAULT 0,
                    smoking_allowed INTEGER DEFAULT 0,
                    luggage_allowed INTEGER DEFAULT 1,
                    FOREIGN KEY(driver_id) REFERENCES USERS(id)
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS RESERVATIONS (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    external_id TEXT UNIQUE,
                    trip_id INTEGER,
                    passenger_id INTEGER,
                    seats_booked INTEGER,
                    status TEXT,
                    FOREIGN KEY(trip_id) REFERENCES TRIPS(id),
                    FOREIGN KEY(passenger_id) REFERENCES USERS(id)
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS RATINGS (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    external_id TEXT UNIQUE,
                    passenger_id INTEGER,
                    driver_id INTEGER,
                    trip_id INTEGER,
                    score INTEGER NOT NULL,
                    comment TEXT,
                    created_at TEXT,
                    FOREIGN KEY(passenger_id) REFERENCES USERS(id),
                    FOREIGN KEY(driver_id) REFERENCES USERS(id),
                    FOREIGN KEY(trip_id) REFERENCES TRIPS(id)
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS PAYMENT_METHODS (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    external_id TEXT UNIQUE,
                    user_id INTEGER,
                    card_number TEXT,
                    card_holder_name TEXT,
                    expiry_date TEXT,
                    cvv TEXT,
                    card_type TEXT,
                    is_default INTEGER DEFAULT 0,
                    FOREIGN KEY(user_id) REFERENCES USERS(id)
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS VEHICLES (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    external_id TEXT UNIQUE,
                    driver_id INTEGER,
                    brand TEXT,
                    model TEXT,
                    year INTEGER,
                    license_plate TEXT,
                    color TEXT,
                    type TEXT,
                    seats INTEGER,
                    active INTEGER DEFAULT 1,
                    FOREIGN KEY(driver_id) REFERENCES USERS(id)
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS NOTIFICATIONS (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    external_id TEXT UNIQUE,
                    recipient_id INTEGER,
                    title TEXT,
                    message TEXT,
                    type TEXT,
                    is_read INTEGER DEFAULT 0,
                    sent_at TEXT,
                    read_at TEXT,
                    email_sent INTEGER DEFAULT 0,
                    sms_sent INTEGER DEFAULT 0,
                    FOREIGN KEY(recipient_id) REFERENCES USERS(id)
                )
                """);
        } catch (SQLException e) {
            System.err.println("Database initialization failed, continuing in-memory only: " + e.getMessage());
        }
    }
}
