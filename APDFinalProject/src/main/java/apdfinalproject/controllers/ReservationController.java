package apdfinalproject.controllers;

import apdfinalproject.models.Kiosk;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.io.File;
import java.io.IOException;
import java.util.logging.*;

public class ReservationController {
    private Kiosk kiosk;

    // set Kiosk for logging if the guest is doing self-service
    public void setKiosk(Kiosk kiosk) {
        this.kiosk = kiosk;

    }

    // Method to handle reservation submission (you can add more fields and logic)
    @FXML
    private void handleSubmitReservation() {
        // You can use the Kiosk object for logging or other purposes
        System.out.println("Reservation made from Kiosk ID: " + kiosk.getKioskID());
        // Further logic to handle reservation submission goes here...
    }
}
