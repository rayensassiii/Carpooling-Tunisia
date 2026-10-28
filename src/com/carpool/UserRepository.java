package com.carpool.repository;

import com.carpool.model.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * In-memory repository for User entities.
 * Uses HashMap for O(1) lookups by ID and email.
 */
public class UserRepository {
    
    private final Map<String, User> usersById;
    private final Map<String, User> usersByEmail;
    
    public UserRepository() {
        this.usersById = new HashMap<>();
        this.usersByEmail = new HashMap<>();
        DatabaseInitializer.initialize();
        loadFromDatabase();
    }
    
    /**
     * Saves a user to the repository.
     */
    public void save(User user) {
        if (user != null) {
            usersById.put(user.getId(), user);
            usersByEmail.put(user.getEmail().toLowerCase(), user);
            upsertUser(user);
        }
    }
    
    /**
     * Finds a user by ID.
     */
    public User findById(String id) {
        return usersById.get(id);
    }
    
    /**
     * Finds a user by email (case-insensitive).
     */
    public User findByEmail(String email) {
        if (email == null) return null;
        return usersByEmail.get(email.toLowerCase());
    }
    
    /**
     * Returns all users (defensive copy).
     */
    public List<User> findAll() {
        return new ArrayList<>(usersById.values());
    }
    
    /**
     * Returns all passengers.
     */
    public List<Passenger> findAllPassengers() {
        List<Passenger> passengers = new ArrayList<>();
        for (User user : usersById.values()) {
            if (user instanceof Passenger && !(user instanceof Driver)) {
                passengers.add((Passenger) user);
            }
        }
        return passengers;
    }
    
    /**
     * Returns all drivers.
     */
    public List<Driver> findAllDrivers() {
        List<Driver> drivers = new ArrayList<>();
        for (User user : usersById.values()) {
            if (user instanceof Driver) {
                drivers.add((Driver) user);
            }
        }
        return drivers;
    }
    
    /**
     * Returns all admins.
     */
    public List<Admin> findAllAdmins() {
        List<Admin> admins = new ArrayList<>();
        for (User user : usersById.values()) {
            if (user instanceof Admin) {
                admins.add((Admin) user);
            }
        }
        return admins;
    }
    
    /**
     * Deletes a user.
     */
    public void delete(User user) {
        if (user != null) {
            usersById.remove(user.getId());
            usersByEmail.remove(user.getEmail().toLowerCase());
            deleteFromDatabase(user);
        }
    }

    public void update(User user) {
        save(user);
    }
    
    /**
     * Checks if email already exists.
     */
    public boolean existsByEmail(String email) {
        return email != null && usersByEmail.containsKey(email.toLowerCase());
    }
    
    /**
     * Returns the count of all users.
     */
    public int count() {
        return usersById.size();
    }
    
    /**
     * Returns users by status.
     */
    public List<User> findByStatus(UserStatus status) {
        List<User> result = new ArrayList<>();
        for (User user : usersById.values()) {
            if (user.getStatus() == status) {
                result.add(user);
            }
        }
        return result;
    }
    
    /**
     * Clears all users.
     */
    public void clear() {
        usersById.clear();
        usersByEmail.clear();
    }

    private void loadFromDatabase() {
        String sql = "SELECT external_id, first_name, last_name, email, password, phone, role, status, license_number, verified FROM USERS";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String externalId = resultSet.getString("external_id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                String email = resultSet.getString("email");
                String password = resultSet.getString("password");
                String phone = resultSet.getString("phone");
                String role = resultSet.getString("role");
                String status = resultSet.getString("status");
                String license = resultSet.getString("license_number");
                boolean verified = resultSet.getInt("verified") == 1;

                User user = createUserFromRow(role, email, password, firstName, lastName, phone, license, verified);
                if (user == null) {
                    continue;
                }

                alignRuntimeId(user, externalId);
                restoreUserStatus(user, status);
                if (user instanceof Passenger) {
                    loadPaymentMethods(connection, (Passenger) user, externalId);
                }
                if (user instanceof Driver) {
                    loadVehicles(connection, (Driver) user, externalId);
                }

                usersById.put(user.getId(), user);
                usersByEmail.put(user.getEmail().toLowerCase(), user);
            }
        } catch (SQLException e) {
            System.err.println("Failed to load users from database, using in-memory data only: " + e.getMessage());
        }
    }

    private void upsertUser(User user) {
        String sql = """
            INSERT INTO USERS (external_id, first_name, last_name, name, email, password, phone, role, status, license_number, verified)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(external_id) DO UPDATE SET
                first_name=excluded.first_name,
                last_name=excluded.last_name,
                name=excluded.name,
                email=excluded.email,
                password=excluded.password,
                phone=excluded.phone,
                role=excluded.role,
                status=excluded.status,
                license_number=excluded.license_number,
                verified=excluded.verified
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getId());
            statement.setString(2, user.getFirstName());
            statement.setString(3, user.getLastName());
            statement.setString(4, user.getFullName());
            statement.setString(5, user.getEmail());
            statement.setString(6, user.getPassword());
            statement.setString(7, user.getPhoneNumber());
            statement.setString(8, normalizeRole(user.getRole()));
            statement.setString(9, user.getStatus().name());
            statement.setString(10, user instanceof Driver ? ((Driver) user).getLicenseNumber() : null);
            statement.setInt(11, user instanceof Driver && ((Driver) user).isVerified() ? 1 : 0);
            statement.executeUpdate();

            if (user instanceof Passenger) {
                savePaymentMethods(connection, (Passenger) user);
            }
            if (user instanceof Driver) {
                saveVehicles(connection, (Driver) user);
            }
        } catch (SQLException e) {
            System.err.println("Failed to persist user, continuing in-memory only: " + e.getMessage());
        }
    }

    private void deleteFromDatabase(User user) {
        String sql = "DELETE FROM USERS WHERE external_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            deletePaymentMethods(connection, user);
            deleteVehicles(connection, user);
            statement.setString(1, user.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to delete user from database: " + e.getMessage());
        }
    }

    private User createUserFromRow(String role, String email, String password, String firstName, String lastName,
                                   String phone, String license, boolean verified) {
        if ("DRIVER".equalsIgnoreCase(role)) {
            Driver driver = new Driver(email, password, firstName, lastName, phone, license == null ? "" : license);
            if (verified) {
                driver.verify();
            }
            return driver;
        }
        if ("PASSENGER".equalsIgnoreCase(role)) {
            return new Passenger(email, password, firstName, lastName, phone);
        }
        if ("ADMIN".equalsIgnoreCase(role)) {
            return new Admin(email, password, firstName, lastName, phone);
        }
        return null;
    }

    private void restoreUserStatus(User user, String status) {
        if (status == null || status.isEmpty()) {
            return;
        }
        try {
            user.setStatus(UserStatus.valueOf(status));
        } catch (IllegalArgumentException ignored) {
            // Keep default status if legacy value is invalid.
        }
    }

    private String normalizeRole(String role) {
        String value = role == null ? "" : role.toUpperCase(Locale.ROOT);
        if (value.startsWith("ADMIN")) {
            return "ADMIN";
        }
        if (value.contains("DRIVER")) {
            return "DRIVER";
        }
        return "PASSENGER";
    }

    private void alignRuntimeId(User user, String expectedId) {
        if (expectedId == null || expectedId.equals(user.getId())) {
            return;
        }
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, expectedId);
        } catch (ReflectiveOperationException ignored) {
            // If reflection fails, runtime IDs remain transient.
        }
    }

    private void loadPaymentMethods(Connection connection, Passenger passenger, String passengerExternalId) {
        String sql = """
            SELECT pm.external_id, pm.card_number, pm.card_holder_name, pm.expiry_date, pm.cvv, pm.card_type, pm.is_default
            FROM PAYMENT_METHODS pm
            JOIN USERS u ON pm.user_id = u.id
            WHERE u.external_id = ?
            ORDER BY pm.is_default DESC, pm.id ASC
            """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, passengerExternalId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    try {
                        PaymentMethod method = new PaymentMethod(
                            resultSet.getString("external_id"),
                            resultSet.getString("card_number"),
                            resultSet.getString("card_holder_name"),
                            resultSet.getString("expiry_date"),
                            resultSet.getString("cvv"),
                            resultSet.getInt("is_default") == 1
                        );
                        passenger.restorePaymentMethod(method);
                    } catch (RuntimeException e) {
                        System.err.println("Skipping invalid payment method for user " + passengerExternalId + ": " + e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to load payment methods for user " + passengerExternalId + ": " + e.getMessage());
        }
    }

    private void savePaymentMethods(Connection connection, Passenger passenger) {
        deletePaymentMethods(connection, passenger);

        String sql = """
            INSERT INTO PAYMENT_METHODS (external_id, user_id, card_number, card_holder_name, expiry_date, cvv, card_type, is_default)
            VALUES (
                ?,
                (SELECT id FROM USERS WHERE external_id = ?),
                ?,
                ?,
                ?,
                ?,
                ?,
                ?
            )
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (PaymentMethod method : passenger.getPaymentMethods()) {
                statement.setString(1, method.getId());
                statement.setString(2, passenger.getId());
                statement.setString(3, method.getCardNumber());
                statement.setString(4, method.getCardHolderName());
                statement.setString(5, method.getExpiryDate());
                statement.setString(6, method.getCvv());
                statement.setString(7, method.getCardType());
                statement.setInt(8, method.isDefault() ? 1 : 0);
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            System.err.println("Failed to persist payment methods for user " + passenger.getId() + ": " + e.getMessage());
        }
    }

    private void deletePaymentMethods(Connection connection, User user) {
        String sql = """
            DELETE FROM PAYMENT_METHODS
            WHERE user_id = (SELECT id FROM USERS WHERE external_id = ?)
            """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to delete payment methods for user " + user.getId() + ": " + e.getMessage());
        }
    }

    private void loadVehicles(Connection connection, Driver driver, String driverExternalId) {
        String sql = """
            SELECT v.external_id, v.brand, v.model, v.year, v.license_plate, v.color, v.type, v.seats, v.active
            FROM VEHICLES v
            JOIN USERS u ON v.driver_id = u.id
            WHERE u.external_id = ?
            ORDER BY v.id ASC
            """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, driverExternalId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    try {
                        Vehicle vehicle = new Vehicle(
                            resultSet.getString("external_id"),
                            resultSet.getString("brand"),
                            resultSet.getString("model"),
                            resultSet.getInt("year"),
                            resultSet.getString("license_plate"),
                            resultSet.getString("color"),
                            VehicleType.valueOf(resultSet.getString("type")),
                            resultSet.getInt("seats"),
                            resultSet.getInt("active") == 1
                        );
                        driver.addVehicle(vehicle);
                    } catch (RuntimeException e) {
                        System.err.println("Skipping invalid vehicle for driver " + driverExternalId + ": " + e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to load vehicles for driver " + driverExternalId + ": " + e.getMessage());
        }
    }

    private void saveVehicles(Connection connection, Driver driver) {
        deleteVehicles(connection, driver);

        String sql = """
            INSERT INTO VEHICLES (external_id, driver_id, brand, model, year, license_plate, color, type, seats, active)
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
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Vehicle vehicle : driver.getVehicles()) {
                statement.setString(1, vehicle.getId());
                statement.setString(2, driver.getId());
                statement.setString(3, vehicle.getBrand());
                statement.setString(4, vehicle.getModel());
                statement.setInt(5, vehicle.getYear());
                statement.setString(6, vehicle.getLicensePlate());
                statement.setString(7, vehicle.getColor());
                statement.setString(8, vehicle.getType().name());
                statement.setInt(9, vehicle.getSeats());
                statement.setInt(10, vehicle.isActive() ? 1 : 0);
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            System.err.println("Failed to persist vehicles for driver " + driver.getId() + ": " + e.getMessage());
        }
    }

    private void deleteVehicles(Connection connection, User user) {
        String sql = """
            DELETE FROM VEHICLES
            WHERE driver_id = (SELECT id FROM USERS WHERE external_id = ?)
            """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to delete vehicles for user " + user.getId() + ": " + e.getMessage());
        }
    }
}
