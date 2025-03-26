package apdfinalproject.dao;

import apdfinalproject.database.DatabaseAccess;
import apdfinalproject.models.Room;
import apdfinalproject.models.RoomType;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;

import static apdfinalproject.database.DatabaseAccess.LOGGER;

public class RoomDAO {

    private final Connection connection;

    public RoomDAO() throws SQLException {
        this.connection = DatabaseAccess.getConnection();
    }

    // Update the room's status (OCCUPIED or AVAILABLE)
    public void updateRoomStatus(int roomId, String newStatus) throws SQLException {
        LOGGER.info("Starting to update room status for room with ID: " + roomId);

        // Create the base SQL query
        String sql = "UPDATE rooms SET status = ?";

        // Modify the query to set reservation_id to NULL only if status is AVAILABLE
        if (newStatus.equals("AVAILABLE")) {
            sql += ", reservation_id = NULL"; // Set reservation_id to NULL only if status is AVAILABLE
        }

        sql += " WHERE room_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, roomId);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                LOGGER.info("Successfully updated room with ID " + roomId + " status to " + newStatus);
            } else {
                LOGGER.warning("No room found with ID: " + roomId + " for status update.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating room status for room ID: " + roomId, e);
            throw e;
        }
    }

    // Get room by ID
    public Room getRoomById(int roomId) throws SQLException {
        String sql = "SELECT * FROM rooms WHERE room_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                RoomType roomType = RoomType.valueOf(rs.getString("room_type").toUpperCase());
                return new Room(
                        rs.getInt("room_id"),
                        roomType,
                        rs.getInt("reservation_id"),
                        rs.getInt("number_of_beds"),
                        rs.getDouble("price"),
                        rs.getString("status")
                );
            }
        }
        return null;
    }

    public ObservableList<Room> getAllRooms() throws SQLException {
        ObservableList<Room> rooms = FXCollections.observableArrayList();
        String sql = "SELECT * FROM rooms";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int roomID = rs.getInt("room_id");
                String roomTypeStr = rs.getString("room_type");
                int numberOfBeds = rs.getInt("number_of_beds");
                double price = rs.getDouble("price");
                int reservationID = rs.getInt("reservation_id");
                String status = rs.getString("status");

                RoomType roomType = RoomType.valueOf(roomTypeStr.toUpperCase());
                Room room = new Room(roomID, roomType, reservationID, numberOfBeds, price, status);
                rooms.add(room);
            }
        }
        return rooms;
    }

    public ObservableList<Room> getAllAvailableRooms() throws SQLException {
        ObservableList<Room> rooms = FXCollections.observableArrayList();
        String sql = "SELECT * FROM rooms WHERE status = 'AVAILABLE'";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int roomID = rs.getInt("room_id");
                String roomTypeStr = rs.getString("room_type");
                int numberOfBeds = rs.getInt("number_of_beds");
                double price = rs.getDouble("price");
                int reservationID = rs.getInt("reservation_id");
                String status = rs.getString("status");

                RoomType roomType = RoomType.valueOf(roomTypeStr.toUpperCase());
                Room room = new Room(roomID, roomType, reservationID, numberOfBeds, price, status);
                rooms.add(room);
            }
        }
        return rooms;
    }

    // Update entire room record
    public void updateRoom(Room room) throws SQLException {
        String sql = "UPDATE rooms SET room_type = ?, number_of_beds = ?, price = ?, reservation_id = ?, status = ? WHERE room_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, room.getRoomType().name());
            stmt.setInt(2, room.getNumberOfBeds());
            stmt.setDouble(3, room.getPrice());
            stmt.setInt(4, room.getReservationID());
            stmt.setString(5, room.getStatus());
            stmt.setInt(6, room.getRoomID());
            stmt.executeUpdate();
        }
    }

    // Delete room
    public void deleteRoom(int roomId) throws SQLException {
        String sql = "DELETE FROM rooms WHERE room_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            stmt.executeUpdate();
        }
    }
}
