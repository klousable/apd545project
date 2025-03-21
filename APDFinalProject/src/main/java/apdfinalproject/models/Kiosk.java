package apdfinalproject.models;

public class Kiosk {
    private int kioskID;
    private String location;

    // Constructor
    public Kiosk(int kioskID, String location) {
        this.kioskID = kioskID;
        this.location = location;
    }

    // Getters and Setters
    public int getKioskID() {
        return kioskID;
    }

    public void setKioskID(int kioskID) {
        this.kioskID = kioskID;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    // Methods
    public String displayWelcomeMessage() {
        return "Welcome to the Self-Service Kiosk (ID:" + getKioskID() + ") located at " + getLocation();
    }

    public void guideBookingProcess() {
        System.out.println("Guiding you through the booking process...");
        // Add logic here to guide the user through booking.
    }

    public boolean validateInput(String input) {
        // Implement input validation logic (e.g., check if the input is valid)
        return input != null && !input.trim().isEmpty();
    }

    public boolean confirmBooking(String details) {
        // Logic to confirm the booking
        System.out.println("Booking confirmed for " + details);
        return true; // Assuming booking is confirmed successfully
    }

    @Override
    public String toString() {
        return "Kiosk[ID=" + kioskID + ", Location=" + location + "]";
    }
}
