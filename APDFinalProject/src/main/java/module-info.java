module apdfinalproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.xerial.sqlitejdbc;

    exports apdfinalproject.application;
    opens apdfinalproject.application to javafx.fxml;

    exports apdfinalproject.database;
    opens apdfinalproject.database to javafx.fxml;

    exports apdfinalproject.models;
    exports apdfinalproject.dao;
    exports apdfinalproject.dto;

    exports apdfinalproject.controllers;
    opens apdfinalproject.controllers to javafx.fxml;
}
