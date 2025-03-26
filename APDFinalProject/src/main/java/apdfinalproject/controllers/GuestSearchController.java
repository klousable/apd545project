package apdfinalproject.controllers;

import apdfinalproject.database.DatabaseAccess;
import apdfinalproject.dto.ReservationTableRow;
import apdfinalproject.models.Guest;
import apdfinalproject.models.Kiosk;
import apdfinalproject.models.Reservation;
import apdfinalproject.dao.ReservationDAO;
import apdfinalproject.dao.FeedbackDAO;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;
import javafx.event.ActionEvent;

import java.sql.SQLException;

public class GuestSearchController {

    private ReservationDAO reservationDAO;
    private Kiosk kiosk;
    private Logger LOGGER = DatabaseAccess.LOGGER;

    // FXML UI components
    @FXML
    private TableView<Reservation> reservationTableView;
    @FXML
    private TableColumn<?, ?> bookingIdColumn;
    @FXML
    private TableColumn<?, ?> guestIdColumn;
    @FXML
    private TableColumn<?, ?> checkOutDateColumn;
    @FXML
    private TableColumn<?, ?> checkInDateColumn;
    @FXML
    private TextField bookingSearch;
    @FXML
    private Button selectBookingButton;
    @FXML
    private Button cancelButton;

    public GuestSearchController() throws SQLException {
        this.reservationDAO = new ReservationDAO();
    }

    public void initialize() {

        // Set up TableView columns
        bookingIdColumn.setCellValueFactory(new PropertyValueFactory<>("reservationID"));
        guestIdColumn.setCellValueFactory(new PropertyValueFactory<>("guestID"));
        checkInDateColumn.setCellValueFactory(new PropertyValueFactory<>("checkInDate"));
        checkOutDateColumn.setCellValueFactory(new PropertyValueFactory<>("checkOutDate"));

        // Disable Select button until a booking is selected through binding
        selectBookingButton.disableProperty().bind(Bindings.isEmpty(reservationTableView.getSelectionModel().getSelectedItems()));
        // Listener on search field
        bookingSearch.textProperty().addListener((observable, oldValue, newValue) -> handleSearchBookingAction(newValue));

    }

    // set Kiosk for logging guest feedback
    public void setKiosk(Kiosk kiosk) {
        this.kiosk = kiosk;

    }

    public void handleSearchBookingAction(String searchTerm) {
        // Trim the input text to avoid leading/trailing spaces
        searchTerm = searchTerm.trim();

        if (!searchTerm.isEmpty()) {
            try {
                // Call your search function here with the non-empty search term
                ObservableList<Reservation> searchResults = reservationDAO.searchReservationsNoFeedback(searchTerm);
                if (searchResults.isEmpty()) {
                    // Clear the table if no results are found
                    reservationTableView.getItems().clear();
                } else {
                    // Populate TableView
                    reservationTableView.getItems().setAll(searchResults);
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error searching for reservations", e);
            }
        } else {
            // Clear table if nothing is in the search for privacy reasons
            reservationTableView.getItems().clear();
        }
    }


    public void handleSelectBookingAction(ActionEvent actionEvent) {
        Reservation selectedReservation = reservationTableView.getSelectionModel().getSelectedItem();

        if (selectedReservation == null) {
            showAlert("No Selection", "Please select a reservation to check out.", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/feedback.fxml"));
            Parent root = loader.load();

            // Pass reservationId to FeedbackController
            FeedbackController feedbackController = loader.getController();
            feedbackController.setReservationId(selectedReservation.getReservationID());

            // Set kiosk for logging purposes
            feedbackController.setKiosk(kiosk);

            // Create a new stage and set the scene
            Stage feedbackStage = new Stage();
            feedbackStage.setTitle("Billing Details");
            feedbackStage.initModality(Modality.APPLICATION_MODAL);
            feedbackStage.setScene(new Scene(root));  // Only set the scene once here

            feedbackStage.show();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load feedback.fxml", e);
            showAlert("Error", "Unable to load the feedback screen: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancelAction(ActionEvent actionEvent) {
        showAlert("Exit Confirmation", "Are you sure you want to exit? Any unsaved data will be lost.", Alert.AlertType.CONFIRMATION, () -> {
            Stage currentStage = (Stage) cancelButton.getScene().getWindow();
            currentStage.close();
        });
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
