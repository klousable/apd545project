package apdfinalproject.dao;

import apdfinalproject.database.DatabaseAccess;
import apdfinalproject.models.Room;
import apdfinalproject.models.RoomType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static apdfinalproject.database.DatabaseAccess.LOGGER;

public class ReservationRoomsDAO {

    private final Connection connection;
    private final RoomDAO roomDAO;

    public ReservationRoomsDAO() throws SQLException {
        this.connection = DatabaseAccess.getConnection();
        this.roomDAO = new RoomDAO();
    }

    public void addRoomToReservation(int reservationId, int roomId) throws SQLException {
        // First, add the room to the reservation_rooms table
        String sql = "INSERT INTO reservation_rooms (reservation_id, room_id) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, reservationId);
            stmt.setInt(2, roomId);
            stmt.executeUpdate();
        }
        // Now, update the room status to 'OCCUPIED'
        roomDAO.updateRoomStatus(roomId, "OCCUPIED");
    }

    public void removeRoomFromReservation(int reservationId, int roomId) throws SQLException {

        // First, remove the room from the reservation_rooms table
        String sql = "DELETE FROM reservation_rooms WHERE reservation_id = ? AND room_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, reservationId);
            stmt.setInt(2, roomId);
            stmt.executeUpdate();
        }
        // Now, update the room status to 'AVAILABLE'
        roomDAO.updateRoomStatus(roomId, "AVAILABLE");
    }

    // Get all rooms for a reservation
    public List<Integer> getRoomIdsForReservation(int reservationId) throws SQLException {
        List<Integer> roomIds = new ArrayList<>();
        String sql = "SELECT room_id FROM reservation_rooms WHERE reservation_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, reservationId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                roomIds.add(rs.getInt("room_id"));
            }
        }
        return roomIds;
    }

    public ObservableList<Room> getAllRoomsForReservation(int reservationId) throws SQLException {
        ObservableList<Room> rooms = FXCollections.observableArrayList();

        String sql = "SELECT r.room_id, r.room_type, r.number_of_beds, r.price, r.status " +
                "FROM reservation_rooms rr " +
                "JOIN rooms r ON rr.room_id = r.room_id " +
                "WHERE rr.reservation_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, reservationId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int roomID = rs.getInt("room_id");
                    String roomTypeStr = rs.getString("room_type");
                    int numberOfBeds = rs.getInt("number_of_beds");
                    double price = rs.getDouble("price");
                    String status = rs.getString("status");

                    // Assuming RoomType is an enum, adjust as needed
                    RoomType roomType = RoomType.valueOf(roomTypeStr.toUpperCase());

                    // Create Room object
                    Room room = new Room(roomID, roomType, reservationId, numberOfBeds, price, status);
                    rooms.add(room);
                }
            }
        }
        return rooms;
    }


}
