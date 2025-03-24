package apdfinalproject.dao;

import apdfinalproject.database.DatabaseAccess;
import apdfinalproject.models.Feedback;

import java.sql.*;
import java.util.logging.Logger;

public class FeedbackDAO {
    private final Connection connection;
    private final Logger LOGGER = DatabaseAccess.LOGGER;

    public FeedbackDAO() throws SQLException {
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
