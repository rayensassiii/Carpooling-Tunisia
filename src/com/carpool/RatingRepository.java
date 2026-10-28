package com.carpool.repository;

import com.carpool.model.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.time.LocalDateTime;

/**
 * Repository for Rating entities with SQLite persistence.
 */
public class RatingRepository {
    
    private final Map<String, Rating> ratings;
    
    public RatingRepository() {
        this.ratings = new HashMap<>();
        DatabaseInitializer.initialize();
    }
    
    /**
     * Saves a rating.
     */
    public void save(Rating rating) {
        if (rating != null) {
            ratings.put(rating.getId(), rating);
            upsertRating(rating);
        }
    }
    
    /**
     * Finds a rating by ID.
     */
    public Rating findById(String id) {
        return ratings.get(id);
    }
    
    /**
     * Returns all ratings.
     */
    public List<Rating> findAll() {
        return new ArrayList<>(ratings.values());
    }
    
    /**
     * Finds ratings by driver.
     */
    public List<Rating> findByDriver(Driver driver) {
        List<Rating> result = new ArrayList<>();
        for (Rating r : ratings.values()) {
            if (r.getDriver().equals(driver)) {
                result.add(r);
            }
        }
        // Sort by date descending
        result.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        return result;
    }
    
    /**
     * Finds rating by passenger and trip.
     */
    public Rating findByPassengerAndTrip(Passenger passenger, Trip trip) {
        for (Rating r : ratings.values()) {
            if (r.getPassenger().equals(passenger) && r.getTrip().equals(trip)) {
                return r;
            }
        }
        return null;
    }
    
    /**
     * Checks if passenger has rated trip.
     */
    public boolean hasRated(Passenger passenger, Trip trip) {
        return findByPassengerAndTrip(passenger, trip) != null;
    }
    
    /**
     * Calculates average rating for driver.
     */
    public double getAverageRating(Driver driver) {
        List<Rating> driverRatings = findByDriver(driver);
        if (driverRatings.isEmpty()) {
            return 0.0;
        }
        
        double sum = 0;
        for (Rating r : driverRatings) {
            sum += r.getScore();
        }
        return sum / driverRatings.size();
    }
    
    /**
     * Returns count of ratings.
     */
    public int count() {
        return ratings.size();
    }
    
    /**
     * Clears all ratings.
     */
    public void clear() {
        ratings.clear();
    }

    public void loadFromDatabase(UserRepository userRepository, TripRepository tripRepository) {
        String sql = """
            SELECT r.external_id, r.score, r.comment, r.created_at,
                   passenger.external_id AS passenger_external_id,
                   driver.external_id AS driver_external_id,
                   trip.external_id AS trip_external_id
            FROM RATINGS r
            JOIN USERS passenger ON r.passenger_id = passenger.id
            JOIN USERS driver ON r.driver_id = driver.id
            JOIN TRIPS trip ON r.trip_id = trip.id
            """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                User passengerUser = userRepository.findById(resultSet.getString("passenger_external_id"));
                User driverUser = userRepository.findById(resultSet.getString("driver_external_id"));
                Trip trip = tripRepository.findById(resultSet.getString("trip_external_id"));
                if (!(passengerUser instanceof Passenger) || !(driverUser instanceof Driver) || trip == null) {
                    continue;
                }

                Passenger passenger = (Passenger) passengerUser;
                Driver driver = (Driver) driverUser;
                LocalDateTime createdAt = LocalDateTime.parse(resultSet.getString("created_at"));
                Rating rating = new Rating(
                    resultSet.getString("external_id"),
                    passenger,
                    driver,
                    trip,
                    resultSet.getInt("score"),
                    resultSet.getString("comment"),
                    createdAt
                );

                rating.rateDriver();
                ratings.put(rating.getId(), rating);
            }
        } catch (SQLException e) {
            System.err.println("Failed to load ratings from database, using in-memory data only: " + e.getMessage());
        }
    }

    private void upsertRating(Rating rating) {
        String sql = """
            INSERT INTO RATINGS (external_id, passenger_id, driver_id, trip_id, score, comment, created_at)
            VALUES (
                ?,
                (SELECT id FROM USERS WHERE external_id = ?),
                (SELECT id FROM USERS WHERE external_id = ?),
                (SELECT id FROM TRIPS WHERE external_id = ?),
                ?,
                ?,
                ?
            )
            ON CONFLICT(external_id) DO UPDATE SET
                passenger_id=(SELECT id FROM USERS WHERE external_id = ?),
                driver_id=(SELECT id FROM USERS WHERE external_id = ?),
                trip_id=(SELECT id FROM TRIPS WHERE external_id = ?),
                score=excluded.score,
                comment=excluded.comment,
                created_at=excluded.created_at
            """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, rating.getId());
            statement.setString(2, rating.getPassenger().getId());
            statement.setString(3, rating.getDriver().getId());
            statement.setString(4, rating.getTrip().getId());
            statement.setInt(5, rating.getScore());
            statement.setString(6, rating.getComment());
            statement.setString(7, rating.getCreatedAt().toString());
            statement.setString(8, rating.getPassenger().getId());
            statement.setString(9, rating.getDriver().getId());
            statement.setString(10, rating.getTrip().getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to persist rating, continuing in-memory only: " + e.getMessage());
        }
    }
}
