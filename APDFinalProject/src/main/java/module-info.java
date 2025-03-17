module apdfinalproject {
    requires javafx.controls;
    requires javafx.fxml;


    opens apdfinalproject to javafx.fxml;
    exports apdfinalproject;
    exports apdfinalproject.application;
    opens apdfinalproject.application to javafx.fxml;
}