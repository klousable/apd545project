package apdfinalproject.application;

import apdfinalproject.controllers.KioskController;
import apdfinalproject.database.DatabaseAccess;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.util.logging.*;

import java.sql.Connection;
import java.util.Objects;

public class HotelApplication extends Application {
    private static final Logger LOGGER = DatabaseAccess.LOGGER;

    @Override
    public void start(Stage primaryStage) {
        try {
            // Logging application startup
            LOGGER.info("Starting Hotel Reservation System...");

            // Initialize connection to SQLite
            Connection conn = DatabaseAccess.getConnection();
            LOGGER.info("Database connection established.");

            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/kiosk.fxml")));
            Scene scene = new Scene(root);
            KioskController.kioskStage = primaryStage;

            primaryStage.setTitle("Hotel Reservation System");
            primaryStage.setScene(scene);
            primaryStage.show();

            LOGGER.info("Application UI loaded and displayed.");

        } catch (Exception e) {
            // Log the exception error
            LOGGER.log(Level.SEVERE, "Error initializing the application", e);
        }
    }

    @Override
    public void stop() {
        // Log application shutdown
        LOGGER.info("Stopping Hotel Reservation System...");

        // Close DB on exit
        DatabaseAccess.closeConnection();
    }

    public static void main(String[] args) {
        // Log that the application is launching
        LOGGER.info("Launching Hotel Reservation System...");
        launch(args);
    }
}
