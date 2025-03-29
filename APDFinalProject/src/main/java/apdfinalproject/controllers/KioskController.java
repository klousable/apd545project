package apdfinalproject.controllers;

import apdfinalproject.database.DatabaseAccess;
import apdfinalproject.models.Kiosk;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

import java.util.logging.Level;
import java.util.logging.Logger;

public class KioskController {

    private Kiosk kiosk;
    private Logger LOGGER = DatabaseAccess.LOGGER;

    @FXML
    private Button addReservationButton;

    @FXML
    private Button addFeedbackButton;

    @FXML
    private Button adminLoginButton;

    @FXML
    private ImageView kioskImageView;

    @FXML
    private Label kioskIdMessage;

    @FXML
    private Button exitButton;

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label infoLabel;

    @FXML
    private ImageView kioskImage;

    public static Stage kioskStage;

    public void initialize() {
        try {
            kiosk = new Kiosk(1, "Lobby Area");
            // Display the welcome message
            kioskIdMessage.setText(kiosk.displayWelcomeMessage());
            // Add Stock image for Guests
            Image image = new Image(getClass().getResource("/images/hotel.jpg").toExternalForm());
            kioskImageView.setImage(image);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing Kiosk: ", e);
        }
    }

    public void setKioskStage(Stage stage) {
        kioskStage = stage;
    }

    // "Self-Service Kiosk" button
    @FXML
    private void handleSelfServiceKioskAction() {
        try {
            // Load the Reservation Menu FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/reservation.fxml"));
            Parent reservationRoot = loader.load();

            // Get the ReservationController and pass the Kiosk object
            ReservationController reservationController = loader.getController();
            reservationController.setKiosk(kiosk);

            // Create a new stage for the reservation form
            Stage reservationStage = new Stage();
            reservationStage.setTitle("Make a Reservation");
            reservationStage.initModality(Modality.APPLICATION_MODAL);
            reservationStage.setScene(new Scene(reservationRoot));

            // Show the reservation window and wait until it is closed
            reservationStage.showAndWait();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening the reservation menu: ", e);
        }
    }

    // "Give Feedback" button
    @FXML
    private void handleGiveFeedbackAction() {
        try {
            // Load the GuestSearch Menu FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/guest-search.fxml"));
            Parent guestSearchRoot = loader.load();

            // Get the GuestSearchController and pass the Kiosk object
            GuestSearchController guestSearchController = loader.getController();
            guestSearchController.setKiosk(kiosk);

            // Create a new stage for the guestSearch form
            Stage guestSearchStage = new Stage();
            guestSearchStage.setTitle("Self-Service Reservation");
            guestSearchStage.initModality(Modality.APPLICATION_MODAL);
            guestSearchStage.setScene(new Scene(guestSearchRoot));

            // Show the guest search window and wait until it is closed
            guestSearchStage.showAndWait();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening the guest search menu: ", e);
        }
    }

    @FXML
    private void handleAdminLoginAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin-login.fxml"));
            Parent adminLoginRoot = loader.load();

            // Get the controller and pass the kiosk stage
            AdminController adminController = loader.getController();
            Stage kioskStage = (Stage) adminLoginButton.getScene().getWindow();

            // Show the login window
            Stage adminLoginStage = new Stage();
            adminLoginStage.setTitle("Administrator Login");
            adminLoginStage.initModality(Modality.WINDOW_MODAL);
            adminLoginStage.initOwner(kioskStage);
            adminLoginStage.setScene(new Scene(adminLoginRoot));
            adminLoginStage.showAndWait();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening the admin login window: ", e);
        }
    }

    @FXML
    private void handleExitAction() {
        showAlert("Exit Confirmation", "Are you sure you want to exit? Any unsaved data will be lost.", AlertType.CONFIRMATION, () -> {
            DatabaseAccess.closeConnection();
            System.exit(0);
        });
    }

    private void showAlert(String title, String message, AlertType type, Runnable onConfirmAction) {
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
