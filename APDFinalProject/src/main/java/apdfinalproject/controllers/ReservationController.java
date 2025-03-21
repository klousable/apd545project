package apdfinalproject.controllers;

import apdfinalproject.database.DatabaseAccess;
import apdfinalproject.models.Admin;
import apdfinalproject.models.Kiosk;
import javafx.fxml.FXML;

import java.util.logging.Logger;


public class ReservationController {
    private Kiosk kiosk;
    private Admin admin;
    private static final Logger LOGGER = DatabaseAccess.LOGGER;

    // set Kiosk for logging if the guest is doing self-service
    public void setKiosk(Kiosk kiosk) {
        this.kiosk = kiosk;

    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    // Method to handle reservation submission (you can add more fields and logic)
    @FXML
    private void handleSubmitReservation() {
        // You can use the Kiosk object for logging or other purposes
        System.out.println("Reservation made from Kiosk ID: " + kiosk.getKioskID());
        // Further logic to handle reservation submission goes here...
    }
}
