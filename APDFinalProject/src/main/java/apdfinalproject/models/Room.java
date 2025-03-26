package apdfinalproject.models;

import javafx.beans.property.*;

public class Room {
    private final IntegerProperty roomID;
    private final ObjectProperty<RoomType> roomType;
    private final IntegerProperty reservationID;
    private final IntegerProperty numberOfBeds;
    private final DoubleProperty price;
    private final StringProperty status;

    public Room(int roomID, RoomType roomType, int reservationID, int numberOfBeds, double price, String status) {
        this.roomID = new SimpleIntegerProperty(roomID);
        this.roomType = new SimpleObjectProperty<>(roomType);
        this.reservationID = new SimpleIntegerProperty(reservationID);
        this.numberOfBeds = new SimpleIntegerProperty(numberOfBeds);
        this.price = new SimpleDoubleProperty(price);
        this.status = new SimpleStringProperty(status);

        validateRoomDetails(roomType, numberOfBeds);
    }

    private void validateRoomDetails(RoomType roomType, int numberOfBeds) {
        switch (roomType) {
            case SINGLE:
                if (numberOfBeds != 1) {
                    throw new IllegalArgumentException("Single rooms must have 1 bed.");
                }
                break;
            case DOUBLE:
                if (numberOfBeds != 2) {
                    throw new IllegalArgumentException("Double rooms must have 2 beds.");
                }
                break;
            case DELUXE:
                if (numberOfBeds <0) {
                    throw new IllegalArgumentException("Deluxe rooms must have at least 1 bed.");
                }
                break;
            case PENTHOUSE:
                if (numberOfBeds < 1 || numberOfBeds > 2) {
                    throw new IllegalArgumentException("Deluxe and Penthouse rooms must have 1 or 2 beds.");
                }
                break;
        }
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
