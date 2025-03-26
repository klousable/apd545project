package apdfinalproject.models;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.util.List;

public class Reservation {

    private IntegerProperty reservationID;
    private IntegerProperty guestID;
    private ObjectProperty<LocalDate> checkInDate;
    private ObjectProperty<LocalDate> checkOutDate;
    private IntegerProperty numberOfGuests;
    private StringProperty status;
    private List<Integer> roomIds;  // List of room IDs associated with this reservation

    // Constructor
    public Reservation(int reservationID, int guestID, LocalDate checkInDate, LocalDate checkOutDate, int numberOfGuests, String status, List<Integer> roomIds) {
        this.reservationID = new SimpleIntegerProperty(reservationID);
        this.guestID = new SimpleIntegerProperty(guestID);
        this.checkInDate = new SimpleObjectProperty<>(checkInDate);
        this.checkOutDate = new SimpleObjectProperty<>(checkOutDate);
        this.numberOfGuests = new SimpleIntegerProperty(numberOfGuests);
        this.status = new SimpleStringProperty(status);
        this.roomIds = roomIds;
    }

    public IntegerProperty reservationIDProperty() {
        return reservationID;
    }

    public IntegerProperty guestIDProperty() {
        return guestID;
    }

    public ObjectProperty<LocalDate> checkInDateProperty() {
        return checkInDate;
    }

    public ObjectProperty<LocalDate> checkOutDateProperty() {
        return checkOutDate;
    }

    public IntegerProperty numberOfGuestsProperty() {
        return numberOfGuests;
    }

    public StringProperty statusProperty() {
        return status;
    }

    // Getter for the list of room IDs
    public List<Integer> getRoomIds() {
        return roomIds;
    }

    public int getReservationID() {
        return reservationID.get();
    }

    public int getGuestID() {
        return guestID.get();
    }

    public LocalDate getCheckInDate() {
        return checkInDate.get();
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate.get();
    }

    public int getNumberOfGuests() {
        return numberOfGuests.get();
    }

    public String getStatus() {
        return status.get();
    }

    public void setReservationID(int reservationID) {
        this.reservationID.set(reservationID);
    }


    public void setGuestID(int guestID) {
        this.guestID.set(guestID);
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate.set(checkInDate);
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate.set(checkOutDate);
    }

    public void setNumberOfGuests(int numberOfGuests) {
        this.numberOfGuests.set(numberOfGuests);
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    public void setRoomIds(List<Integer> roomIds) {
        this.roomIds = roomIds;
    }
}
