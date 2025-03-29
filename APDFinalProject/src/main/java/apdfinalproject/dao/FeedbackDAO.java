package apdfinalproject.dao;

import apdfinalproject.database.DatabaseAccess;
import apdfinalproject.models.Feedback;

import java.sql.*;
import java.util.logging.Logger;
import java.util.logging.Level;

public class FeedbackDAO {
    private final Connection connection;
    private final Logger LOGGER = DatabaseAccess.LOGGER;
    private final GuestDAO guestDAO;

    public FeedbackDAO() throws SQLException {
        this.guestDAO = new GuestDAO();
        this.connection = DatabaseAccess.getConnection();
    }

    // Get feedback status by reservation ID, returns "Submitted" if feedback exists, "N/A" if none exists
    public String getFeedbackStatusByReservationId(int reservationId) throws SQLException {
        String sql = "SELECT feedback_id FROM feedback WHERE reservation_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, reservationId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return "Submitted"; // Feedback exists for the reservation
            }
        }
        return "N/A"; // No feedback exists for the reservation
    }

    public void updateFeedbackGuestByReservationId(int reservationId) {
        // SQL query to get the guest_id from the reservation
        String sql = "SELECT guest_id FROM reservations WHERE reservation_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, reservationId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int guestId = rs.getInt("guest_id");

                // Now update the guest_id in the feedback table based on the reservation_id
                String feedbackSql = "UPDATE feedback SET guest_id = ? WHERE reservation_id = ?";
                try (PreparedStatement feedbackStmt = connection.prepareStatement(feedbackSql)) {
                    feedbackStmt.setInt(1, guestId); // Set the guest_id
                    feedbackStmt.setInt(2, reservationId); // Set the reservation_id

                    int rowsUpdated = feedbackStmt.executeUpdate(); // Execute the update

                    if (rowsUpdated > 0) {
                    } else {
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Log or handle the exception as per your requirements
        }
    }

    public int getNextFeedbackId() {
        String query = "SELECT MAX(feedback_id) FROM feedback";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1) + 1;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving next feedback ID", e);
        }
        return 1;  // Default to 1 if table is empty
    }

    public Feedback getFeedbackByReservationId(int reservationId) throws SQLException {
        LOGGER.info("Querying feedback for Reservation ID: " + reservationId);
        String sql = "SELECT * FROM feedback WHERE reservation_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, reservationId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // Fetch the data from the result set
                int feedbackId = rs.getInt("feedback_id");
                int guestId = rs.getInt("guest_id");
                String comments = rs.getString("comments");
                int rating = rs.getInt("rating");
                return new Feedback(feedbackId, guestId, reservationId, comments, rating);
            }
        }
        return null; // No feedback object exists
    }

    public void createFeedback(Feedback feedback) throws SQLException {
        String sql = "INSERT INTO feedback (guest_id, reservation_id, comments, rating) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // Set parameters
            stmt.setInt(1, feedback.getGuestId());
            stmt.setInt(2, feedback.getReservationId());
            stmt.setString(3, feedback.getComments());
            stmt.setInt(4, feedback.getRating());

            // Execute the insert statement
            int rowsAffected = stmt.executeUpdate();

            // Retrieve the generated feedback_id
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        feedback.setFeedbackId(generatedKeys.getInt(1)); // Set the generated feedback ID
                    }
                }
            }
        }
    }


}
