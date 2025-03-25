package apdfinalproject.dao;

import apdfinalproject.database.DatabaseAccess;
import apdfinalproject.models.Billing;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BillingDAO {

    private final Connection connection;
    private final Logger LOGGER = DatabaseAccess.LOGGER;

    public BillingDAO() throws SQLException {
        this.connection = DatabaseAccess.getConnection();
    }

    // Create new billing record
    public void createBilling(Billing billing) {
        String query = "INSERT INTO billing (reservation_id, amount, tax, total_amount, discount) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, billing.getReservationID());
            stmt.setDouble(2, billing.getAmount());
            stmt.setDouble(3, billing.getTax());
            stmt.setDouble(4, billing.getTotalAmount());
            stmt.setDouble(5, billing.getDiscount());

            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating billing record", e);
        }
    }

    // Read billing record by ID
    public Billing getBillingById(int billID) {
        String query = "SELECT * FROM billing WHERE bill_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, billID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Billing(
                            rs.getInt("bill_id"),
                            rs.getInt("reservation_id"),
                            rs.getDouble("amount"),
                            rs.getDouble("tax"),
                            rs.getDouble("total_amount"),
                            rs.getDouble("discount")
                    );
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving billing record", e);
        }
        return null;  // No record found
    }

    // Read billing record by Reservation ID
    public Billing getBillingByReservationId(int reservationID) {
        String query = "SELECT * FROM billing WHERE reservation_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, reservationID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Billing(
                            rs.getInt("bill_id"),
                            rs.getInt("reservation_id"),
                            rs.getDouble("amount"),
                            rs.getDouble("tax"),
                            rs.getDouble("total_amount"),
                            rs.getDouble("discount")
                    );
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving billing record", e);
        }
        return null;  // No record found
    }

    // Update billing record
    public void updateBilling(Billing billing) {
        String query = "UPDATE billing SET reservation_id = ?, amount = ?, tax = ?, total_amount = ?, discount = ? " +
                "WHERE bill_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, billing.getReservationID());
            stmt.setDouble(2, billing.getAmount());
            stmt.setDouble(3, billing.getTax());
            stmt.setDouble(4, billing.getTotalAmount());
            stmt.setDouble(5, billing.getDiscount());
            stmt.setInt(6, billing.getBillID());

            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating billing record", e);
        }
    }

    // Delete billing record
    public void deleteBilling(int billID) {
        String query = "DELETE FROM billing WHERE bill_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, billID);
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting billing record", e);
        }
    }

    // Get the next billing ID
    public int getNextBillingId() {
        String query = "SELECT MAX(bill_id) FROM billing";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1) + 1;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving next billing ID", e);
        }
        return 1;  // Default to 1 if table is empty
    }

}
