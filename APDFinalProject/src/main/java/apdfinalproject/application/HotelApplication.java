package apdfinalproject.application;

import apdfinalproject.database.DatabaseAccess;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.logging.*;

import java.sql.Connection;
import java.util.Objects;

public class HotelApplication extends Application {
    private static final Logger LOGGER = Logger.getLogger(HotelApplication.class.getName());

    // Setup the logger
    private static void setupLogger() {
        try {
            // Use relative path for logs (relative to src/main/logs)
            File logsDir = new File("src/main/logs");
            if (!logsDir.exists()) logsDir.mkdirs();
            FileHandler fileHandler = new FileHandler("src/main/logs/system_logs.%g.log", 1024 * 1024, 10, true);
            fileHandler.setFormatter(new SimpleFormatter());
            LOGGER.setLevel(Level.ALL);
            LOGGER.addHandler(fileHandler);

            LOGGER.info("Logger initialized with file rotation.");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize file logger", e);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        // Initialize the logger before doing any logging
        setupLogger();

        try {
            // Log the start of the application
            LOGGER.info("Starting Hotel Reservation System...");

            // Initialize DB connection
            Connection conn = DatabaseAccess.getConnection();
            LOGGER.info("Database connection established.");

            // Load your FXML
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/kiosk.fxml")));
            Scene scene = new Scene(root);
            primaryStage.setTitle("Hotel Reservation System");
            primaryStage.setScene(scene);
            primaryStage.show();

            LOGGER.info("Application UI loaded and displayed.");

        } catch (Exception e) {
            // Log the exception
            LOGGER.log(Level.SEVERE, "Error initializing the application", e);
        }
    }

    @Override
    public void stop() {
        // Log when the application is stopping
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
