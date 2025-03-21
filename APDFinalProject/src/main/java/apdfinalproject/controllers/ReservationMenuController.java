package apdfinalproject.controllers;

import apdfinalproject.models.Admin;

public class ReservationMenuController {

    private Admin admin;

    public ReservationMenuController(Admin admin) {
        this.admin = admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

}
