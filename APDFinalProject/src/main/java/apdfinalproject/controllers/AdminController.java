package apdfinalproject.controllers;

import apdfinalproject.dao.AdminDAO;
import apdfinalproject.database.DatabaseAccess;
import apdfinalproject.models.Admin;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class AdminController {

    private final AdminDAO adminDAO;
    private static final Logger LOGGER = DatabaseAccess.LOGGER;
    private Stage kioskStage;

    @FXML
    private TextField loginUsername;

    @FXML
    private PasswordField loginPassword;

    @FXML
    private Button loginButton;
    @FXML
    private Button cancelButton;

    public AdminController() throws SQLException {
        adminDAO = new AdminDAO(); // Initialize the DAO
    }

    public void setKioskStage(Stage kioskStage) {
        this.kioskStage = kioskStage;
    }
    // Initialize method for setting up event listeners
    @FXML
    public void initialize() {
    }

    @FXML
    private void handleLogin() {
        String username = loginUsername.getText();
        String password = loginPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Input Error", "Username and password must not be empty!", AlertType.WARNING);
            loginUsername.requestFocus();
            return;
        }

        try {
            Admin loggedInAdmin = validateLoginAndGetAdmin(username, password);
            if (loggedInAdmin != null) {
                LOGGER.log(Level.INFO, "Admin " + username + " logged in successfully.", username);

                // Load reservation-menu.fxml
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/reservation-menu.fxml"));
                Parent root = loader.load();

                // Set admin in controller
                ReservationMenuController reservationMenuController = loader.getController();
                reservationMenuController.setAdmin(loggedInAdmin);

                // Show reservation menu in new stage
                Stage reservationStage = new Stage();
                reservationStage.setScene(new Scene(root));
                reservationStage.setTitle("Admin Dashboard - Reservations");
                reservationStage.show();

                // Close both the login window and the kiosk window
                Stage loginStage = (Stage) loginButton.getScene().getWindow();
                loginStage.close();

                // Close kiosk stage (if stored globally)
                if (KioskController.kioskStage != null) {
                    KioskController.kioskStage.close();
                }

            } else {
                showAlert("Invalid Login", "Invalid username or password!", AlertType.ERROR);
                loginUsername.requestFocus();
            }
        } catch (SQLException | IOException e) {
            showAlert("Error", "Login failed: " + e.getMessage(), AlertType.ERROR);
            LOGGER.log(Level.SEVERE, "Login error", e);
        }
    }

    public Admin validateLoginAndGetAdmin(String username, String password) throws SQLException {
        Admin admin = adminDAO.getAdminByUsername(username);
        if (admin != null && admin.getPassword().equals(password)) {
            return admin;
        }
        return null;
    }

    @FXML
    private void handleCancelLogin() {
        // Create a confirmation alert
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Cancel Login");
        alert.setHeaderText("Are you sure you want to cancel the login?");
        alert.setContentText("Any unsaved data will be lost.");

        // Show the confirmation and wait for the user's response
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Close the current window if the user confirms
                Stage currentStage = (Stage) cancelButton.getScene().getWindow();
                currentStage.close();
            }
        });
    }

    private void showAlert(String title, String message, AlertType type) {
        // Create and configure the alert
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(message);

        // Show the alert and wait for the user's response
        alert.showAndWait();
    }
}
