module com.example.apdfinalproject {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.apdfinalproject to javafx.fxml;
    exports com.example.apdfinalproject;
}