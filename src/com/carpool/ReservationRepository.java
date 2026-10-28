package com.carpool.repository;

import com.carpool.model.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * In-memory repository for Reservation entities.
 */
public class ReservationRepository {
    
    private final Map<String, Reservation> reservations;
    
    public ReservationRepository() {
        this.reservations = new HashMap<>();
    }
    
    /**
     * Saves a reservation.
     */
    public void save(Reservation reservation) {
        if (reservation != null) {
            reservations.put(reservation.getId(), reservation);
            upsertReservation(reservation);
        }
    }
    
    /**
     * Finds a reservation by ID.
     */
    public Reservation findById(String id) {
        return reservations.get(id);
    }
    
    /**
     * Returns all reservations (defensive copy).
     */
    public List<Reservation> findAll() {
        return new ArrayList<>(reservations.values());
    }
    
    /**
     * Finds reservations by passenger.
     */
    public List<Reservation> findByPassenger(Passenger passenger) {
        List<Reservation> result = new ArrayList<>();
        for (Reservation r : reservations.values()) {
            if (r.getPassenger().equals(passenger)) {
                result.add(r);
            }
        }
        return result;
    }
    
    /**
     * Finds reservations by trip.
     */
    public List<Reservation> findByTrip(Trip trip) {
        List<Reservation> result = new ArrayList<>();
        for (Reservation r : reservations.values()) {
            if (r.getTrip().equals(trip)) {
                result.add(r);
            }
        }
        return result;
    }
    
    /**
     * Finds reservations by status.
     */
    public List<Reservation> findByStatus(ReservationStatus status) {
        List<Reservation> result = new ArrayList<>();
        for (Reservation r : reservations.values()) {
            if (r.getStatus() == status) {
                result.add(r);
            }
        }
        return result;
    }
    
    /**
     * Checks if passenger has existing reservation for trip.
     */
    public boolean existsByPassengerAndTrip(Passenger passenger, Trip trip) {
        for (Reservation r : reservations.values()) {
            if (r.getPassenger().equals(passenger) && 
                r.getTrip().equals(trip) &&
                r.getStatus() != ReservationStatus.CANCELLED) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Deletes a reservation.
     */
    public void delete(Reservation reservation) {
        if (reservation != null) {
            reservations.remove(reservation.getId());
            deleteFromDatabase(reservation);
        }
    }

    public void update(Reservation reservation) {
        save(reservation);
    }
    
    /**
     * Returns count of reservations.
     */
    public int count() {
        return reservations.size();
    }
    
    /**
     * Clears all reservations.
     */
    public void clear() {
        reservations.clear();
    }

    public void loadFromDatabase(UserRepository userRepository, TripRepository tripRepository) {
        String sql = """
            SELECT r.external_id, r.seats_booked, r.status,
                   u.external_id AS passenger_external_id,
                   t.external_id AS trip_external_id
            FROM RESERVATIONS r
            JOIN USERS u ON r.passenger_id = u.id
            JOIN TRIPS t ON r.trip_id = t.id
            """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                User user = userRepository.findById(resultSet.getString("passenger_external_id"));
                Trip trip = tripRepository.findById(resultSet.getString("trip_external_id"));
                if (!(user instanceof Passenger) || trip == null) {
                    continue;
                }

                Passenger passenger = (Passenger) user;
                int seatsBooked = resultSet.getInt("seats_booked");

                Reservation reservation = new Reservation(passenger, trip, seatsBooked);
                reservation.setStatus(parseStatus(resultSet.getString("status")));
                alignRuntimeId(reservation, resultSet.getString("external_id"));

                boolean shouldRestoreCompleted = reservation.getStatus() == ReservationStatus.COMPLETED ||
                    (trip.getStatus() == TripStatus.COMPLETED && reservation.getStatus() == ReservationStatus.CONFIRMED);

                if (shouldRestoreCompleted) {
                    if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
                        reservation.setStatus(ReservationStatus.COMPLETED);
                    }
                    trip.restoreReservation(reservation);
                    save(reservation);
                } else if (reservation.getStatus() == ReservationStatus.PENDING ||
                    reservation.getStatus() == ReservationStatus.CONFIRMED) {
                    trip.addPassenger(reservation);
                }
                passenger.addReservation(reservation);
                reservations.put(reservation.getId(), reservation);
            }
        } catch (SQLException e) {
            System.err.println("Failed to load reservations from database, using in-memory data only: " + e.getMessage());
        }
    }

    private void upsertReservation(Reservation reservation) {
        String sql = """
            INSERT INTO RESERVATIONS (external_id, trip_id, passenger_id, seats_booked, status)
            VALUES (
                ?,
                (SELECT id FROM TRIPS WHERE external_id = ?),
                (SELECT id FROM USERS WHERE external_id = ?),
                ?,
                ?
            )
            ON CONFLICT(external_id) DO UPDATE SET
                trip_id=(SELECT id FROM TRIPS WHERE external_id = ?),
                passenger_id=(SELECT id FROM USERS WHERE external_id = ?),
                seats_booked=excluded.seats_booked,
                status=excluded.status
            """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, reservation.getId());
            statement.setString(2, reservation.getTrip().getId());
            statement.setString(3, reservation.getPassenger().getId());
            statement.setInt(4, reservation.getSeatsBooked());
            statement.setString(5, reservation.getStatus().name());
            statement.setString(6, reservation.getTrip().getId());
            statement.setString(7, reservation.getPassenger().getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to persist reservation, continuing in-memory only: " + e.getMessage());
        }
    }

    private void deleteFromDatabase(Reservation reservation) {
        String sql = "DELETE FROM RESERVATIONS WHERE external_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, reservation.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to delete reservation from database: " + e.getMessage());
        }
    }

    private ReservationStatus parseStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.isEmpty()) {
            return ReservationStatus.PENDING;
        }
        try {
            return ReservationStatus.valueOf(rawStatus);
        } catch (IllegalArgumentException e) {
            return ReservationStatus.PENDING;
        }
    }

    private void alignRuntimeId(Reservation reservation, String expectedId) {
        if (expectedId == null || expectedId.equals(reservation.getId())) {
            return;
        }
        try {
            java.lang.reflect.Field idField = Reservation.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(reservation, expectedId);
        } catch (ReflectiveOperationException ignored) {
        }
    }
}
