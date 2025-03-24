package apdfinalproject.models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Feedback {
    private IntegerProperty feedbackId;
    private IntegerProperty guestId;
    private IntegerProperty reservationId;
    private StringProperty comments;
    private IntegerProperty rating;

    // Constructor
    public Feedback(int feedbackId, int guestId, int reservationId, String comments, int rating) {
        this.feedbackId = new SimpleIntegerProperty(feedbackId);
        this.guestId = new SimpleIntegerProperty(guestId);
        this.reservationId = new SimpleIntegerProperty(reservationId);
        this.comments = new SimpleStringProperty(comments);
        this.rating = new SimpleIntegerProperty(rating);
    }

    // Getters and Setters for properties
    public IntegerProperty feedbackIdProperty() {
        return feedbackId;
    }

    public int getFeedbackId() {
        return feedbackId.get();
    }

    public void setFeedbackId(int feedbackId) {
        this.feedbackId.set(feedbackId);
    }

    public IntegerProperty guestIdProperty() {
        return guestId;
    }

    public int getGuestId() {
        return guestId.get();
    }

    public void setGuestId(int guestId) {
        this.guestId.set(guestId);
    }

    public IntegerProperty reservationIdProperty() {
        return reservationId;
    }

    public int getReservationId() {
        return reservationId.get();
    }

    public void setReservationId(int reservationId) {
        this.reservationId.set(reservationId);
    }

    public StringProperty commentsProperty() {
        return comments;
    }

    public String getComments() {
        return comments.get();
    }

    public void setComments(String comments) {
        this.comments.set(comments);
    }

    public IntegerProperty ratingProperty() {
        return rating;
    }

    public int getRating() {
        return rating.get();
    }

    public void setRating(int rating) {
        this.rating.set(rating);
    }
}
