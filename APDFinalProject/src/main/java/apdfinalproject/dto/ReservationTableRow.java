package apdfinalproject.dto;

import javafx.beans.property.*;

import java.time.LocalDate;

public class ReservationTableRow {
    private IntegerProperty reservationId;
    private IntegerProperty guestId;
    private StringProperty guestName;
    private StringProperty guestPhoneNumber;
    private ObjectProperty<LocalDate> checkInDate;
    private ObjectProperty<LocalDate> checkOutDate;
    private StringProperty status;
    private DoubleProperty price;
    private StringProperty feedbackStatus;

    public ReservationTableRow(int reservationId, int guestId, String guestName, String guestPhoneNumber,
                               LocalDate checkInDate, LocalDate checkOutDate, String status,
                               double price, String feedbackStatus) {
        this.reservationId = new SimpleIntegerProperty(reservationId);
        this.guestId = new SimpleIntegerProperty(guestId);
        this.guestName = new SimpleStringProperty(guestName);
        this.guestPhoneNumber = new SimpleStringProperty(guestPhoneNumber);
        this.checkInDate = new SimpleObjectProperty<>(checkInDate);
        this.checkOutDate = new SimpleObjectProperty<>(checkOutDate);
        this.status = new SimpleStringProperty(status);
        this.price = new SimpleDoubleProperty(price);
        this.feedbackStatus = new SimpleStringProperty(feedbackStatus);
    }

    // Getters (for TableView bindings)
    public IntegerProperty reservationIdProperty() { return reservationId; }
    public IntegerProperty guestIdProperty() { return guestId; }
    public StringProperty guestNameProperty() { return guestName; }
    public StringProperty guestPhoneNumberProperty() { return guestPhoneNumber; }
    public ObjectProperty<LocalDate> checkInDateProperty() { return checkInDate; }
    public ObjectProperty<LocalDate> checkOutDateProperty() { return checkOutDate; }
    public StringProperty statusProperty() { return status; }
    public DoubleProperty priceProperty() { return price; }
    public StringProperty feedbackStatusProperty() { return feedbackStatus; }

    // Optional: plain getters if you still want to use them elsewhere
    public int getReservationId() { return reservationId.get(); }
    public int getGuestId() { return guestId.get(); }
    public String getGuestName() { return guestName.get(); }
    public String getGuestPhoneNumber() { return guestPhoneNumber.get(); }
    public LocalDate getCheckInDate() { return checkInDate.get(); }
    public LocalDate getCheckOutDate() { return checkOutDate.get(); }
    public String getStatus() { return status.get(); }
    public double getPrice() { return price.get(); }
    public String getFeedbackStatus() { return feedbackStatus.get(); }
}