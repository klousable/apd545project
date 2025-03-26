package apdfinalproject.dao;

import apdfinalproject.database.DatabaseAccess;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationRoomsDAO {

    private final Connection connection;
    private final RoomDAO roomDAO;

    public ReservationRoomsDAO() throws SQLException {
        this.connection = DatabaseAccess.getConnection();
        this.roomDAO = new RoomDAO();
    }

    // Add a room to a reservation
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

    // Remove a room from a reservation
    public void removeRoomFromReservation(int reservationId, int roomId) throws SQLException {
        // First, remove the room from the reservation_rooms table
        String sql = "DELETE FROM reservation_rooms WHERE reservation_id = ? AND room_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, reservationId);
            stmt.setInt(2, roomId);
            stmt.executeUpdate();
        }
        roomDAO.updateRoomStatus(roomId, "AVAILABLE");
    }

    // Get all rooms for a reservation
    public List<Integer> getRoomsForReservation(int reservationId) throws SQLException {
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
}
