package apdfinalproject.database;

import java.sql.*;
import java.io.File;
import java.io.IOException;
import java.util.logging.*;

public class DatabaseAccess {
    private static final Logger LOGGER = Logger.getLogger(DatabaseAccess.class.getName());
    private static final String DB_NAME = "hotel_reservation.db";
    // Use relative path for the database (relative to src/main/resources)
    private static final String DB_PATH = "src/main/java/apdfinalproject/database/" + DB_NAME;
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
                            "status TEXT NOT NULL CHECK(status IN ('AVAILABLE', 'OCCUPIED')), " +
                            "FOREIGN KEY (room_type) REFERENCES room_types(room_type))");

            statement.execute(
                    "CREATE TABLE reservations (" +
                            "reservation_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "guest_id INTEGER NOT NULL, " +
                            "room_id INTEGER NOT NULL, " +
                            "check_in_date TEXT NOT NULL, " +
                            "check_out_date TEXT NOT NULL, " +
                            "number_of_guests INTEGER NOT NULL, " +
                            "status TEXT NOT NULL CHECK(status IN ('PENDING', 'CONFIRMED', 'CHECKED_IN', 'CHECKED_OUT', 'CANCELLED')), " +
                            "FOREIGN KEY (guest_id) REFERENCES guests(guest_id), " +
                            "FOREIGN KEY (room_id) REFERENCES rooms(room_id))");

            statement.execute(
                    "CREATE TABLE billing (" +
                            "bill_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "reservation_id INTEGER NOT NULL, " +
                            "amount REAL NOT NULL, " +
                            "tax REAL NOT NULL, " +
                            "discount REAL DEFAULT 0, " +
                            "total_amount REAL NOT NULL, " +
                            "FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id))");

            statement.execute(
                    "CREATE TABLE admins (" +
                            "admin_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "username TEXT UNIQUE NOT NULL, " +
                            "password TEXT NOT NULL)");

            statement.execute(
                    "CREATE TABLE feedback (" +
                            "feedback_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "guest_id INTEGER NOT NULL, " +
                            "reservation_id INTEGER NOT NULL, " +
                            "comments TEXT, " +
                            "rating INTEGER CHECK(rating BETWEEN 1 AND 5), " +
                            "FOREIGN KEY (guest_id) REFERENCES guests(guest_id), " +
                            "FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id))");

            statement.execute(
                    "CREATE TABLE kiosks (" +
                            "kiosk_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "location TEXT NOT NULL)");

            LOGGER.info("Inserting initial data...");

            statement.execute("INSERT INTO room_types (type_name) VALUES " +
                    "('SINGLE'), ('DOUBLE'), ('DELUXE'), ('PENTHOUSE')");

            statement.execute("INSERT INTO admins (username, password) VALUES ('admin1', 'pass1')");
            statement.execute("INSERT INTO admins (username, password) VALUES ('admin2', 'pass2')");

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
