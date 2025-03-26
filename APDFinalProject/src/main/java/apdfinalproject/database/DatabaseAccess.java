package apdfinalproject.database;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.sql.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.logging.*;

public class DatabaseAccess {
    public static final Logger LOGGER = Logger.getLogger(DatabaseAccess.class.getName());
    private static final String DB_NAME = "hotel_reservation.db";
    private static final String DB_PATH = "src/data/" + DB_NAME;
    private static final String DB_URL = "jdbc:sqlite:" + DB_PATH;
    private static Connection connection;

    static {
        setupLogger();
    }

    private DatabaseAccess() {
        // Prevent instantiation
    }

    private static void setupLogger() {
        try {
            File logsDir = new File("src/");
            if (!logsDir.exists()) logsDir.mkdirs();
            FileHandler fileHandler = new FileHandler("src/logs/system_logs.%g.log", 1024 * 1024, 10, true);
            fileHandler.setFormatter(new SimpleFormatter());
            LOGGER.setLevel(Level.ALL);
            LOGGER.addHandler(fileHandler);

            LOGGER.info("Logger initialized with file rotation.");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize file logger", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.sqlite.JDBC");
                File dbFile = new File(DB_PATH);
                boolean firstRun = !dbFile.exists();

                LOGGER.info("Connecting to SQLite database...");
                connection = DriverManager.getConnection(DB_URL);

                if (firstRun) {
                    LOGGER.info("Database file not found. First run detected. Initializing schema...");
                    initializeDatabase();
                }

                Statement statement = connection.createStatement();
                statement.execute("PRAGMA foreign_keys = ON");
                statement.close();

                LOGGER.info("Database connection established and foreign key support enabled.");
            } catch (ClassNotFoundException e) {
                LOGGER.log(Level.SEVERE, "SQLite JDBC driver not found.", e);
                throw new SQLException("Driver not found", e);
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Database connection error.", e);
                throw e;
            }
        }
        return connection;
    }

    private static void initializeDatabase() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            LOGGER.info("Creating tables...");

            statement.execute(
                    "CREATE TABLE guests (" +
                            "guest_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "name TEXT NOT NULL, " +
                            "phone_number TEXT, " +
                            "email TEXT UNIQUE NOT NULL, " +
                            "address TEXT, " +
                            "feedback_id INTEGER, " +
                            "FOREIGN KEY (feedback_id) REFERENCES feedback(feedback_id))");

            statement.execute(
                    "CREATE TABLE room_types (" +
                            "room_type_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "type_name TEXT NOT NULL UNIQUE CHECK(type_name IN ('SINGLE', 'DOUBLE', 'DELUXE', 'PENTHOUSE')))");

            statement.execute(
                    "CREATE TABLE rooms (" +
                            "room_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "room_type STRING NOT NULL, " +
                            "number_of_beds INTEGER NOT NULL, " +
                            "price REAL NOT NULL, " +
                            "reservation_id INTEGER, " +
                            "status TEXT NOT NULL CHECK(status IN ('AVAILABLE', 'OCCUPIED')), " +
                            "FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id), " +
                            "FOREIGN KEY (room_type) REFERENCES room_types(room_type))");

            statement.execute(
                    "CREATE TABLE reservations (" +
                            "reservation_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "guest_id INTEGER NULL, " +
                            "check_in_date TEXT NOT NULL, " +
                            "check_out_date TEXT NOT NULL, " +
                            "number_of_guests INTEGER NOT NULL, " +
                            "status TEXT NOT NULL CHECK(status IN ('CONFIRMED', 'CHECKED_IN', 'CHECKED_OUT', 'CANCELLED')), " +
                            "FOREIGN KEY (guest_id) REFERENCES guests(guest_id) ON DELETE SET NULL)");

            statement.execute(
                    "CREATE TABLE reservation_rooms (" +
                            "reservation_id INTEGER, " +
                            "room_id INTEGER, " +
                            "PRIMARY KEY (reservation_id, room_id), " +
                            "FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id), " +
                            "FOREIGN KEY (room_id) REFERENCES rooms(room_id))");

            statement.execute(
                    "CREATE TABLE billing (" +
                            "bill_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "reservation_id INTEGER, " +
                            "amount REAL NOT NULL, " +
                            "tax REAL NOT NULL, " +
                            "discount REAL DEFAULT 0, " +
                            "total_amount REAL NOT NULL, " +
                            "FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id) ON DELETE SET NULL)");

            statement.execute(
                    "CREATE TABLE admins (" +
                            "admin_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "username TEXT UNIQUE NOT NULL, " +
                            "password TEXT NOT NULL)");

            statement.execute(
                    "CREATE TABLE feedback (" +
                            "feedback_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "guest_id INTEGER NOT NULL, " +
                            "reservation_id INTEGER, " +
                            "comments TEXT, " +
                            "rating INTEGER CHECK(rating BETWEEN 1 AND 5), " +
                            "FOREIGN KEY (guest_id) REFERENCES guests(guest_id), " +
                            "FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id) ON DELETE SET NULL)");

            statement.execute(
                    "CREATE TABLE kiosks (" +
                            "kiosk_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "location TEXT NOT NULL)");

            LOGGER.info("Inserting initial data...");

            statement.execute(
                    "INSERT INTO guests (name, phone_number, email, address) VALUES " +
                        "('John Doe', '123-456-7890', 'john.doe@example.com', '123 Main St, Cityville, CV'), " +
                        "('Jane Smith', '987-654-3210', 'jane.smith@example.com', '456 Oak St, Townsville, TS')");

            statement.execute("INSERT INTO room_types (type_name) VALUES " +
                                "('SINGLE'), ('DOUBLE'), ('DELUXE'), ('PENTHOUSE')");

            statement.execute("INSERT INTO rooms (room_type, number_of_beds, price, status) VALUES " +
                    "('SINGLE', 1, 110.00, 'OCCUPIED'), " +
                    "('SINGLE', 1, 120.00, 'OCCUPIED'), " +
                    "('DOUBLE', 2, 160.00, 'OCCUPIED'), " +
                    "('DOUBLE', 2, 175.00, 'AVAILABLE'), " +
                    "('DELUXE', 2, 250.00, 'AVAILABLE'), " +
                    "('DELUXE', 2, 280.00, 'AVAILABLE'), " +
                    "('PENTHOUSE', 2, 600.00, 'AVAILABLE'), " +
                    "('PENTHOUSE', 2, 650.00, 'AVAILABLE')");

            statement.execute("INSERT INTO reservations (guest_id, check_in_date, check_out_date, number_of_guests, status) VALUES " +
                                "(1, '2025-03-21', '2025-03-25', 2, 'CONFIRMED'), " +
                                "(2, '2025-03-22', '2025-03-23', 1, 'CHECKED_IN')");

            statement.execute("INSERT INTO reservation_rooms (reservation_id, room_id) VALUES " +
                                "(1, 1), (1, 2), (2, 3)");

            statement.execute("INSERT INTO feedback (guest_id, reservation_id, comments, rating) VALUES " +
                                "(1, 1, 'Great stay, will come again!', 5), " +
                                "(2, 2, 'Room was clean but a bit noisy.', 3)");
            statement.execute("INSERT INTO kiosks (location) VALUES " +
                                "('Lobby'), ('Pool Area'), ('Main Entrance')");

            statement.execute("INSERT INTO admins (username, password) VALUES ('admin1', 'pass1')");
            statement.execute("INSERT INTO admins (username, password) VALUES ('admin2', 'pass2')");

            LOGGER.info("Dummy data insertion complete.");
            LOGGER.info("Database initialization complete.");
        }
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                LOGGER.info("Database connection closed.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error closing database connection", e);
        }
    }
}
