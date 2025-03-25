package apdfinalproject.controllers;

import apdfinalproject.dao.AdminDAO;
import apdfinalproject.dao.BillingDAO;
import apdfinalproject.dao.ReservationDAO;
import apdfinalproject.database.DatabaseAccess;
import apdfinalproject.models.Admin;
import apdfinalproject.models.Billing;
import apdfinalproject.models.Reservation;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BillingController {

    private int selectedReservationId;
    private Admin admin;
    private static final Logger LOGGER = DatabaseAccess.LOGGER;
    private ReservationDAO reservationDAO;
    private BillingDAO billingDAO;
    private Billing selectedBill;

    @FXML
    private Label billReminderLabel;
    @FXML
    private TextField billingAmountField;
    @FXML
    private TextField billingTotalField;
    @FXML
    private TextField billingIdField;
    @FXML
    private TextField billingDiscountField;
    @FXML
    private TextField reservationIdField;
    @FXML
    private Label billingTaxField;
    @FXML
    private Button clearButton;
    @FXML
    private Button checkoutButton;
    @FXML
    private Button cancelButton;


    public BillingController() throws SQLException {
        this.reservationDAO = new ReservationDAO();
        this.billingDAO = new BillingDAO();
        this.admin = new Admin(1, "admin1", "pass1");
    }

    @FXML
    public void initialize() throws SQLException {

        // TO DO: Add functionality to load previous bill for read-only
        // Check if selected bill exists before binding ?

        // Ensure numeric values in TextFields
        DoubleProperty amount = new SimpleDoubleProperty();
        DoubleProperty discount = new SimpleDoubleProperty();
        DoubleProperty tax = new SimpleDoubleProperty();
        DoubleProperty total = new SimpleDoubleProperty();

        // Bind text fields to properties
        billingAmountField.textProperty().addListener((obs, oldVal, newVal) -> {
            amount.set(parseDouble(newVal));
        });

        billingDiscountField.textProperty().addListener((obs, oldVal, newVal) -> {
            discount.set(parseDouble(newVal));
        });

        // Compute tax: (amount - discount) * 0.13
        tax.bind(amount.subtract(discount).multiply(0.13));

        // Compute total: (amount - discount) + tax
        total.bind(amount.subtract(discount).add(tax));

        // Bind computed values to UI fields with proper rounding
        billingTaxField.textProperty().bind(Bindings.createStringBinding(
                () -> String.format("%.2f", tax.get()), tax));

        billingTotalField.textProperty().bind(Bindings.createStringBinding(
                () -> String.format("%.2f", total.get()), total));

        billingIdField.setText(String.valueOf(billingDAO.getNextBillingId()));

        // Load everything first before setting the reservation ID
        Platform.runLater(() -> {
            if (selectedReservationId != 0) {
                try {
                    loadReservationData(); // Load reservation info
                    loadPreviousBilling(); // Load previous bill if available
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error loading reservation or billing data", e);
                }
            } else {
                LOGGER.warning("No Reservation ID selected!");
            }
        });

    }

    private void loadPreviousBilling() throws SQLException {
        Optional<Billing> existingBill = Optional.ofNullable(billingDAO.getBillingByReservationId(selectedReservationId));

        if (existingBill.isPresent()) {
            Billing bill = existingBill.get();

            // Unbind text properties before setting them manually
            if (reservationIdField.textProperty().isBound()) {
                reservationIdField.textProperty().unbind();
            }
            if (billingIdField.textProperty().isBound()) {
                billingIdField.textProperty().unbind();
            }
            if (billingAmountField.textProperty().isBound()) {
                billingAmountField.textProperty().unbind();
            }
            if (billingDiscountField.textProperty().isBound()) {
                billingDiscountField.textProperty().unbind();
            }
            if (billingTaxField.textProperty().isBound()) {
                billingTaxField.textProperty().unbind();
            }
            if (billingTotalField.textProperty().isBound()) {
                billingTotalField.textProperty().unbind();
            }

            // Populate fields
            billReminderLabel.setText("* Please remind the customer they can leave feedback at the kiosk.");
            billReminderLabel.setVisible(true);
            reservationIdField.setText(String.valueOf(bill.reservationIDProperty().get()));
            billingIdField.setText(String.valueOf(bill.billIDProperty().get()));
            billingAmountField.setText(String.format("%.2f", bill.amountProperty().get()));
            billingDiscountField.setText(String.format("%.2f", bill.discountProperty().get()));
            billingTaxField.setText(String.format("%.2f", bill.taxProperty().get()));
            billingTotalField.setText(String.format("%.2f", bill.totalAmountProperty().get()));

            // Unbind disable properties before setting them manually
            if (clearButton.disableProperty().isBound()) {
                clearButton.disableProperty().unbind();
            }
            if (checkoutButton.disableProperty().isBound()) {
                checkoutButton.disableProperty().unbind();
            }
            if (billingDiscountField.disableProperty().isBound()) {
                billingDiscountField.disableProperty().unbind();
            }

            // Disable the clear and save buttons, and the discount field
            clearButton.setDisable(true);
            checkoutButton.setDisable(true);
            billingDiscountField.setDisable(true);

            LOGGER.info("Loaded existing billing for reservation ID: " + selectedReservationId);
        } else {
            LOGGER.info("No previous billing found for reservation ID: " + selectedReservationId);
        }
    }



    // Set the selected reservation ID for querying from the admin view
    public void setReservationId(int reservationId) {
        LOGGER.info("Setting selected reservation ID: " + reservationId);
        this.selectedReservationId = reservationId;
    }

    // Set Admin for logging purposes
    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    // Retrieve reservation data through DAO if possible
    private void loadReservationData() throws SQLException {
        LOGGER.info("Loading reservation data for reservation ID: " + selectedReservationId);
        Reservation reservation = reservationDAO.getReservationById(selectedReservationId);

        if (reservation != null) {
            LOGGER.info("Reservation found for reservation ID: " + selectedReservationId);
            loadReservation(reservation);
        } else {
            LOGGER.warning("No reservation found for reservation ID: " + selectedReservationId);
        }
    }

    // Populate the reservation form with existing reservation data for update
    public void loadReservation (Reservation reservation) throws SQLException {
        double amount = reservationDAO.getTotalPriceForReservation(reservation.getReservationID());
        reservationIdField.setText(String.valueOf(reservation.getReservationID()));
        billingAmountField.setText(String.valueOf(amount));
    }

    @FXML
    private void handleClearAction(ActionEvent actionEvent) {
        resetFieldStyles();
        billingDiscountField.clear();
    }

    @FXML
    private void handleCancelAction(ActionEvent actionEvent) {
        showAlert("Exit Confirmation", "Are you sure you want to exit? Any unsaved data will be lost.", Alert.AlertType.CONFIRMATION, () -> {
            Stage currentStage = (Stage) cancelButton.getScene().getWindow();
            currentStage.close();
        });
    }

    @FXML
    private void handleCheckOutAction(ActionEvent actionEvent) {
        // First, validate the billing fields before proceeding with checkout
        if (!validateBeforeCheckout()) {
            return; // Abort the checkout process if validation fails
        }

        // Proceed with the checkout process if validation passes
        try {
            finalizeCheckout();  // Save the billing information and finalize the checkout

            // Checkout finalizes successfully
            Stage currentStage = (Stage) cancelButton.getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            // Log and show any errors during the checkout process
            LOGGER.log(Level.SEVERE, "Error during checkout", e);
            showAlert("Error", "An error occurred during checkout: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void finalizeCheckout() {
        // Retrieve the values from the billing fields after validation
        int billId = Integer.parseInt(billingIdField.getText());
        double amount = parseField(billingAmountField);
        double total = parseField(billingTotalField);
        double discount = billingDiscountField.getText().isEmpty() ? 0.0 : parseField(billingDiscountField);
        double tax = Double.parseDouble(billingTaxField.getText());

        // Create the Billing object
        Billing billing = new Billing(billId, selectedReservationId, amount, tax, total, discount);

        try {
            // Save the billing information to the database
            billingDAO.createBilling(billing);

            // Log the successful creation of the billing entry
            LOGGER.log(Level.INFO, "Admin: " + admin.getUsername() + " successfully created Billing with ID: " + billId);

            // Update the reservation status to 'checked out' in the database
            reservationDAO.checkOutReservation(selectedReservationId);
            LOGGER.log(Level.INFO, "Reservation ID: " + selectedReservationId + " has been checked out successfully.");

            // Show a success alert to the admin
            showBillInAlert(billing);

        } catch (SQLException e) {
            // Log the error and show failure alert
            LOGGER.log(Level.SEVERE, "Error saving billing information", e);
            showAlert("Error", "There was an error while processing the checkout. Please try again.", Alert.AlertType.ERROR);
        }
    }

    private boolean validateBeforeCheckout() {
        StringBuilder errorMessage = new StringBuilder();

        // Reset border styles before validation
        resetFieldStyles();

        try {
            double amount = parseField(billingAmountField);
            double total = parseField(billingTotalField);
            double discount = 0.0; // Default discount value is 0

            // If the discount field is not blank, parse its value
            if (!billingDiscountField.getText().isEmpty()) {
                discount = parseField(billingDiscountField);
            }

            // Validate amount > 0
            if (amount <= 0) {
                errorMessage.append("- The billing amount must be greater than 0.\n");
                highlightField(billingAmountField);
            }
            // Validate total > 0
            if (total <= 0) {
                errorMessage.append("- The total amount must be greater than 0.\n");
                highlightField(billingTotalField);
            }
            // Validate discount ≤ amount
            if (discount > amount) {
                errorMessage.append("- The discount cannot be greater than the billing amount.\n");
                highlightField(billingDiscountField);
            }
            // Validate discount format (max 2 decimal places)
            if (!billingDiscountField.getText().matches("\\d+(\\.\\d{1,2})?") && !billingDiscountField.getText().isEmpty()) {
                errorMessage.append("- The discount must be a number with up to 2 decimal places.\n");
                highlightField(billingDiscountField);
            }

            // Show error message if there are validation issues
            if (!errorMessage.isEmpty()) {
                showAlert("Invalid Input", errorMessage.toString(), Alert.AlertType.ERROR, null);
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter valid numbers in the billing fields.", Alert.AlertType.ERROR, null);
            return false;
        }
    }


    private double parseField(TextField field) throws NumberFormatException {
        return Double.parseDouble(field.getText().trim());
    }

    // Highlight invalid fields with a red border
    private void highlightField(TextField field) {
        field.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
    }

    // Reset all field styles
    private void resetFieldStyles() {
        billingAmountField.setStyle("");
        billingTotalField.setStyle("");
        billingDiscountField.setStyle("");
    }

    private void showBillInAlert(Billing billing) {
        // Get the formatted bill message
        String billMessage = printBill(billing);

        // Create a custom alert dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        // Set the header text (message displayed inside the dialog box)
        alert.setHeaderText("Checkout Successful!"); // Set your custom header here

        // Set the title of the alert window (the window's scene header)
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setTitle("Billing Completed"); // Custom window title

        // Create a custom DialogPane
        DialogPane dialogPane = alert.getDialogPane();

        // Create a label to display the bill message
        Label billLabel = new Label(billMessage);
        billLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: black;");

        // Create a label for the feedback reminder
        Label feedbackReminder = new Label("Please remember to ask the customer for feedback.");
        feedbackReminder.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: red; -fx-alignment: center;");

        // Create a VBox for layout
        VBox content = new VBox(10);
        content.getChildren().addAll(billLabel, feedbackReminder);
        content.setAlignment(Pos.CENTER);

        // Set the VBox as the content of the dialog
        dialogPane.setContent(content);

        // Show the alert
        alert.showAndWait();
    }

    private String printBill(Billing billing) {
        StringBuilder billDetails = new StringBuilder();

        // Add the Bill Header
        billDetails.append(formatHeader(billing));

        // Add the billing details (amount, discount, etc.)
        billDetails.append(formatBillingDetails(billing));

        // Add footer with a reminder to ask for feedback
        billDetails.append("\n--------------------------\n");
        billDetails.append("Thank you for your business!\n");

        return billDetails.toString();
    }

    private String formatHeader(Billing billing) {
        return String.format("----- BILLING DETAILS -----\n" +
                        "Billing ID: %d\n" +
                        "Reservation ID: %d\n",
                billing.getBillID(), billing.getReservationID());
    }

    private String formatBillingDetails(Billing billing) {
        return String.format("Amount: $%.2f\n" +
                        "Discount: $%.2f\n" +
                        "Tax (13%%): $%.2f\n" +
                        "Total: $%.2f\n",
                billing.getAmount(),       // Use getter from Billing object
                billing.getDiscount(),     // Use getter from Billing object
                billing.getTax(),          // Use getter from Billing object
                billing.getTotalAmount());       // Use getter from Billing object
    }


    private void showAlert(String title, String message, Alert.AlertType type) {
        // Create and configure the alert
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(message);

        // Show the alert and wait for the user's response
        alert.showAndWait();
    }

    private void showAlert(String title, String message, Alert.AlertType type, Runnable onConfirmAction) {
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

    // Parse doubles from text fields
    private double parseDouble(String value) {
        try {
            return value.isEmpty() ? 0.0 : Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }


}
