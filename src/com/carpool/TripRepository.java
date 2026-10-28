package com.carpool.repository;

import com.carpool.model.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * In-memory repository for Trip entities.
 */
public class TripRepository {
    
    private final Map<String, Trip> trips;
    
    public TripRepository() {
        this.trips = new HashMap<>();
    }
    
    /**
     * Saves a trip to the repository.
     */
    public void save(Trip trip) {
        if (trip != null) {
            trips.put(trip.getId(), trip);
            upsertTrip(trip);
        }
    }
    
    /**
     * Finds a trip by ID.
     */
    public Trip findById(String id) {
        return trips.get(id);
    }
    
    /**
     * Returns all trips (defensive copy).
     */
    public List<Trip> findAll() {
        return new ArrayList<>(trips.values());
    }
    
    /**
     * Finds trips by driver.
     */
    public List<Trip> findByDriver(Driver driver) {
        List<Trip> result = new ArrayList<>();
        for (Trip trip : trips.values()) {
            if (trip.getDriver().equals(driver)) {
                result.add(trip);
            }
        }
        return result;
    }
    
    /**
     * Finds available (open) trips.
     */
    public List<Trip> findAvailable() {
        List<Trip> result = new ArrayList<>();
        for (Trip trip : trips.values()) {
            if (trip.getStatus() == TripStatus.OPEN) {
                result.add(trip);
            }
        }
        return result;
    }
    
    /**
     * Finds trips by status.
     */
    public List<Trip> findByStatus(TripStatus status) {
        List<Trip> result = new ArrayList<>();
        for (Trip trip : trips.values()) {
            if (trip.getStatus() == status) {
                result.add(trip);
            }
        }
        return result;
    }
    
    /**
     * Searches trips by departure and arrival cities.
     */
    public List<Trip> searchByRoute(String departureCity, String arrivalCity) {
        List<Trip> result = new ArrayList<>();
        for (Trip trip : trips.values()) {
            boolean matchesDeparture = departureCity == null || 
                trip.getDepartureCity().toLowerCase().contains(departureCity.toLowerCase());
            boolean matchesArrival = arrivalCity == null || 
                trip.getArrivalCity().toLowerCase().contains(arrivalCity.toLowerCase());
            
            if (matchesDeparture && matchesArrival && trip.isOpen()) {
                result.add(trip);
            }
        }
        return result;
    }
    
    /**
     * Searches trips by departure city, arrival city, and date.
     */
    public List<Trip> search(String departureCity, String arrivalCity, LocalDate date) {
        List<Trip> result = new ArrayList<>();
        for (Trip trip : trips.values()) {
            boolean matchesDeparture = departureCity == null || departureCity.isEmpty() ||
                trip.getDepartureCity().toLowerCase().contains(departureCity.toLowerCase());
            boolean matchesArrival = arrivalCity == null || arrivalCity.isEmpty() ||
                trip.getArrivalCity().toLowerCase().contains(arrivalCity.toLowerCase());
            boolean matchesDate = date == null || 
                trip.getDepartureTime().toLocalDate().equals(date);
            
            if (matchesDeparture && matchesArrival && matchesDate && 
                (trip.isOpen() || trip.getStatus() == TripStatus.FULL)) {
                result.add(trip);
            }
        }
        
        // Sort by departure time
        result.sort(Comparator.comparing(Trip::getDepartureTime));
        return result;
    }
    
    /**
     * Filters trips by price and minimum seats.
     */
    public List<Trip> filter(double maxPrice, int minSeats) {
        List<Trip> result = new ArrayList<>();
        for (Trip trip : trips.values()) {
            boolean matchesPrice = maxPrice <= 0 || trip.getPricePerSeat() <= maxPrice;
            boolean matchesSeats = minSeats <= 0 || trip.getAvailableSeats() >= minSeats;
            
            if (matchesPrice && matchesSeats && trip.isOpen()) {
                result.add(trip);
            }
        }
        return result;
    }
    
    /**
     * Deletes a trip.
     */
    public void delete(Trip trip) {
        if (trip != null) {
            trips.remove(trip.getId());
            deleteFromDatabase(trip);
        }
    }

    public void update(Trip trip) {
        save(trip);
    }
    
    /**
     * Returns the count of all trips.
     */
    public int count() {
        return trips.size();
    }
    
    /**
     * Clears all trips.
     */
    public void clear() {
        trips.clear();
    }

    public void loadFromDatabase(UserRepository userRepository) {
        String sql = """
            SELECT t.external_id, t.driver_id, t.departure, t.destination, t.date, t.available_seats,
                   t.total_seats, t.price_per_seat, t.status, t.description, t.pets_allowed,
                   t.smoking_allowed, t.luggage_allowed, u.external_id AS driver_external_id
            FROM TRIPS t
            JOIN USERS u ON t.driver_id = u.id
            """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                User user = userRepository.findById(resultSet.getString("driver_external_id"));
                if (!(user instanceof Driver)) {
                    continue;
                }
                Driver driver = (Driver) user;
                LocalDateTime departure = LocalDateTime.parse(resultSet.getString("date"));
                int totalSeats = resultSet.getInt("total_seats");
                Vehicle vehicle = driver.getPrimaryVehicle();
                if (vehicle == null) {
                    vehicle = new Vehicle(
                        "Vehicle",
                        "Default",
                        LocalDate.now().getYear(),
                        "N/A",
                        "N/A",
                        VehicleType.SEDAN,
                        Math.max(1, totalSeats)
                    );
                    driver.addVehicle(vehicle);
                }

                Trip trip = new Trip(
                    driver,
                    vehicle,
                    resultSet.getString("departure"),
                    resultSet.getString("destination"),
                    departure,
                    totalSeats,
                    resultSet.getDouble("price_per_seat")
                );

                if (resultSet.getString("status") != null) {
                    try {
                        trip.setStatus(TripStatus.valueOf(resultSet.getString("status")));
                    } catch (IllegalArgumentException ignored) {
                    }
                }
                trip.setDescription(resultSet.getString("description"));
                trip.setPetsAllowed(resultSet.getInt("pets_allowed") == 1);
                trip.setSmokingAllowed(resultSet.getInt("smoking_allowed") == 1);
                trip.setLuggageAllowed(resultSet.getInt("luggage_allowed") == 1);
                alignRuntimeId(trip, resultSet.getString("external_id"));

                driver.addTrip(trip);
                trips.put(trip.getId(), trip);
            }
        } catch (SQLException e) {
            System.err.println("Failed to load trips from database, using in-memory data only: " + e.getMessage());
        }
    }

    private void upsertTrip(Trip trip) {
        String sql = """
            INSERT INTO TRIPS (external_id, driver_id, departure, destination, date, available_seats, total_seats,
                               price_per_seat, status, description, pets_allowed, smoking_allowed, luggage_allowed)
            VALUES (?, (SELECT id FROM USERS WHERE external_id = ?), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(external_id) DO UPDATE SET
                driver_id=excluded.driver_id,
                departure=excluded.departure,
                destination=excluded.destination,
                date=excluded.date,
                available_seats=excluded.available_seats,
                total_seats=excluded.total_seats,
                price_per_seat=excluded.price_per_seat,
                status=excluded.status,
                description=excluded.description,
                pets_allowed=excluded.pets_allowed,
                smoking_allowed=excluded.smoking_allowed,
                luggage_allowed=excluded.luggage_allowed
            """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, trip.getId());
            statement.setString(2, trip.getDriver().getId());
            statement.setString(3, trip.getDepartureCity());
            statement.setString(4, trip.getArrivalCity());
            statement.setString(5, trip.getDepartureTime().toString());
            statement.setInt(6, trip.getAvailableSeats());
            statement.setInt(7, trip.getTotalSeats());
            statement.setDouble(8, trip.getPricePerSeat());
            statement.setString(9, trip.getStatus().name());
            statement.setString(10, trip.getDescription());
            statement.setInt(11, trip.isPetsAllowed() ? 1 : 0);
            statement.setInt(12, trip.isSmokingAllowed() ? 1 : 0);
            statement.setInt(13, trip.isLuggageAllowed() ? 1 : 0);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to persist trip, continuing in-memory only: " + e.getMessage());
        }
    }

    private void deleteFromDatabase(Trip trip) {
        String sql = "DELETE FROM TRIPS WHERE external_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, trip.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to delete trip from database: " + e.getMessage());
        }
    }

    private void alignRuntimeId(Trip trip, String expectedId) {
        if (expectedId == null || expectedId.equals(trip.getId())) {
            return;
        }
        try {
            java.lang.reflect.Field idField = Trip.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(trip, expectedId);
        } catch (ReflectiveOperationException ignored) {
        }
    }
}
