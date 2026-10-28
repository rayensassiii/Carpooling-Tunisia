package com.carpool.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * SQLite database connection provider.
 */
public final class DatabaseConnection {

    private static final String URL = "jdbc:sqlite:carpool.db";

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found on classpath", e);
        }
        return DriverManager.getConnection(URL);
    }
}
