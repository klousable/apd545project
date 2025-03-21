package apdfinalproject.models;

import javafx.beans.property.*;

public class Admin {
    private final IntegerProperty adminId;
    private final StringProperty username;
    private final StringProperty password;

    public Admin(int adminId, String username, String password) {
        this.adminId = new SimpleIntegerProperty(adminId);
        this.username = new SimpleStringProperty(username);
        this.password = new SimpleStringProperty(password);
    }

    public IntegerProperty adminIdProperty() {
        return adminId;
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public StringProperty passwordProperty() {
        return password;
    }

    public int getAdminId() {
        return adminId.get();
    }

    public void setAdminId(int adminId) {
        this.adminId.set(adminId);
    }

    public String getUsername() {
        return username.get();
    }

    public void setUsername(String username) {
        this.username.set(username);
    }

    public String getPassword() {
        return password.get();
    }

    public void setPassword(String password) {
        this.password.set(password);
    }

}
