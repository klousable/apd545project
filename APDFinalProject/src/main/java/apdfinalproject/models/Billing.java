package apdfinalproject.models;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class Billing {

    private final IntegerProperty billID;
    private final IntegerProperty reservationID;
    private final DoubleProperty amount;
    private final DoubleProperty tax;
    private final DoubleProperty totalAmount;
    private final DoubleProperty discount;

    // Constructors
    public Billing(int billID, int reservationID, double amount, double tax, double totalAmount, double discount) {
        this.billID = new SimpleIntegerProperty(billID);
        this.reservationID = new SimpleIntegerProperty(reservationID);
        this.amount = new SimpleDoubleProperty(amount);
        this.tax = new SimpleDoubleProperty(tax);
        this.totalAmount = new SimpleDoubleProperty(totalAmount);
        this.discount = new SimpleDoubleProperty(discount);
    }

    // Getters and Setters for properties
    public int getBillID() {
        return billID.get();
    }

    public void setBillID(int billID) {
        this.billID.set(billID);
    }

    public IntegerProperty billIDProperty() {
        return billID;
    }

    public int getReservationID() {
        return reservationID.get();
    }

    public void setReservationId(int selectedReservationId) {
    }

    public IntegerProperty reservationIDProperty() {
        return reservationID;
    }

    public double getAmount() {
        return amount.get();
    }

    public void setAmount(double amount) {
        this.amount.set(amount);
    }

    public DoubleProperty amountProperty() {
        return amount;
    }

    public double getTax() {
        return tax.get();
    }

    public void setTax(double tax) {
        this.tax.set(tax);
    }

    public DoubleProperty taxProperty() {
        return tax;
    }

    public double getTotalAmount() {
        return totalAmount.get();
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount.set(totalAmount);
    }

    public DoubleProperty totalAmountProperty() {
        return totalAmount;
    }

    public double getDiscount() {
        return discount.get();
    }

    public void setDiscount(double discount) {
        this.discount.set(discount);
    }

    public DoubleProperty discountProperty() {
        return discount;
    }

}