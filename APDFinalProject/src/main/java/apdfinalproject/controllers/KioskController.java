package apdfinalproject.controllers;

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

public class KioskController {

    private Kiosk kiosk;

    // UI elements

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

    public void initialize() {
        kiosk = new Kiosk(1, "Lobby Area");
        // Display the welcome message
        kioskIdMessage.setText(kiosk.displayWelcomeMessage());
        // Add Stock image for Guests
        Image image = new Image(getClass().getResource("/images/hotel.jpg").toExternalForm());
        kioskImageView.setImage(image);
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
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }

    // "Administrator Login" button
    @FXML
    private void handleAdminLoginAction() {
        try {
            // Load the Admin Login Menu FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin-login.fxml"));
            Parent adminLoginRoot = loader.load();

            // Get the Admin Login Controller
            AdminController adminController = loader.getController();

            // Create a new stage for the guestSearch form
            Stage adminLoginStage = new Stage();
            adminLoginStage.setTitle("Administrator Login");
            adminLoginStage.initModality(Modality.APPLICATION_MODAL);
            adminLoginStage.setScene(new Scene(adminLoginRoot));

            // Show the admin window and wait until it is closed
            adminLoginStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // "Exit" button
    @FXML
    private void handleExitAction() {
        System.exit(0);
    }
}
