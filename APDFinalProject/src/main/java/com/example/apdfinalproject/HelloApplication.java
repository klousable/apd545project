package com.example.apdfinalproject;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("start-menu.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
//
//notes for ui mockup
//        guest does not need add existing, search by phone number and update id field if the guest exists
//        start menu does not need a video, image works as well and text guide
//        reservation menu needs a selected rooms and add rooms, make the window larger
//        feedback button added to main menu, search by reservation id for privacy then add feedback
//        add heading to admin-login
//        main menu remove second table, add more column headings for guest name, phone number, price
//        add button on the button to show available rooms
//        admin cannot change bookings? forgot
