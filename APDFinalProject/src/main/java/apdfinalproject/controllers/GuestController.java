package apdfinalproject.controllers;

import apdfinalproject.dao.GuestDAO;
import apdfinalproject.database.DatabaseAccess;
import apdfinalproject.models.Admin;
import apdfinalproject.models.Guest;
import apdfinalproject.models.Kiosk;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GuestController {


    private Kiosk kiosk;
    private Admin admin;
    private Guest guest;
    private GuestDAO guestDAO;
    private static final Logger LOGGER = DatabaseAccess.LOGGER;

    @FXML
    private Label guestTitle;
    @FXML
    private TextField guestNameField;
    @FXML
    private TextField guestEmailField;
    @FXML
    private TextField guestAddressField;
    @FXML
    private TextField guestIdField;
    @FXML
    private TextField guestPhoneNumberField;
    @FXML
    private Label phoneNumberSearchLabel;
    @FXML
    private Button clearButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Button saveButton;

    public GuestController() throws SQLException {
        this.guestDAO = new GuestDAO();
    }

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            // Check if admin or kiosk is null
            if (this.admin != null) {
                LOGGER.info("Admin: " + this.admin.getUsername() + " initialized Guest menu.");
            } else if (this.kiosk != null) {
                LOGGER.info("Kiosk: " + this.kiosk.getKioskID() + " adding a Guest from " + this.kiosk.getLocation());
            } else {
                LOGGER.warning("Both Admin and Kiosk are null.");
            }

            if (this.guest != null) {
                LOGGER.info("Editing existing Guest ID: " + this.guest.getGuestId());
                loadGuest(); // Only load the existing guest data
            } else {
                int nextGuestId = guestDAO.getNextGuestId();
                LOGGER.info("Creating new Guest with ID: " + nextGuestId);
                guestIdField.setText(String.valueOf(nextGuestId)); // Only set this if creating a new guest
            }
        });
    }


    public Guest getGuest() {
        return this.guest;
    }

    public void setGuest(Guest guest) {
        this.guest = guest;
    }

    public void setAdmin(Admin admin){
        this.admin = admin;
    }

    public void setKiosk(Kiosk kiosk){
        this.kiosk = kiosk;
    }

    public void setGuestTitle(String title) {
        guestTitle.setText(title);
    }

    public void loadGuest() {
        guestIdField.setText(String.valueOf(this.guest.guestIdProperty().get()));
        guestNameField.setText(this.guest.nameProperty().get());
        guestEmailField.setText(this.guest.emailProperty().get());
        guestAddressField.setText(this.guest.addressProperty().get());
        guestPhoneNumberField.setText(this.guest.phoneNumberProperty().get());
    }

    public boolean validateGuestFields() throws SQLException {
        // Clear any previous styling
        resetFieldStyles();

        StringBuilder errorMessages = new StringBuilder();
        boolean isValid = true;

        // Check if guest name is empty
        if (guestNameField.getText().isEmpty()) {
            guestNameField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            errorMessages.append("Guest name cannot be empty.\n");
            isValid = false;
        }

        // Check if guest email is empty
        if (guestEmailField.getText().isEmpty()) {
            guestEmailField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            errorMessages.append("Guest email cannot be empty.\n");
            isValid = false;
        }

        // Check if guest address is empty
        if (guestAddressField.getText().isEmpty()) {
            guestAddressField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            errorMessages.append("Guest address cannot be empty.\n");
            isValid = false;
        }

        // Check if guest phone number is empty or not unique
        if (guestPhoneNumberField.getText().isEmpty()) {
            guestPhoneNumberField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            errorMessages.append("Guest phone number cannot be empty.\n");
            isValid = false;
        } else {
            Guest existingGuest = guestDAO.getGuestByPhoneNumber(guestPhoneNumberField.getText());
            if (existingGuest != null) {
                errorMessages.append("Guest phone number is not unique. Check existing data or enter another phone number.\n");
            }
        }

        // Show alert if there are any validation errors
        if (!isValid) {
            showAlert("Validation Errors", errorMessages.toString(), Alert.AlertType.WARNING);
        }

        return isValid;
    }

    public void handleGuestSearch(ActionEvent actionEvent) {
        String phoneNumber = guestPhoneNumberField.getText().trim();

        if (phoneNumber.isEmpty()) {
            showAlert("Error", "Please enter a phone number to search.", Alert.AlertType.ERROR);
            return;
        }

        try {
            Guest existingGuest = guestDAO.getGuestByPhoneNumber(phoneNumber);

            if (existingGuest != null) {
                System.out.println(existingGuest.getGuestId());
                    this.setGuest(existingGuest);
                System.out.println(this.guest.getGuestId());
                this.loadGuest();
                this.disableEdit();
            } else {
                showAlert("Guest Not Found", "No guest found with this phone number.", Alert.AlertType.INFORMATION);
            }
        } catch (SQLException e) {
            showAlert("Database Error", "An error occurred while searching for the guest.", Alert.AlertType.ERROR);
            LOGGER.log(Level.SEVERE, "Database error while searching for guest: ", e);
        }
    }

    public void handleClearAction(ActionEvent actionEvent) {
        resetFieldStyles();
        resetIdField();
        guestNameField.setText("");
        guestEmailField.setText("");
        guestAddressField.setText("");
        guestPhoneNumberField.setText("");
    }

    public void handleSaveAction(ActionEvent actionEvent) throws SQLException {
        boolean valid = validateGuestFields();
        if (valid) {

            int guestId = Integer.parseInt(guestIdField.getText());
            String name = guestNameField.getText().trim();
            String phoneNumber = guestPhoneNumberField.getText().trim();
            String email = guestEmailField.getText().trim();
            String address = guestAddressField.getText().trim();

            // Update the Guest object
            this.guest = new Guest(guestId, name, phoneNumber, email, address);
            LOGGER.info("Guest in Guest Controller ID: " + this.guest.getGuestId());

            // Log that the guest info has been changed by either the admin or the kiosk
            // If the admin is editing an existing guest, ask for confirmation
            if (admin != null) {  // Ensure it's not a new guest (guestId > 0)
                Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmationAlert.setTitle("Confirm Guest Update");
                confirmationAlert.setHeaderText("Updating Existing Guest");
                confirmationAlert.setContentText("You are updating Guest ID: " + guestId +
                        "\nAre you sure you want to proceed?");

                Optional<ButtonType> result = confirmationAlert.showAndWait();
                if (result.isEmpty() || result.get() != ButtonType.OK) {
                    LOGGER.info("Admin canceled guest update for Guest ID: " + guestId);
                    return; // Stop execution if admin cancels
                }
            } else if (kiosk != null) {
                LOGGER.info("Kiosk: " + kiosk.getKioskID() + " located at " + kiosk.getLocation() + " updated info for Guest ID: " + guestId);
            } else {
                LOGGER.warning("Unknown source: Guest info updated, but no admin or kiosk detected for Guest ID: " + guestId);
            }

            // Close the window and return to the reservation
            close();
        }
    }

    public void close() {
        // Close the current guest window
        Stage currentStage = (Stage) cancelButton.getScene().getWindow();
        currentStage.close();

        // Explicitly restore the main window (opacity and bring to front)
        Stage mainStage = (Stage) currentStage.getOwner();
        if (mainStage != null) {
            mainStage.setOpacity(1.0);  // Restore the opacity to normal
            mainStage.toFront();  // Bring the main window to the front
        }
    }

    public void handleCancelAction(ActionEvent actionEvent) {
        // Show an exit confirmation alert
        showAlert("Exit Confirmation", "Are you sure you want to exit? Any unsaved data will be lost.", Alert.AlertType.CONFIRMATION, this::close);
    }


    public void disableEdit() {
        guestNameField.setDisable(true);
        guestEmailField.setDisable(true);
        guestAddressField.setDisable(true);
        guestPhoneNumberField.setDisable(true);
    }

    public void setViewOnly() {
        disableEdit();
        clearButton.setDisable(true);
        saveButton.setDisable(true);
    }

    private void resetIdField() {
        guestIdField.setText(String.valueOf(guestDAO.getNextGuestId()));
    }

    private void resetFieldStyles() {

        // Reset disabling assuming that a previous guest's data was loaded
        guestNameField.setDisable(false);
        guestEmailField.setDisable(false);
        guestAddressField.setDisable(false);
        guestPhoneNumberField.setDisable(false);

        guestNameField.setStyle("");
        guestEmailField.setStyle("");
        guestAddressField.setStyle("");
        guestPhoneNumberField.setStyle("");
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
