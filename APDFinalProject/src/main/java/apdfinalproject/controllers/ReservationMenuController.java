package apdfinalproject.controllers;

import apdfinalproject.dao.FeedbackDAO;
import apdfinalproject.dao.GuestDAO;
import apdfinalproject.dao.ReservationDAO;
import apdfinalproject.dao.RoomDAO;
import apdfinalproject.database.DatabaseAccess;
import apdfinalproject.dto.ReservationTableRow;
import apdfinalproject.models.Admin;
import apdfinalproject.models.Reservation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.beans.binding.Bindings;


import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReservationMenuController {

    private Admin admin;
    private ReservationDAO reservationDAO;
    private GuestDAO guestDAO;
    private FeedbackDAO feedbackDAO;
    private RoomDAO roomDAO;
    private static final Logger LOGGER = DatabaseAccess.LOGGER;

    // TableView and columns
    @FXML
    private TableView<ReservationTableRow> reservationTableView;

    @FXML
    private TableColumn<ReservationTableRow, Integer> reservationIdColumn;
    @FXML
    private TableColumn<ReservationTableRow, Integer> guestIdColumn;
    @FXML
    private TableColumn<ReservationTableRow, String> guestNameColumn;
    @FXML
    private TableColumn<ReservationTableRow, String> guestPhoneNumberColumn;
    @FXML
    private TableColumn<ReservationTableRow, LocalDate> reservationCheckInDateColumn;
    @FXML
    private TableColumn<ReservationTableRow, LocalDate> reservationCheckOutDateColumn;
    @FXML
    private TableColumn<ReservationTableRow, String> reservationStatusColumn;
    @FXML
    private TableColumn<ReservationTableRow, Double> reservationPriceColumn;
    @FXML
    private TableColumn<ReservationTableRow, String> reservationFeedbackStatusColumn;


    // Labels and TextFields
    @FXML
    private Label reservationAdminLabel;
    @FXML
    private TextField bookingSearchField;

    // Buttons
    @FXML
    private Button viewAvailableRoomsButton;
    @FXML
    private Button viewFeedbackButton;
    @FXML
    private Button addReservationButton;
    @FXML
    private Button editReservationButton;
    @FXML
    private Button checkoutButton;
    @FXML
    private Button deleteReservationButton;
    @FXML
    private Button exitButton;
    @FXML
    private Button cancelReservationButton;

    public ReservationMenuController() throws SQLException {
        this.reservationDAO = new ReservationDAO();
        this.guestDAO = new GuestDAO();
        this.feedbackDAO = new FeedbackDAO();
        this.roomDAO = new RoomDAO();
        // remove after debugging
        this.admin = new Admin(1, "admin1", "pass1");
    }

    @FXML
    public void initialize() {

        // Add listener for viewing feedback button: only reservations with feedback can use the button.
        reservationTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {

                // Get the feedback status of the selected reservation
                String feedbackStatus = newValue.getFeedbackStatus();
                // Get the reservation status of the selected reservation
                String reservationStatus = newValue.getStatus();
                // Enable the View Feedback button if feedback is "Submitted", otherwise disable it
                viewFeedbackButton.setDisable(!"Submitted".equals(feedbackStatus));
                // Enable the Cancel button if status is not CANCELLED and an item is selected
                cancelReservationButton.setDisable(reservationStatus.equals("CANCELLED") ||
                        reservationStatus.equals("CHECKED_OUT") ||
                        reservationTableView.getSelectionModel().getSelectedItem() == null);  // Disable button if status is CANCELLED or nothing is selected
//

                // Change Checkout button text to "View Billing" if the reservation is checked out
                // Change Modify to View Detail if the reservation is checked out
                if ("CHECKED_OUT".equals(reservationStatus)) {
                    checkoutButton.setText("View Billing");
                    editReservationButton.setText("View Detail");
                } else {
                    checkoutButton.setText("Checkout");
                    editReservationButton.setText("Modify");
                }

            }
        });

        // Set up the reservation menu dashboard
        reservationIdColumn.setCellValueFactory(cellData -> cellData.getValue().reservationIdProperty().asObject());
        guestIdColumn.setCellValueFactory(cellData -> cellData.getValue().guestIdProperty().asObject());
        guestNameColumn.setCellValueFactory(cellData -> cellData.getValue().guestNameProperty());
        guestPhoneNumberColumn.setCellValueFactory(cellData -> cellData.getValue().guestPhoneNumberProperty());
        reservationCheckInDateColumn.setCellValueFactory(cellData -> cellData.getValue().checkInDateProperty());
        reservationCheckOutDateColumn.setCellValueFactory(cellData -> cellData.getValue().checkOutDateProperty());
        reservationStatusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        reservationPriceColumn.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asObject());
        reservationFeedbackStatusColumn.setCellValueFactory(cellData -> cellData.getValue().feedbackStatusProperty());

        bookingSearchField.textProperty().addListener((observable, oldValue, newValue) -> handleSearchBookingAction(newValue));

        // Call the method to populate the table with data
        refreshTable();

        // Disable CRUD buttons if nothing is selected through binding
        deleteReservationButton.disableProperty().bind(Bindings.isEmpty(reservationTableView.getSelectionModel().getSelectedItems()));
        editReservationButton.disableProperty().bind(Bindings.isEmpty(reservationTableView.getSelectionModel().getSelectedItems()));
        cancelReservationButton.setDisable(reservationTableView.getSelectionModel().getSelectedItem() == null);  // Disable button if status is CANCELLED or nothing is selected
        checkoutButton.disableProperty().bind(Bindings.isEmpty(reservationTableView.getSelectionModel().getSelectedItems()));
    }

    // Method to refresh and populate the table
    private void refreshTable() {
        try {
            ObservableList<ReservationTableRow> rows = FXCollections.observableArrayList();

            for (Reservation reservation : reservationDAO.getAllReservations()) {
                int guestId = reservation.getGuestID();
                String guestName = guestDAO.getGuestNameById(guestId);
                String guestPhone = guestDAO.getGuestPhoneNumberById(guestId);

                // Retrieve total price through reservation DAO
                double totalPrice = reservationDAO.getTotalPriceForReservation(reservation.getReservationID());
                // Retrieve feedback status through feedback DAO
                String feedbackStatus = feedbackDAO.getFeedbackStatusByReservationId(reservation.getReservationID());

                // Add a new row to the observable list
                rows.add(new ReservationTableRow(
                        reservation.getReservationID(),
                        guestId,
                        guestName,
                        guestPhone,
                        reservation.getCheckInDate(),
                        reservation.getCheckOutDate(),
                        reservation.getStatus(),
                        totalPrice,
                        feedbackStatus
                ));
            }

            // Set the rows to the table
            reservationTableView.setItems(rows);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading reservation data", e);
            showAlert("Error", "Failed to load reservation data: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }


    public void setAdmin(Admin admin) {
        this.admin = admin;
        reservationAdminLabel.setText("Reservation Management: (" + admin.getUsername() + ")");
    }

    @FXML
    private void handleViewAvailableRoomsAction(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/available-rooms.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Available Rooms");
            stage.show();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Admin: " + admin.getUsername() + " could not load available rooms", e);
            // Alert to the user if there is an error loading the file
            showAlert("Error", "Could not load available rooms view: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleViewFeedback(ActionEvent actionEvent) {
        // Get the selected reservation
        ReservationTableRow selectedReservation = reservationTableView.getSelectionModel().getSelectedItem();

        if (selectedReservation != null && "Submitted".equals(selectedReservation.getFeedbackStatus())) {
            try {
                // Load the feedback.fxml
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/feedback.fxml"));
                Parent root = loader.load();

                // Pass only the reservationId to the FeedbackController
                FeedbackController feedbackController = loader.getController();
                feedbackController.setReservationId(selectedReservation.getReservationId());
                feedbackController.setAdmin(this.admin);

                // Create a new Stage for the feedback view
                Stage stage = new Stage();
                stage.setTitle("Feedback for Reservation ID: " + selectedReservation.getReservationId());
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error loading feedback window", e);
                showAlert("Error", "Failed to load feedback view: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void handleSearchBookingAction(String newValue) {
        if (newValue == null || newValue.isEmpty()) {
            refreshTable();
        } else {
            FilteredList<ReservationTableRow> filteredList = new FilteredList<>(reservationTableView.getItems(), reservation -> {
                // Check if guest name or phone number matches the search term (case-insensitive)
                String searchTerm = newValue.toLowerCase();
                return reservation.getGuestName().toLowerCase().contains(searchTerm) ||
                        reservation.getGuestPhoneNumber().toLowerCase().contains(searchTerm);
            });

            // Set the filtered list to the table view
            reservationTableView.setItems(filteredList);
        }
    }


    @FXML
    private void handleAddReservationAction(ActionEvent actionEvent) {
        try {
            // Load the Reservation Menu FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/reservation.fxml"));
            Parent reservationRoot = loader.load();

            // Get the ReservationController and pass the Admin object
            ReservationController reservationController = loader.getController();
            reservationController.setAdmin(this.admin);

            // Get the current stage (main window)
            Stage mainStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

            // Dim the main window
            mainStage.setOpacity(0.5);

            // Create a new stage for the reservation form
            Stage reservationStage = new Stage();
            reservationStage.setTitle("Make a Reservation");
            reservationStage.initModality(Modality.APPLICATION_MODAL);
            reservationStage.initOwner(mainStage);
            reservationStage.setScene(new Scene(reservationRoot));

            // Restore main window when the reservation form closes
            reservationStage.setOnCloseRequest(event -> {
                mainStage.setOpacity(1.0);
                mainStage.toFront();
            });

            reservationStage.showAndWait();

            // Ensure table refresh after the reservation window is closed
            refreshTable();

            // Bring back the main window
            mainStage.setOpacity(1.0);
            mainStage.toFront();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening the reservation menu: ", e);
        }
    }

    @FXML
    private void handleEditReservationAction(ActionEvent actionEvent) {
        ReservationTableRow selectedReservation = reservationTableView.getSelectionModel().getSelectedItem();

        if (selectedReservation == null) {
            showAlert("No Selection", "Please select a reservation to edit.", Alert.AlertType.WARNING);
            return;
        }

        try {
            // Load the Reservation Menu FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/reservation.fxml"));
            Parent reservationRoot = loader.load();

            // Get the ReservationController and pass the Admin object
            ReservationController reservationController = loader.getController();
            reservationController.setAdmin(this.admin);
            reservationController.setSelectedReservationId(selectedReservation.getReservationId());
            reservationController.setReservationTitle("Edit Reservation");

            // Get the current stage (main window)
            Stage mainStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

            // Dim the main window
            mainStage.setOpacity(0.5);

            // Create a new stage for the reservation form
            Stage reservationStage = new Stage();
            reservationStage.setTitle("Edit Reservation");
            reservationStage.initModality(Modality.APPLICATION_MODAL);
            reservationStage.initOwner(mainStage);
            reservationStage.setScene(new Scene(reservationRoot));

            // Restore main window when the reservation form closes
            reservationStage.setOnCloseRequest(event -> {
                mainStage.setOpacity(1.0);
                mainStage.toFront();
            });

            reservationStage.showAndWait();

            // Ensure table refresh after the reservation window is closed
            refreshTable();

            // Bring back the main window
            mainStage.setOpacity(1.0);
            mainStage.toFront();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening the reservation menu: ", e);
        }
    }


    @FXML
    private void handleCheckOutAction(ActionEvent actionEvent) {
        ReservationTableRow selectedReservation = reservationTableView.getSelectionModel().getSelectedItem();

        if (selectedReservation == null) {
            showAlert("No Selection", "Please select a reservation to check out.", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/billing.fxml"));
            Parent root = loader.load();

            // Pass reservationId to BillingController
            BillingController billingController = loader.getController();
            billingController.setReservationId(selectedReservation.getReservationId());

            // Create a new stage and set the scene
            Stage billingStage = new Stage();
            billingStage.setTitle("Billing Details");
            billingStage.initModality(Modality.APPLICATION_MODAL);
            billingStage.setScene(new Scene(root));  // Only set the scene once here

            // Set an event listener to refresh the table after the billing stage is closed
            billingStage.setOnHiding(event -> refreshTable());

            billingStage.show();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load billing.fxml", e);
            showAlert("Error", "Unable to load the billing screen: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDeleteReservationAction(ActionEvent actionEvent) {
        ReservationTableRow selectedReservation = reservationTableView.getSelectionModel().getSelectedItem();

        if (selectedReservation != null) {
            // Check if the reservation is checked in
            if ("CHECKED_IN".equals(selectedReservation.getStatus())) {
                // Show a custom warning message if the reservation is checked in
                showAlert("Warning", "This reservation has already been checked in. Bill the customer before deleting the reservation.", Alert.AlertType.WARNING, null);
            } else {
                // Show confirmation alert to delete if not checked in
                showAlert("Confirm Deletion", "Are you sure you want to delete this reservation?", Alert.AlertType.CONFIRMATION, () -> {
                    try {
                        // Delete the reservation and associated rooms
                        reservationDAO.deleteReservation(selectedReservation.getReservationId());
                        showAlert("Success", "The reservation has been deleted successfully.", Alert.AlertType.INFORMATION, null);
                    } catch (SQLException e) {
                        LOGGER.log(Level.SEVERE, "Error during deletion", e);
                        showAlert("Error", "Failed to delete the reservation: " + e.getMessage(), Alert.AlertType.ERROR, null);
                    }
                });
            }
        }
        refreshTable();
    }

    @FXML
    private void handleCancelBookingAction(ActionEvent actionEvent) {
        ReservationTableRow selectedReservation = reservationTableView.getSelectionModel().getSelectedItem();

        if (selectedReservation != null) {
            showAlert("Confirm Cancellation", "Are you sure you want to cancel this reservation?", Alert.AlertType.CONFIRMATION, () -> {
                try {
                    reservationDAO.cancelReservation(selectedReservation.getReservationId());
                    LOGGER.info("Admin: " + admin.getUsername() + " canceled reservation ID " + selectedReservation.getReservationId());
                    showAlert("Success", "The reservation has been canceled successfully.", Alert.AlertType.INFORMATION, null);
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error during cancellation", e);
                    showAlert("Error", "Failed to cancel the reservation: " + e.getMessage(), Alert.AlertType.ERROR, null);
                }
            });
        }
        refreshTable();
    }

    @FXML
    private void handleExitAction(ActionEvent actionEvent) {
        showAlert("Exit Confirmation", "Are you sure you want to exit? Any unsaved data will be lost.", Alert.AlertType.CONFIRMATION, () -> {
            Stage currentStage = (Stage) exitButton.getScene().getWindow();
            currentStage.close();
        });
    }

    private void showAlert(String title, String message, Alert.AlertType type, Runnable onConfirmAction) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(message);

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK && onConfirmAction != null) {
                onConfirmAction.run();
            }
        });
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.showAndWait();
    }

}
