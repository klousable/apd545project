package apdfinalproject.dao;

import apdfinalproject.database.DatabaseAccess;
import apdfinalproject.models.Guest;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GuestDAO {

    private final Connection connection;
    private static final Logger LOGGER = DatabaseAccess.LOGGER;

    public GuestDAO() throws SQLException {
        try {
            connection = DatabaseAccess.getConnection();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database connection error in GuestDAO.", e);
            throw e;
        }
    }

    // Get a guest by their ID
    public Guest getGuestById(int guestId) throws SQLException {
        Guest guest = null;
        String sql = "SELECT * FROM guests WHERE guest_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, guestId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String guestName = rs.getString("name");
                String guestPhoneNumber = rs.getString("phone_number");
                String guestEmail = rs.getString("email");
                String guestAddress = rs.getString("address");
                guest = new Guest(guestId, guestName, guestPhoneNumber, guestEmail, guestAddress);
            }
        }
        return guest;
    }

    // Method to add a new guest to the database
    public void createGuest(Guest guest) throws SQLException {
        String sql = "INSERT INTO guests (name, phone_number, email, address) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, guest.getName());
            stmt.setString(2, guest.getPhoneNumber());
            stmt.setString(3, guest.getEmail());
            stmt.setString(4, guest.getAddress());
            stmt.executeUpdate();
        }
    }

    // Get guest name by guest ID
    public String getGuestNameById(int guestId) throws SQLException {
        String sql = "SELECT name FROM guests WHERE guest_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, guestId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("name");
            }
        }
        return null; // Return null if no guest found with that ID
    }

    // Get guest by phone number
    public Guest getGuestByPhoneNumber(String phoneNumber) throws SQLException {
        Guest guest = null;
        String sql = "SELECT * FROM guests WHERE phone_number = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, phoneNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int guestId = rs.getInt("guest_id");
                String guestName = rs.getString("name");
                String guestPhoneNumber = rs.getString("phone_number");
                String guestEmail = rs.getString("email");
                String guestAddress = rs.getString("address");
                guest = new Guest(guestId, guestName, guestPhoneNumber, guestEmail, guestAddress);
            }
        }
        return guest;
    }

    public Guest getGuestByReservationId(int reservationId) throws SQLException {
        // SQL query to join the reservations and guests tables on guest_id
        String sql = "SELECT g.guest_id, g.name, g.phone_number, g.email, g.address FROM guests g " +
                "JOIN reservations r ON g.guest_id = r.guest_id " +
                "WHERE r.reservation_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            // Set the reservation ID as the parameter
            stmt.setInt(1, reservationId);

            // Execute the query
            ResultSet rs = stmt.executeQuery();

            // If a guest is found, create and return the Guest object
            if (rs.next()) {
                int guestId = rs.getInt("guest_id");
                String guestName = rs.getString("name");
                String phoneNumber = rs.getString("phone_number");
                String email = rs.getString("email");
                String address = rs.getString("address");
                // Return a new Guest object using the fetched data
                return new Guest(guestId, guestName, phoneNumber, email, address);
            }
        }

        // Return null if no guest is found for the given reservation ID
        return null;
    }

    public int getNextGuestId() {
        String query = "SELECT MAX(guest_id) FROM guests";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1) + 1;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving next guest ID", e);
        }
        return 1;  // Default to 1 if table is empty
    }

    // Get guest phone number by guest ID
    public String getGuestPhoneNumberById(int guestId) throws SQLException {
        String sql = "SELECT phone_number FROM guests WHERE guest_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, guestId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("phone_number");
            }
        }
        return null; // Return null if no guest found with that ID
    }

    // Method to update a guest's details
    public void updateGuest(Guest guest) throws SQLException {
        String sql = "UPDATE guests SET name = ?, phone_number = ?, email = ?, address = ? WHERE guest_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, guest.getName());
            stmt.setString(2, guest.getPhoneNumber());
            stmt.setString(3, guest.getEmail());
            stmt.setString(4, guest.getAddress());
            stmt.setInt(5, guest.getGuestId());
            stmt.executeUpdate();
        }
    }

    // Method to delete a guest from the database
    public void deleteGuest(int guestId) throws SQLException {
        String sql = "DELETE FROM guests WHERE guest_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, guestId);
            stmt.executeUpdate();
        }
    }

    // Get all guests
    public ObservableList<Guest> getAllGuests() throws SQLException {
        ObservableList<Guest> guests = FXCollections.observableArrayList();
        String sql = "SELECT * FROM guests";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int guestId = rs.getInt("guest_id");
                String guestName = rs.getString("name");
                String guestPhoneNumber = rs.getString("phone_number");
                String guestEmail = rs.getString("email");
                String guestAddress = rs.getString("address");
                guests.add(new Guest(guestId, guestName, guestPhoneNumber, guestEmail, guestAddress));
            }
        }
        return guests;
    }

}
