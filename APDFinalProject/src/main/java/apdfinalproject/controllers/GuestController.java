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

        // Default set the Guest ID
        guestIdField.setText(String.valueOf(guestDAO.getNextGuestId()));

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
                loadGuest();
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

    public void loadGuest() {
        guestTitle.setText("Edit Guest");
        guestIdField.setText(String.valueOf(this.guest.getGuestId()));
        guestNameField.setText(this.guest.nameProperty().get());
        guestEmailField.setText(this.guest.emailProperty().get());
        guestAddressField.setText(this.guest.addressProperty().get());
        guestPhoneNumberField.setText(this.guest.phoneNumberProperty().get());
    }

    public boolean validateGuestFields() {
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

        // Check if guest phone number is empty
        if (guestPhoneNumberField.getText().isEmpty()) {
            guestPhoneNumberField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            errorMessages.append("Guest phone number cannot be empty.\n");
            isValid = false;
        }

        // Show alert if there are any validation errors
        if (!isValid) {
            showAlert("Validation Errors", errorMessages.toString(), Alert.AlertType.WARNING);
        }

        return isValid;
    }

    public void handleGuestSearch(ActionEvent actionEvent) {
    }

    public void handleClearAction(ActionEvent actionEvent) {
        resetFieldStyles();
        guestNameField.setText("");
        guestEmailField.setText("");
        guestAddressField.setText("");
        guestPhoneNumberField.setText("");
    }

    public void handleSaveAction(ActionEvent actionEvent) {
        // Validate the fields
        boolean valid = validateGuestFields();
        if (valid) {

            // Grab fields after verifying validity
            int guestId = Integer.parseInt(guestIdField.getText());
            String name = guestNameField.getText().trim();
            String phoneNumber = guestPhoneNumberField.getText().trim();
            String email = guestEmailField.getText().trim();
            String address = guestAddressField.getText().trim();

            // Update the Guest object
            this.guest = new Guest(guestId, name, phoneNumber, email, address);

            // Log that the guest info has been changed by either the admin or the kiosk
            if (admin != null) {
                LOGGER.info("Admin: " + admin.getUsername() + " created/updated info for Guest ID: " + guestId);
            } else if (kiosk != null) {
                LOGGER.info("Kiosk: " + kiosk.getKioskID() + " located at " + kiosk.getLocation() + " updated info for Guest ID: " + guestId);
            } else {
                LOGGER.warning("Unknown source: Guest info updated, but no admin or kiosk detected for Guest ID: " + guestId);
            }

            // Close the window and return to the reservation
            Stage currentStage = (Stage) cancelButton.getScene().getWindow();
            currentStage.close();
        }
    }

    public void handleCancelAction(ActionEvent actionEvent) {
        showAlert("Exit Confirmation", "Are you sure you want to exit? Any unsaved data will be lost.", Alert.AlertType.CONFIRMATION, () -> {
            Stage currentStage = (Stage) cancelButton.getScene().getWindow();
            currentStage.close();
        });
    }

    private void resetFieldStyles() {
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
