package apdfinalproject.models;

import javafx.beans.property.*;

public class Room {
    private final IntegerProperty roomID;
    private final ObjectProperty<RoomType> roomType;
    private final IntegerProperty reservationID;
    private final IntegerProperty numberOfBeds;
    private final DoubleProperty price;
    private final StringProperty status;

    // Updated Constructor
    public Room(int roomID, RoomType roomType, int reservationID, int numberOfBeds, double price, String status) {
        this.roomID = new SimpleIntegerProperty(roomID);
        this.roomType = new SimpleObjectProperty<>(roomType);
        this.reservationID = new SimpleIntegerProperty(reservationID);
        this.numberOfBeds = new SimpleIntegerProperty(numberOfBeds);
        this.price = new SimpleDoubleProperty(price);
        this.status = new SimpleStringProperty(status);
    }

    public int getRoomID() {
        return roomID.get();
    }

    public void setRoomID(int roomID) {
        this.roomID.set(roomID);
    }

    public IntegerProperty roomIDProperty() {
        return roomID;
    }

    public int getReservationID() { return reservationID.get(); }

    public RoomType getRoomType() {
        return roomType.get();
    }

    public void setRoomType(RoomType roomType) {
        this.roomType.set(roomType);
    }

    public ObjectProperty<RoomType> roomTypeProperty() {
        return roomType;
    }

    public int getNumberOfBeds() {
        return numberOfBeds.get();
    }

    public void setNumberOfBeds(int numberOfBeds) {
        this.numberOfBeds.set(numberOfBeds);
    }

    public IntegerProperty numberOfBedsProperty() {
        return numberOfBeds;
    }

    public double getPrice() {
        return price.get();
    }

    public void setPrice(double price) {
        this.price.set(price);
    }

    public DoubleProperty priceProperty() {
        return price;
    }

    public String getStatus() {
        return status.get();
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    public StringProperty statusProperty() {
        return status;
    }
}
