package apdfinalproject.dao;

import apdfinalproject.database.DatabaseAccess;
import apdfinalproject.dto.ReservationTableRow;
import apdfinalproject.models.Reservation;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import static apdfinalproject.database.DatabaseAccess.LOGGER;

public class ReservationDAO {

    private final Connection connection;
    private final ReservationRoomsDAO reservationRoomsDAO;
    private final RoomDAO roomDAO;

    public ReservationDAO() throws SQLException {
        this.reservationRoomsDAO = new ReservationRoomsDAO();
        this.roomDAO = new RoomDAO();
        this.connection = DatabaseAccess.getConnection();
    }

    public void createReservation(Reservation reservation) throws SQLException {
        String reservationSql = "INSERT INTO reservations (guest_id, check_in_date, check_out_date, number_of_guests, status) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(reservationSql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, reservation.getGuestID());
            stmt.setString(2, reservation.getCheckInDate().toString());
            stmt.setString(3, reservation.getCheckOutDate().toString());
            stmt.setInt(4, reservation.getNumberOfGuests());
            stmt.setString(5, reservation.getStatus());

            stmt.executeUpdate();
            ResultSet generatedKeys = stmt.getGeneratedKeys();

            if (generatedKeys.next()) {
                int reservationId = generatedKeys.getInt(1);
                // Get the list of room IDs directly from the reservation object
                List<Integer> roomIds = reservation.getRoomIds();

                // Now link rooms using ReservationRoomsDAO
                for (int roomId : roomIds) {
                    reservationRoomsDAO.addRoomToReservation(reservationId, roomId);
                }
            }
        }
    }


    // Cancel a reservation and update room status to AVAILABLE
    public void cancelReservation(int reservationID) throws SQLException {
        // First, update the reservation status to 'CANCELLED'
        String sql = "UPDATE reservations SET status = 'CANCELLED' WHERE reservation_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, reservationID);
            stmt.executeUpdate();
        }

        // Now, update the status of all rooms associated with the reservation to 'AVAILABLE' and keep the historical data in the reservation_rooms table
        String getRoomIdsSql = "SELECT room_id FROM reservation_rooms WHERE reservation_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(getRoomIdsSql)) {
            stmt.setInt(1, reservationID);
            ResultSet rs = stmt.executeQuery();

            // For each room, set room status to AVAILABLE
            while (rs.next()) {
                int roomId = rs.getInt("room_id");
                // Set the room status to 'AVAILABLE'
                roomDAO.updateRoomStatus(roomId, "AVAILABLE");
            }
        }
    }

    public void updateReservation(Reservation reservation) throws SQLException {

        Reservation oldReservation = getReservationById(reservation.getReservationID());
        List<Integer> oldRoomIds = oldReservation.getRoomIds();
        List<Integer> newRoomIds = reservation.getRoomIds();

        // Update the reservation itself
        String reservationSql = "UPDATE reservations SET check_in_date = ?, check_out_date = ?, number_of_guests = ?, status = ? WHERE reservation_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(reservationSql)) {
            stmt.setString(1, reservation.getCheckInDate().toString());
            stmt.setString(2, reservation.getCheckOutDate().toString());
            stmt.setInt(3, reservation.getNumberOfGuests());
            stmt.setString(4, reservation.getStatus());
            stmt.setInt(5, reservation.getReservationID());

            // Update the reservation record
            stmt.executeUpdate();

            // Remove rooms no longer associated with the reservation
            for (int oldRoomId: oldRoomIds) {
                if (!newRoomIds.contains(oldRoomId)) {
                    // Remove the room from the reservation_rooms table
                    reservationRoomsDAO.removeRoomFromReservation(reservation.getReservationID(), oldRoomId);
                    // Update the room's availability to "AVAILABLE"
                }
            }

            // Add new rooms to the reservation and set them as OCCUPIED
            for (int newRoomId : newRoomIds) {
                if (!oldRoomIds.contains(newRoomId)) {
                    // Add the room to the reservation_rooms table
                    reservationRoomsDAO.addRoomToReservation(reservation.getReservationID(), newRoomId);
                }
            }
        }
    }



    // Confirm a reservation for creation
    public void confirmReservation(int reservationID) throws SQLException {
        String sql = "UPDATE reservations SET status = 'CONFIRMED' WHERE reservation_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, reservationID);
            stmt.executeUpdate();
        }
    }

    // Check out a reservation and set all associated rooms to AVAILABLE
    public void checkOutReservation(int reservationID) throws SQLException {
        // First, update the reservation status to 'CHECKED_OUT'
        String sql = "UPDATE reservations SET status = 'CHECKED_OUT' WHERE reservation_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, reservationID);
            stmt.executeUpdate();
        }

        // Now, update the status of all rooms associated with the reservation to 'AVAILABLE'
        String getRoomIdsSql = "SELECT room_id FROM reservation_rooms WHERE reservation_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(getRoomIdsSql)) {
            stmt.setInt(1, reservationID);
            ResultSet rs = stmt.executeQuery();

            // Update each room's status to 'AVAILABLE'
            while (rs.next()) {
                int roomId = rs.getInt("room_id");
                roomDAO.updateRoomStatus(roomId, "AVAILABLE");
            }
        }
    }

    // Get reservation by ID
    public Reservation getReservationById(int reservationId) throws SQLException {
        Reservation reservation = null;
        String reservationSql = "SELECT * FROM reservations WHERE reservation_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(reservationSql)) {
            stmt.setInt(1, reservationId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int guestId = rs.getInt("guest_id");
                LocalDate checkInDate = LocalDate.parse(rs.getString("check_in_date"));
                LocalDate checkOutDate = LocalDate.parse(rs.getString("check_out_date"));
                int numberOfGuests = rs.getInt("number_of_guests");
                String status = rs.getString("status");

                // Get the list of room IDs for this reservation
                List<Integer> roomIds = getRoomIdsForReservation(reservationId);

                // Create the Reservation object
                reservation = new Reservation(reservationId, guestId, checkInDate, checkOutDate, numberOfGuests, status, roomIds);
            }
        }
        return reservation;
    }

    // Search for reservations by booking ID or guest ID
    public ObservableList<Reservation> searchReservationsNoFeedback(String searchTerm) throws SQLException {
        ObservableList<Reservation> reservations = FXCollections.observableArrayList();
        String query = """
        SELECT r.*
        FROM reservations r
        LEFT JOIN feedback f ON r.reservation_id = f.reservation_id
        WHERE f.reservation_id IS NULL
        AND (r.reservation_id LIKE ? OR r.guest_id LIKE ?)
    """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "%" + searchTerm + "%");
            stmt.setString(2, "%" + searchTerm + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int reservationId = rs.getInt("reservation_id");
                    int guestId = rs.getInt("guest_id");
                    LocalDate checkInDate = LocalDate.parse(rs.getString("check_in_date"));
                    LocalDate checkOutDate = LocalDate.parse(rs.getString("check_out_date"));
                    int numberOfGuests = rs.getInt("number_of_guests");
                    String status = rs.getString("status");

                    // Get the list of room IDs for this reservation
                    List<Integer> roomIds = getRoomIdsForReservation(reservationId);

                    // Create the Reservation object
                    Reservation reservation = new Reservation(reservationId, guestId, checkInDate, checkOutDate, numberOfGuests, status, roomIds);
                    reservations.add(reservation);
                }
            }
        } catch (SQLException e) {
            // Log or handle the SQLException
            LOGGER.log(Level.SEVERE, "Error fetching reservations", e);
            throw e; // Rethrow or handle as needed
        }
        return reservations;
    }

    // Search for reservations by booking ID or guest ID
    public ObservableList<Reservation> searchReservations(String searchTerm) throws SQLException {
        ObservableList<Reservation> reservations = FXCollections.observableArrayList();
        String query = "SELECT * FROM reservations WHERE reservation_id LIKE ? OR guest_id LIKE ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "%" + searchTerm + "%");
            stmt.setString(2, "%" + searchTerm + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int reservationId = rs.getInt("reservation_id");
                    int guestId = rs.getInt("guest_id");
                    LocalDate checkInDate = LocalDate.parse(rs.getString("check_in_date"));
                    LocalDate checkOutDate = LocalDate.parse(rs.getString("check_out_date"));
                    int numberOfGuests = rs.getInt("number_of_guests");
                    String status = rs.getString("status");

                    // Get the list of room IDs for this reservation
                    List<Integer> roomIds = getRoomIdsForReservation(reservationId);

                    // Create the Reservation object
                    Reservation reservation = new Reservation(reservationId, guestId, checkInDate, checkOutDate, numberOfGuests, status, roomIds);
                    reservations.add(reservation);
                }
            }
        } catch (SQLException e) {
            // Log or handle the SQLException
            LOGGER.log(Level.SEVERE, "Error fetching reservations", e);
            throw e; // Rethrow or handle as needed
        }
        return reservations;
    }


    public double getTotalPriceForReservation(int reservationId) throws SQLException {
        String sql = "SELECT SUM(ro.price) AS total_price " +
                "FROM reservation_rooms rr " +
                "JOIN rooms ro ON rr.room_id = ro.room_id " +
                "WHERE rr.reservation_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, reservationId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total_price");
            }
        }
        return 0.0;
    }

    private List<Integer> getRoomIdsForReservation(int reservationId) throws SQLException {
        List<Integer> roomIds = new ArrayList<>();
        String roomsSql = "SELECT room_id FROM reservation_rooms WHERE reservation_id = ?";
        try (PreparedStatement roomsStmt = connection.prepareStatement(roomsSql)) {
            roomsStmt.setInt(1, reservationId);
            ResultSet roomsRs = roomsStmt.executeQuery();
            while (roomsRs.next()) {
                roomIds.add(roomsRs.getInt("room_id"));
            }
        }
        return roomIds;
    }

    // Get all reservations
    public ObservableList<Reservation> getAllReservations() throws SQLException {
        ObservableList<Reservation> reservations = FXCollections.observableArrayList();
        String sql = "SELECT * FROM reservations";
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                int reservationID = rs.getInt("reservation_id");
                int guestID = rs.getInt("guest_id");
                LocalDate checkInDate = LocalDate.parse(rs.getString("check_in_date"));
                LocalDate checkOutDate = LocalDate.parse(rs.getString("check_out_date"));
                int numberOfGuests = rs.getInt("number_of_guests");
                String status = rs.getString("status");

                // Get the list of room IDs for this reservation
                List<Integer> roomIds = getRoomIdsForReservation(reservationID);

                reservations.add(new Reservation(reservationID, guestID, checkInDate, checkOutDate, numberOfGuests, status, roomIds));
            }
        }
        return reservations;
    }

    // Delete a reservation
    public void deleteReservation(int reservationId) throws SQLException {
        Connection conn = connection;
        PreparedStatement getRoomIdsStmt = null;
        PreparedStatement deleteReservationRoomsStmt = null;
        PreparedStatement deleteReservationStmt = null;
        PreparedStatement updateGuestStmt = null; // Declare updateGuestStmt here
        try {

            conn.setAutoCommit(false);

            // Get rooms associated with the reservation
            String getRoomIdsSql = "SELECT room_id FROM reservation_rooms WHERE reservation_id = ?";
            getRoomIdsStmt = conn.prepareStatement(getRoomIdsSql);
            getRoomIdsStmt.setInt(1, reservationId);
            ResultSet rs = getRoomIdsStmt.executeQuery();

            // Log room IDs associated with the reservation
            LOGGER.info("Retrieving room IDs for reservation ID: " + reservationId);

            // Make the rooms available again
            while (rs.next()) {
                int roomId = rs.getInt("room_id");
                LOGGER.info("Updating status of room with ID " + roomId + " to AVAILABLE");
                roomDAO.updateRoomStatus(roomId, "AVAILABLE");
            }

            // Delete the record from the bridge table
            String deleteReservationRoomsSql = "DELETE FROM reservation_rooms WHERE reservation_id = ?";
            deleteReservationRoomsStmt = conn.prepareStatement(deleteReservationRoomsSql);
            deleteReservationRoomsStmt.setInt(1, reservationId);
            deleteReservationRoomsStmt.executeUpdate();
            LOGGER.info("Deleted associated records from reservation_rooms for reservation ID: " + reservationId);

            // Update the guest_id to disassociate since we are keeping guest data.
            String updateGuestSql = "UPDATE reservations SET guest_id = NULL WHERE reservation_id = ?";
            updateGuestStmt = conn.prepareStatement(updateGuestSql);
            updateGuestStmt.setInt(1, reservationId);
            updateGuestStmt.executeUpdate();

            // Delete the reservation from the reservations table
            String deleteReservationSql = "DELETE FROM reservations WHERE reservation_id = ?";
            deleteReservationStmt = conn.prepareStatement(deleteReservationSql);
            deleteReservationStmt.setInt(1, reservationId);
            deleteReservationStmt.executeUpdate();

            conn.commit();
            LOGGER.info("Reservation with ID " + reservationId + " and associated rooms successfully deleted.");

        } catch (SQLException e) {
            // Rollback in case of an error
            try {
                conn.rollback();
                LOGGER.warning("Transaction rolled back due to error: " + e.getMessage());
            } catch (SQLException rollbackEx) {
                LOGGER.warning("Failed to roll back transaction: " + rollbackEx.getMessage());
            }
            LOGGER.log(Level.SEVERE, "Error deleting reservation: ", e);
            throw e;
        } finally {
            // Clean up
            try {
                if (getRoomIdsStmt != null) getRoomIdsStmt.close();
                if (deleteReservationRoomsStmt != null) deleteReservationRoomsStmt.close();
                if (deleteReservationStmt != null) deleteReservationStmt.close();
                if (updateGuestStmt != null) updateGuestStmt.close(); // Close updateGuestStmt
                if (conn != null) conn.setAutoCommit(true);
                LOGGER.info("Transaction completed. Auto-commit restored.");
            } catch (SQLException e) {
                LOGGER.warning("Error closing resources: " + e.getMessage());
            }
        }
    }

    public int getNextReservationId() {
        String query = "SELECT MAX(reservation_id) FROM reservations";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1) + 1;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving next reservation ID", e);
        }
        return 1;  // Default to 1 if table is empty
    }



}
