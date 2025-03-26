package apdfinalproject.controllers;

import apdfinalproject.dao.ReservationDAO;
import apdfinalproject.database.DatabaseAccess;
import apdfinalproject.models.Admin;
import apdfinalproject.models.Kiosk;
import apdfinalproject.models.Reservation;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import apdfinalproject.models.Feedback;
import apdfinalproject.dao.FeedbackDAO;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FeedbackController {

    private FeedbackDAO feedbackDAO;
    private ReservationDAO reservationDAO;
    private int selectedReservationId;
    private static final Logger LOGGER = DatabaseAccess.LOGGER;
    private Admin admin;
    private Kiosk kiosk;

    @FXML
    private Label feedbackTitle;
    @FXML
    private TextField reservationIdField;
    @FXML
    private TextField feedbackIdField;
    @FXML
    private TextField guestIdField;
    @FXML
    private Slider feedbackRatingField;
    @FXML
    private TextArea feedbackCommentArea;
    @FXML
    private Button saveButton;
    @FXML
    private Button clearButton;
    @FXML
    private Button cancelButton;

    public FeedbackController() throws SQLException {
        this.reservationDAO = new ReservationDAO();
        this.feedbackDAO = new FeedbackDAO();
    }

    @FXML
    public void initialize() throws SQLException {
        LOGGER.info("Initializing FeedbackController...");

        feedbackDAO = new FeedbackDAO();
        saveButton.setDisable(true);
        clearButton.setDisable(false);
        cancelButton.setDisable(false);
        // Assume that we are adding feedback first so load the next feedback ID for view
        feedbackIdField.setText(String.valueOf(feedbackDAO.getNextFeedbackId()));

        Platform.runLater(() -> {
            if (selectedReservationId != 0 && this.admin != null) {
                try {
                    loadFeedbackData();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Errors loading feedback data", e);
                }
            } else if (selectedReservationId != 0 && this.kiosk != null) {
                try {
                    loadReservationData();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Errors loading reservation data", e);
                }
            }
        });
    }

    // Set the selected reservation ID for querying from the admin view
    public void setReservationId(int reservationId) {
        LOGGER.info("Setting selected reservation ID: " + reservationId);
        this.selectedReservationId = reservationId;
    }

    // Set the admin for logging purposes
    public void setAdmin(Admin admin) {
        if (admin != null) {
            this.admin = admin;
            LOGGER.info("Admin " + admin.getUsername() + " viewing Feedback for reservation ID: " + selectedReservationId);
        }
    }

    // Set the admin for logging purposes
    public void setKiosk(Kiosk kiosk) {
        this.kiosk = kiosk;
    }

    private void loadFeedbackData() throws SQLException {
        LOGGER.info("Loading feedback data for reservation ID: " + selectedReservationId);
        Feedback feedback = feedbackDAO.getFeedbackByReservationId(selectedReservationId);

        if (feedback != null) {
            LOGGER.info("Feedback found for reservation ID: " + selectedReservationId);
            loadFeedback(feedback);
        } else {
            LOGGER.warning("No feedback found for reservation ID: " + selectedReservationId);
            feedbackCommentArea.setText("No feedback submitted.");
        }
    }

    // Populate the feedback form with existing feedback data
    public void loadFeedback(Feedback feedback) {
        feedbackTitle.setText("View Feedback");

        feedbackIdField.setText(String.valueOf(feedback.getFeedbackId()));
        guestIdField.setText(String.valueOf(feedback.getGuestId()));
        reservationIdField.setText(String.valueOf(feedback.getReservationId()));
        feedbackCommentArea.setText(feedback.getComments());
        feedbackRatingField.setValue(feedback.getRating());

        // Prevent admin from changing feedback from guests
        saveButton.setDisable(true);
        clearButton.setDisable(true);
        feedbackCommentArea.setDisable(true);
        feedbackRatingField.setDisable(true);
    }

    private void loadReservationData() throws SQLException {
        LOGGER.info("Loading reservation data for reservation ID: " + selectedReservationId);
        Reservation reservation = reservationDAO.getReservationById(selectedReservationId);

        if (reservation != null) {
            LOGGER.info("Feedback found for reservation ID: " + selectedReservationId);
            loadReservation(reservation);
        } else {
            LOGGER.warning("No feedback found for reservation ID: " + selectedReservationId);
            feedbackCommentArea.setText("No feedback submitted.");
        }
    }

    public void loadReservation(Reservation reservation) {
        guestIdField.setText(String.valueOf(reservation.getGuestID()));
        reservationIdField.setText(String.valueOf(reservation.getReservationID()));
    }

    @FXML
    private void handleSaveButton(ActionEvent event) {

        // Get values from the fields
        String comments = feedbackCommentArea.getText();
        int rating = (int) feedbackRatingField.getValue();
        int feedbackId = Integer.parseInt(feedbackIdField.getText());
        int reservationId = Integer.parseInt(reservationIdField.getText());
        int guestId = Integer.parseInt(guestIdField.getText());

        // Create the Feedback object
        Feedback feedback = new Feedback(feedbackId, guestId, reservationId, comments, rating);

        try {
            LOGGER.info("Saving feedback for reservation ID: " + reservationId);
            // Save feedback using DAO
            feedbackDAO.createFeedback(feedback);
            LOGGER.info("Feedback saved successfully from Kiosk: " + kiosk.getKioskID() + "at Location: " + kiosk.getLocation());
        } catch (SQLException e) {
            LOGGER.severe("Error saving feedback: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Handle the clear button click event
    @FXML
    private void handleClearAction(ActionEvent event) {
        // Clear fields
        reservationIdField.clear();
        feedbackIdField.clear();
        guestIdField.clear();
        feedbackCommentArea.clear();
        feedbackRatingField.setValue(1);  // Reset rating to default
    }

    // Handle the cancel button click event
    @FXML
    private void handleCancelAction(ActionEvent actionEvent) {

        // Only show a warning message if a guest is leaving the kiosk
        if (kiosk != null) {
            showAlert("Exit Confirmation", "Are you sure you want to exit? Any unsaved data will be lost.", Alert.AlertType.CONFIRMATION, () -> {
                // Reset selected reservation
                this.selectedReservationId = 0;
                Stage currentStage = (Stage) cancelButton.getScene().getWindow();
                currentStage.close();
            });
        // Admin is viewing feedback, close without warning
        } else {
            Stage currentStage = (Stage) cancelButton.getScene().getWindow();
            currentStage.close();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type, Runnable onConfirmAction) {
        // Create and configure the alert
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(message);

        // Show the alert and wait for the user's response
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK && onConfirmAction != null) {
                onConfirmAction.run();
            }
        });
    }

}