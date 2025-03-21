package apdfinalproject.models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Guest {
    private final IntegerProperty guestId;
    private final StringProperty name;
    private final StringProperty phoneNumber;
    private final StringProperty email;
    private final StringProperty address;
    private final StringProperty feedback;

    // Constructors
    public Guest(int guestId, String name, String phoneNumber, String email, String address) {
        this.guestId = new SimpleIntegerProperty(guestId);
        this.name = new SimpleStringProperty(name);
        this.phoneNumber = new SimpleStringProperty(phoneNumber);
        this.email = new SimpleStringProperty(email);
        this.address = new SimpleStringProperty(address);
        this.feedback = new SimpleStringProperty(null);
    }

    public IntegerProperty guestIdProperty() {
        return guestId;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty phoneNumberProperty() {
        return phoneNumber;
    }

    public StringProperty emailProperty() {
        return email;
    }

    public StringProperty addressProperty() {
        return address;
    }

    public StringProperty feedbackProperty() {
        return feedback;
    }

    public int getGuestId() {
        return guestId.get();
    }

    public void setGuestId(int guestId) {
        this.guestId.set(guestId);
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getPhoneNumber() {
        return phoneNumber.get();
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber.set(phoneNumber);
    }

    public String getEmail() {
        return email.get();
    }

    public void setEmail(String email) {
        this.email.set(email);
    }

    public String getAddress() {
        return address.get();
    }

    public void setAddress(String address) {
        this.address.set(address);
    }

    public String getFeedback() {
        return feedback.get();
    }

    public void setFeedback(String feedback) {
        this.feedback.set(feedback);
    }

    public void getGuestDetails() {
    }

    public void setGuestDetails(String name, String phoneNumber, String email, String address) {
        setName(name);
        setPhoneNumber(phoneNumber);
        setEmail(email);
        setAddress(address);
    }

    public boolean validateGuestDetails() {
        return true;
    }
}
