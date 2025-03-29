package apdfinalproject.controllers;

import apdfinalproject.dao.GuestDAO;
import apdfinalproject.dao.ReservationDAO;
import apdfinalproject.dao.ReservationRoomsDAO;
import apdfinalproject.dao.RoomDAO;
import apdfinalproject.database.DatabaseAccess;
import apdfinalproject.models.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.collections.ListChangeListener;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class ReservationController {


    private int selectedReservationId;
    private Kiosk kiosk;
    private Admin admin;
    private SimpleObjectProperty<Guest> guest = new SimpleObjectProperty<>();
    private StringProperty reservationStatusProperty = new SimpleStringProperty();

    private final GuestDAO guestDAO;
    private final ReservationDAO reservationDAO;
    private final ReservationRoomsDAO reservationRoomsDAO;
    private final RoomDAO roomDAO;
    private static final Logger LOGGER = DatabaseAccess.LOGGER;

    @FXML
    private Label reservationTitle;
    @FXML
    private Label numRoomsField;
    @FXML
    private TextField numberGuestsField;
    @FXML
    private TextField reservationIdField;
    @FXML
    private DatePicker checkInDateField;
    @FXML
    private DatePicker checkOutDateField;
    @FXML
    private Button addGuestDetailsButton;
    @FXML
    private Label reservationSubtotalField;
    @FXML
    private Label reservationTaxField;
    @FXML
    private Label reservationTotalField;
    @FXML
    private Label roomCapacityField;
    @FXML
    private Button clearButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    @FXML
    private Label validationErrorLabel;
    @FXML
    private Label selectedRoomErrorLabel;

    @FXML
    private Button addAvailableRoomButton;
    @FXML
    private Button roomRulesButton;
    @FXML
    private Label recommendedRoomsLabel;
    @FXML
    private TableView<Room> availableRoomsTableView;
    @FXML
    private TableColumn<Room, Integer> availableRoomIdColumn;
    @FXML
    private TableColumn<Room, RoomType> availableRoomTypeColumn;
    @FXML
    private TableColumn<Room, Integer> availableRoomsBedsColumn;
    @FXML
    private TableColumn<Room, Double> availableRoomsPriceColumn;

    @FXML
    private Button removeSelectedRoomButton;
    @FXML
    private Button clearSelectedRoomsButton;
    @FXML
    private TableView<Room> selectedRoomsTableView;
    @FXML
    private TableColumn<Room, Integer> selectedRoomIdColumn;
    @FXML
    private TableColumn<Room, RoomType> selectedRoomTypeColumn;
    @FXML
    private TableColumn<Room, Integer> selectedRoomBedsColumn;
    @FXML
    private TableColumn<Room, Double> selectedRoomPriceField;


    public ReservationController() throws SQLException {
        this.reservationDAO = new ReservationDAO();
        this.guestDAO = new GuestDAO();
        this.reservationRoomsDAO = new ReservationRoomsDAO();
        this.roomDAO = new RoomDAO();
    }

    public void initialize() {

        // Default the save button to be disabled
        saveButton.setDisable(true);

        // Default set the Booking ID
        reservationIdField.setText(String.valueOf(reservationDAO.getNextReservationId()));

        // Initialize the columns for Available Rooms
        availableRoomIdColumn.setCellValueFactory(cellData -> cellData.getValue().roomIDProperty().asObject());
        availableRoomTypeColumn.setCellValueFactory(cellData -> cellData.getValue().roomTypeProperty());
        availableRoomsBedsColumn.setCellValueFactory(cellData -> cellData.getValue().numberOfBedsProperty().asObject());
        availableRoomsPriceColumn.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asObject());

        // Initialize the columns for Selected Rooms
        selectedRoomIdColumn.setCellValueFactory(cellData -> cellData.getValue().roomIDProperty().asObject());
        selectedRoomTypeColumn.setCellValueFactory(cellData -> cellData.getValue().roomTypeProperty());
        selectedRoomBedsColumn.setCellValueFactory(cellData -> cellData.getValue().numberOfBedsProperty().asObject());
        selectedRoomPriceField.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asObject());

        // Disable all room selection/CRUD related buttons when nothing is selected or reservation is checked out
        addAvailableRoomButton.disableProperty().bind(
                Bindings.isEmpty(availableRoomsTableView.getSelectionModel().getSelectedItems())
                        .or(reservationStatusProperty.isEqualTo("CHECKED_OUT"))
        );

        removeSelectedRoomButton.disableProperty().bind(
                Bindings.isEmpty(selectedRoomsTableView.getSelectionModel().getSelectedItems())
                        .or(reservationStatusProperty.isEqualTo("CHECKED_OUT"))
        );

        clearSelectedRoomsButton.disableProperty().bind(
                Bindings.isEmpty(selectedRoomsTableView.getItems()).not()
                        .or(reservationStatusProperty.isEqualTo("CHECKED_OUT"))
        );

        // Add listener to # Rooms field and Subtotal field based on what is in the Selected Room Table
        selectedRoomsTableView.getItems().addListener((ListChangeListener<Room>) change -> {
            updateReservationSummary();
            validateReservationFields();
        });

// Add a listener to adjust button text depending on guest information and reservation status
        guest.addListener(new ChangeListener<Guest>() {
            @Override
            public void changed(ObservableValue<? extends Guest> observable, Guest oldValue, Guest newValue) {
                // Log the change in the guest object
                LOGGER.info("Guest changed. Old value: " + (oldValue != null ? oldValue.getName() : "null")
                        + ", New value: " + (newValue != null ? newValue.getName() : "null"));

                // Check if the guest object is null or not
                if (newValue != null) {
                    // Guest is not null
                    String reservationStatus = reservationStatusProperty.get();  // Get the status from the reservation's StringProperty
                    LOGGER.info("Current reservation status: " + reservationStatus);

                    // Update button text based on reservation status
                    if ("CHECKED_OUT".equals(reservationStatus)) {
                        addGuestDetailsButton.setText("View Guest Details");

                        // Disable clear and save buttons when reservation is checked out
                        clearButton.setDisable(true);
                        saveButton.setDisable(true);

                        // Disable entry fields for view only
                        numberGuestsField.setDisable(true);
                        checkInDateField.setDisable(true);
                        checkOutDateField.setDisable(true);

                    } else if ("CANCELLED".equals(reservationStatus)){
                        // Clear selected rooms to prevent duplication
                        selectedRoomsTableView.getItems().clear();
                    } else {
                        addGuestDetailsButton.setText("Edit Guest Details");

                        // Enable clear and save buttons when reservation is not checked out
                        clearButton.setDisable(false);
                        saveButton.setDisable(false);

                        // Enable entry fields
                        numberGuestsField.setDisable(false);
                        checkInDateField.setDisable(false);
                        checkOutDateField.setDisable(false);

                    }
                } else {
                    // If no guest is set, set default text
                    addGuestDetailsButton.setText("Add Guest Details");
                }
            }
        });

        numberGuestsField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                int numGuests = Integer.parseInt(newValue);  // Get the number of guests from the input field
                updateRecommendedRooms(numGuests);  // Update the label with recommended rooms
                validateReservationFields();  // Validate the reservation fields
            } catch (NumberFormatException e) {
                // Handle invalid input (e.g., non-numeric input)
                recommendedRoomsLabel.setText("Please enter a valid number.");
                addGuestDetailsButton.setDisable(true);  // Disable the button if the input is invalid
            }
        });

        // Add listeners to the check-in and check-out date fields to trigger validation
        checkInDateField.valueProperty().addListener((observable, oldValue, newValue) -> validateReservationFields());
        checkOutDateField.valueProperty().addListener((observable, oldValue, newValue) -> validateReservationFields());

        // Log that available rooms are being loaded
        try {
            refreshAvailableRoomsTable();
            LOGGER.info("Available rooms loaded successfully.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading available rooms", e);
        }

        Platform.runLater(() -> {
            // Check if admin or kiosk is null
            if (this.admin != null) {
                LOGGER.info("Admin: " + this.admin.getUsername() + " initialized Reservation menu.");
            } else if (this.kiosk != null) {
                LOGGER.info("Kiosk: " + this.kiosk.getKioskID() + " adding a Reservation from " + this.kiosk.getLocation());
            } else {
                LOGGER.warning("Both Admin and Kiosk are null.");
            }

            if (selectedReservationId != 0) {
                try {
                    loadReservationData();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public Guest getGuest() {
        return guest.get();
    }

    public void setReservationTitle(String title) {
        reservationTitle.setText(title);
    }

    public void setSelectedReservationId(int reservationId) {
        this.selectedReservationId = reservationId;
    }

    // set Kiosk for logging if the guest is doing self-service
    public void setKiosk(Kiosk kiosk) {
        this.kiosk = kiosk;

    }

    // set Admin for logging purposes
    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    private void loadReservationData() throws SQLException {
        LOGGER.info("Loading reservation data for reservation ID: " + selectedReservationId);
        Reservation reservation = reservationDAO.getReservationById(selectedReservationId);

        if (reservation != null) {
            LOGGER.info("Reservation found for reservation ID: " + selectedReservationId);

            // Set the reservation status property
            reservationStatusProperty.set(reservation.statusProperty().get());

            loadReservation(reservation);
        } else {
            LOGGER.warning("No reservation found for reservation ID: " + selectedReservationId);
        }
    }

    private void loadReservation(Reservation reservation) throws SQLException {
        LOGGER.info("Loading reservation ID: " + reservation.getReservationID());

        reservationIdField.setText(String.valueOf(reservation.reservationIDProperty().get()));
        checkInDateField.setValue(reservation.checkInDateProperty().get());
        checkOutDateField.setValue(reservation.checkOutDateProperty().get());
        numberGuestsField.setText(String.valueOf(reservation.numberOfGuestsProperty().get()));
        try {
            LOGGER.info("Fetching selected rooms for reservation ID: " + reservation.getReservationID());
            ObservableList<Room> rooms = reservationRoomsDAO.getAllRoomsForReservation(reservation.getReservationID());
            selectedRoomsTableView.setItems(rooms);
            LOGGER.info("Rooms loaded successfully.");
            selectedRoomsTableView.refresh();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading rooms for reservation ID: " + reservation.getReservationID(), e);
        }

        try {
            LOGGER.info("Fetching guest details for reservation ID: " + reservation.getReservationID());
            Guest guest = guestDAO.getGuestByReservationId(reservation.getReservationID());
            if (guest != null) {
                LOGGER.info("Guest found: " + guest.getName());
                this.guest.set(guest);
            } else {
                LOGGER.warning("No guest found for reservation ID: " + reservation.getReservationID());
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading guest for reservation ID: " + reservation.getReservationID(), e);
        }

        // Update validation after information is set
        validateReservationFields();
        updateReservationSummary();

    }

    private void updateReservationSummary() {
        // Get all rooms from the selectedRoomsTableView
        ObservableList<Room> selectedRooms = selectedRoomsTableView.getItems();

        // Calculate the total number of rooms
        int numRooms = selectedRooms.size();
        numRoomsField.setText(String.valueOf(numRooms));
        int capacity = 0;

        // Calculate the total price of the rooms
        double totalPrice = 0;
        for (Room room : selectedRooms) {
            capacity += room.getRoomType() == RoomType.DELUXE || room.getRoomType() == RoomType.PENTHOUSE ? 2 : room.getNumberOfBeds() * 2;
            totalPrice += room.getPrice();  // Assuming room has a method getPrice() to get the room price
        }

        // Set the calculated capacity
        roomCapacityField.setText(String.valueOf(capacity));

        // Set the total price to the reservationSubtotalField
        reservationSubtotalField.setText(String.format("%.2f", totalPrice));

        // Set the tax
        double tax = totalPrice * 0.13;
        reservationTaxField.setText(String.format("%.2f", tax));

        // Set the total
        reservationTotalField.setText(String.format("%.2f", totalPrice + tax));

    }

    private void updateRecommendedRooms(int numGuests) {
        String recommendedRooms = "";

        if (numGuests == 0) {
            recommendedRooms = "Please enter a valid number of guests.";
        } else if (numGuests <= 2) {
            recommendedRooms = "1 Single room";
        } else if (numGuests <= 4) {
            recommendedRooms = "1 Double room or 2 Single rooms";
        } else if (numGuests <= 6) {
            recommendedRooms = "2 Double rooms or a combination of Double and Single rooms";
        } else {
            recommendedRooms = "Multiple Double rooms or a combination of Double and Single rooms";
        }

        recommendedRoomsLabel.setText("Recommended rooms: " + recommendedRooms);
    }

    @FXML
    private boolean validateReservationFields() {
        boolean isValid = true;
        StringBuilder errorMessage = new StringBuilder();

        // Reset all field styles before validation
        resetFieldStyles();

        // 1. Ensure no fields are empty
        if (numberGuestsField.getText().isEmpty()) {
            isValid = false;
            errorMessage.append("Number of guests cannot be empty.\n");
            highlightField(numberGuestsField);  // Highlight invalid field
        }

        if (checkInDateField.getValue() == null) {
            isValid = false;
            errorMessage.append("Check-in date cannot be empty.\n");
            highlightDatePickerField(checkInDateField);  // Highlight invalid field
        }

        if (checkOutDateField.getValue() == null) {
            isValid = false;
            errorMessage.append("Check-out date cannot be empty.\n");
            highlightDatePickerField(checkOutDateField);  // Highlight invalid field
        }

        // 2. Validate number of guests (must be a number and <= total number of beds)
        int numGuests = 0;
        try {
            numGuests = Integer.parseInt(numberGuestsField.getText());
        } catch (NumberFormatException e) {
            isValid = false;
            errorMessage.append("Number of guests must be a valid number.\n");
            highlightField(numberGuestsField);  // Highlight invalid field
        }

        // Assuming selectedRoomsTableView contains a list of selected rooms
        int totalBeds = 0;
        int capacity = 0;
        for (Room room : selectedRoomsTableView.getItems()) {
            totalBeds += room.getNumberOfBeds();
            if (room.getRoomType() == RoomType.PENTHOUSE || room.getRoomType() == RoomType.DELUXE) {
                capacity += 2;
            } else {
                capacity += room.getNumberOfBeds() * 2;
            }
        }


        if (numGuests > capacity) {
            isValid = false;
            errorMessage.append("Number of guests cannot exceed total bed capacity in selected rooms.\n");
            selectedRoomErrorLabel.setText("Not enough beds. Add more rooms.");
            highlightField(numberGuestsField);  // Highlight invalid field
        }

        // 3. Ensure check-out date is not earlier than check-in date
        if (checkOutDateField.getValue() != null && checkInDateField.getValue() != null) {
            if (checkOutDateField.getValue().isBefore(checkInDateField.getValue())) {
                isValid = false;
                errorMessage.append("Check-out date cannot be earlier than check-in date.\n");
                highlightDatePickerField(checkOutDateField);  // Highlight invalid field
            }
        }
//
//        // Ensure check-in and check-out dates are not earlier than today
//        if (checkInDateField.getValue() != null && checkInDateField.getValue().isBefore(LocalDate.now())) {
//            isValid = false;
//            errorMessage.append("Check-in date cannot be earlier than today.\n");
//            highlightDatePickerField(checkInDateField);
//        }
//
//        if (checkOutDateField.getValue() != null && checkOutDateField.getValue().isBefore(LocalDate.now())) {
//            isValid = false;
//            errorMessage.append("Check-out date cannot be later than today.\n");
//            highlightDatePickerField(checkOutDateField);
//        }

        // Display error message and enable/disable button based on validation
        if (isValid) {
            validationErrorLabel.setText("");
            validationErrorLabel.setVisible(false);
            addGuestDetailsButton.setDisable(false);  // Enable the add guest details button
        } else {
            validationErrorLabel.setVisible(true);
            validationErrorLabel.setText(errorMessage.toString());
            addGuestDetailsButton.setDisable(true);  // Disable the button
        }

        return isValid;
    }

    @FXML
    private void handleClearAction() {
        resetFieldStyles();
        numberGuestsField.setText("");
        checkInDateField.setValue(null);
        checkOutDateField.setValue(null);
    }

    @FXML
    private void handleViewRoomRulesAction() {
        // Create an alert of type INFORMATION
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Room Rules");
        alert.setHeaderText("Room Rules and Booking Information");

        // Create the content of the popup (using a TextArea for better formatting)
        String roomRules =
                "• Single room: Max 2 people.\n" +
                        "• Double room: Max 4 people.\n" +
                        "• Deluxe and Penthouse rooms: Max 2 people, but prices are higher.\n" +
                        "• More than 2 adults and less than 5: Double room or two Single rooms will be offered.\n" +
                        "• More than 4 adults: Multiple Double rooms or a combination of Double and Single rooms will be offered.";

        // Set the content of the alert
        alert.setContentText(roomRules);

        // Add an OK button
        alert.getButtonTypes().setAll(ButtonType.OK);

        // Show the alert
        alert.showAndWait();
    }

    @FXML
    private void handleAddGuestAction(ActionEvent actionEvent) {
        try {
            // Load the guest.fxml and get the controller
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/guest.fxml"));
            Parent root = loader.load();

            // Get the controller of the guest form
            GuestController guestController = loader.getController();
            guestController.setGuest(getGuest());

            if (guestController.getGuest() != null) {
                guestController.setGuestTitle("View/Edit Guest");
                if ("CHECKED_OUT".equals(reservationStatusProperty.get())) {
                    guestController.setViewOnly();
                }
            }

            // Set admin or kiosk depending
            if (this.kiosk != null) {
                guestController.setKiosk(this.kiosk);
            } else if (this.admin != null) {
                guestController.setAdmin(this.admin);
            }

            // Get the current stage (main window)
            Stage mainStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

            // Disable interaction with the main window and dim it
            mainStage.setOpacity(0.5); // Optionally dim the main window

            // Show the guest form in a new stage
            Stage guestStage = new Stage();
            guestStage.setScene(new Scene(root));
            guestStage.setTitle("Guest Menu");

            // Make the guest window modal (block interaction with main window)
            guestStage.initOwner(mainStage); // Set main stage as owner
            guestStage.initModality(Modality.APPLICATION_MODAL); // Block interaction with the main window

            // When the guest form is closed, restore the main window
            guestStage.setOnCloseRequest(event -> {
                // Restore the main window opacity when the guest form is closed
                mainStage.setOpacity(1.0); // Restore main window opacity
                mainStage.toFront(); // Bring the main window to the front
            });

            guestStage.showAndWait(); // Wait until the guest form is closed

            // After the form is closed, get the guest object
            this.guest.set(guestController.getGuest());
            System.out.println(this.guest.get().getGuestId());
            System.out.println(this.guest.get().getName());

            // Bring the main window to the front in case it wasn't restored properly
            mainStage.toFront();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "An error occurred while loading the Guest menu: " + e);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSaveAction(ActionEvent actionEvent) throws SQLException {
        showAlert("Confirmation", "Are you sure you want to submit this reservation? Modification or cancellation can only be done through the front desk.", Alert.AlertType.CONFIRMATION, () -> {
            boolean valid = validateReservationFields();

            if (valid) {
                int reservationId = Integer.parseInt(reservationIdField.getText());
                List<Integer> roomIds = selectedRoomsTableView.getItems().stream()
                        .map(Room::getRoomID)
                        .collect(Collectors.toList());

                LocalDate checkInDate = checkInDateField.getValue();
                LocalDate checkOutDate = checkOutDateField.getValue();
                int numGuests = Integer.parseInt(numberGuestsField.getText());

                // Safe to do since the save button is only open if this.guest is not null
                int guestId = this.guest.get().getGuestId();

                Reservation reservation = new Reservation(
                        reservationId, guestId, checkInDate,checkOutDate,
                        numGuests, "CONFIRMED", roomIds);

                // Check if this reservation exists in the database
                Reservation existingReservation = null;
                Guest existingGuest = null;

                try {
                    existingReservation = reservationDAO.getReservationById(reservationId);

                    if (existingReservation != null) {
                        // Check if the guest exists first if not then we need to create a Guest
                        existingGuest  = guestDAO.getGuestById(this.getGuest().getGuestId());
                        if (existingGuest != null) {
                            // Kiosk can only read existing guest information not edit
                            if (kiosk!= null) {
                                LOGGER.info("Kiosk ID: " + kiosk.getKioskID() + "located at " + kiosk.getLocation() + " used existing guest ID " + this.getGuest().getGuestId());
                            }
                        } else {
                            guestDAO.createGuest(this.getGuest());
                            if (admin != null) {
                                LOGGER.info("Admin " + admin.getUsername() + " successfully created Guest ID: " + this.getGuest().getGuestId());
                            }
                            if (kiosk!= null) {
                                LOGGER.info("Kiosk ID: " + kiosk.getKioskID() + "located at " + kiosk.getLocation() + " successfully created Guest ID: " + this.getGuest().getGuestId());
                            }
                        }

                        // Point the new reservation info to the database
                        existingReservation = reservation;
                        // Update the reservation object in the database
                        reservationDAO.updateReservation(existingReservation);

                        // Log success, must be admin as kiosks cannot edit existing reservations
                        LOGGER.info("Admin " + admin.getUsername() + " successfully updated Reservation ID " + existingReservation.getReservationID() + " for Guest ID: " + this.getGuest().getGuestId());
                    } else {
                        // Check if the guest exists, update or create the guest accordingly
                        existingGuest = guestDAO.getGuestById(this.getGuest().getGuestId());

                        if (existingGuest != null) {
                            // Kiosk can only read existing guest information not edit
                            if (kiosk!= null) {
                                LOGGER.info("Kiosk ID: " + kiosk.getKioskID() + "located at " + kiosk.getLocation() + " used existing guest ID " + this.getGuest().getGuestId());
                            }
                        } else {
                            LOGGER.info("Guest does not exist");
                            guestDAO.createGuest(this.getGuest());
                            if (admin != null) {
                                LOGGER.info("Admin " + admin.getUsername() + " successfully created Guest ID: " + this.getGuest().getGuestId());
                            }
                            if (kiosk!= null) {
                                LOGGER.info("Kiosk ID: " + kiosk.getKioskID() + "located at " + kiosk.getLocation() + " successfully created Guest ID: " + this.getGuest().getGuestId());
                            }
                        }

                        // Create a new reservation if it doesn't exist now that guest info existss
                        reservationDAO.createReservation(reservation);

                        // Log success depending on if it is an admin or kiosk
                        if (admin != null) {
                            LOGGER.info("Admin " + admin.getUsername() + " successfully created Reservation ID " + reservation.getReservationID() + " for Guest ID: " + this.getGuest().getGuestId());
                        }
                        if (kiosk!= null) {
                            LOGGER.info("Kiosk ID: " + kiosk.getKioskID() + "located at " + kiosk.getLocation() + " successfully created Reservation" + reservation.getReservationID() + " for Guest ID: " + this.getGuest().getGuestId());
                        }
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "An error occurred while processing the reservation: " + e);
                }
                showAlert("Reservation Saved Successfully", "Reservation with ID " + reservation.getReservationID() + " was saved successfully.", Alert.AlertType.INFORMATION);
                close();
            }
        });

    }

    @FXML
    private void handleCancelAction(ActionEvent actionEvent) {
        showAlert("Exit Confirmation", "Are you sure you want to exit? Any unsaved data will be lost.", Alert.AlertType.CONFIRMATION, this::close);
    }

    @FXML
    private void handleAddAvailableRoomAction(ActionEvent actionEvent) {
        // Get the selected room from the availableRoomsTableView
        Room selectedRoom = availableRoomsTableView.getSelectionModel().getSelectedItem();

        if (selectedRoom != null) {
            // Add the selected room to the selectedRoomsTableView
            selectedRoomsTableView.getItems().add(selectedRoom);

            // Remove the room from availableRoomsTableView to disallow double room
            availableRoomsTableView.getItems().remove(selectedRoom);

            // Trigger validation check
            validateReservationFields();
            updateReservationSummary();
        } else {
            // Show an alert if no room is selected
            showAlert("No Room Selected", "Please select a room to add.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleRemoveSelectedRoomAction(ActionEvent actionEvent) {
        // Get the selected room from the selectedRoomsTableView
        Room selectedRoom = selectedRoomsTableView.getSelectionModel().getSelectedItem();

        if (selectedRoom != null) {
            // Remove the selected room from selectedRoomsTableView
            selectedRoomsTableView.getItems().remove(selectedRoom);

            // Add the room back to availableRoomsTableView
            availableRoomsTableView.getItems().add(selectedRoom);

            // Trigger validation check
            validateReservationFields();
            updateReservationSummary();
        } else {
            // Show an alert if no room is selected
            showAlert("No Room Selected", "Please select a room to remove.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleClearSelectedRoomAction(ActionEvent actionEvent) throws SQLException {
        // Clear the selected rooms table view
        selectedRoomsTableView.getItems().clear();
        // Refresh the available rooms table view
        refreshAvailableRoomsTable();
        // Trigger validation check
        validateReservationFields();
        updateReservationSummary();
    }

    // Highlight invalid fields with a red border
    private void highlightField(TextField field) {
        field.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
    }

    // Highlight invalid DatePicker field with a red border
    private void highlightDatePickerField(DatePicker datePicker) {
        datePicker.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
    }

    private void resetFieldStyles() {
        // Reset styles for all fields
        numberGuestsField.setStyle("");
        checkInDateField.setStyle("");
        checkOutDateField.setStyle("");

        validationErrorLabel.setStyle("");
        validationErrorLabel.setVisible(false);

        selectedRoomErrorLabel.setStyle("");
        validationErrorLabel.setText("");
        selectedRoomErrorLabel.setText("");

    }

    private void close() {
        // Close the current window
        Stage currentStage = (Stage) cancelButton.getScene().getWindow();
        currentStage.close();

        // Explicitly restore the main window (opacity and bring to front)
        Stage mainStage = (Stage) currentStage.getOwner();
        if (mainStage != null) {
            mainStage.setOpacity(1.0);  // Restore the opacity to normal
            mainStage.toFront();  // Bring the main window to the front
        }
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

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.showAndWait();
    }

    private void refreshAvailableRoomsTable() throws SQLException {
        // Fetch available rooms from the DAO
        ObservableList<Room> rooms = roomDAO.getAllAvailableRooms();

        // Bind the table to the available rooms list
        availableRoomsTableView.setItems(rooms);
    }

}
