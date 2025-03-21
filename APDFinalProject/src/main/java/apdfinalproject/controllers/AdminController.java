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

    // Initialize method for setting up event listeners
    @FXML
    public void initialize() {
        // Handling the login button action
        loginButton.setOnAction(event -> handleLogin());
    }

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
                LOGGER.log(Level.INFO, "Admin '{0}' logged in successfully.", username);

                // Load reservation-menu.fxml
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/reservation-menu.fxml"));
                Parent root = loader.load();

                // Pass the admin object to the ReservationMenuController
                ReservationMenuController reservationMenuController = loader.getController();
                reservationMenuController.setAdmin(loggedInAdmin);

                // Set the new scene
                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Admin Dashboard - Reservations");
                stage.show();
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

    private void showAlert(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
