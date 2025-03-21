package apdfinalproject.dao;

import apdfinalproject.database.DatabaseAccess;
import apdfinalproject.models.Admin;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdminDAO {
    private static final Logger LOGGER = DatabaseAccess.LOGGER;
    private final Connection connection;

    public AdminDAO() throws SQLException {
        try {
            connection = DatabaseAccess.getConnection();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database connection error in AdminDAO.", e);
            throw e;
        }
    }

    // Method to get an Admin by username
    public Admin getAdminByUsername(String username) {
        String sql = "SELECT * FROM Admins WHERE username = ?";
        Admin admin = null;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            // if the admin is found, create the Admin object
            if (rs.next()) {
                int adminID = rs.getInt("admin_id");
                String password = rs.getString("password");
                admin = new Admin(adminID, username, password);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error while retrieving admin by username: " + username, e);
        }
        return admin;
    }

    // Method to add a new Admin to the database
    public boolean addAdmin(Admin admin) {
        String sql = "INSERT INTO Admins (username, password) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, admin.getUsername());
            pstmt.setString(2, admin.getPassword());
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;  // If rows are affected, it means insert was successful
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error while adding admin: " + admin.getUsername(), e);
        }
        return false;
    }

    // Method to update an existing Admin's details (username, password)
    public boolean updateAdmin(Admin admin) {
        String sql = "UPDATE Admins SET username = ?, password = ? WHERE AdminID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, admin.getUsername());
            pstmt.setString(2, admin.getPassword());
            pstmt.setInt(3, admin.getAdminId());
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error while updating admin ID: " + admin.getAdminId(), e);
        }
        return false;
    }

    // Method to delete an Admin by AdminID
    public boolean deleteAdmin(int adminID) {
        String sql = "DELETE FROM Admins WHERE AdminID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, adminID);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error while deleting admin ID: " + adminID, e);
        }
        return false;
    }

    // Method to get a list of all admins (just for demonstration)
    public List<Admin> getAllAdmins() {
        List<Admin> adminList = new ArrayList<>();
        String sql = "SELECT * FROM Admins";

        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                int adminID = rs.getInt("AdminID");
                String username = rs.getString("username");
                String password = rs.getString("password");
                adminList.add(new Admin(adminID, username, password));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error while retrieving all admins", e);
        }

        return adminList;
    }

    // Closing the database connection when done
    public void closeConnection() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error closing database connection.", e);
        }
    }
}
