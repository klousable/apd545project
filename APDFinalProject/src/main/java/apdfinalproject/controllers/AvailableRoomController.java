package apdfinalproject.controllers;

import apdfinalproject.dao.RoomDAO;
import apdfinalproject.models.Room;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class AvailableRoomController {
    @FXML
    private TableView<Room> roomTableView;
    @FXML
    private TableColumn<?, ?> roomIdColumn;
    @FXML
    private TableColumn<?, ?> roomTypeColumn;
    @FXML
    private TableColumn<?, ?> roomBedsColumn;
    @FXML
    private TableColumn<?, ?> roomPriceColumn;
    @FXML
    private TableColumn<?, ?> roomStatusColumn;
    @FXML
    public Button exitButton;

    @FXML
    public void initialize() {
        roomIdColumn.setCellValueFactory(new PropertyValueFactory<>("roomID"));
        roomTypeColumn.setCellValueFactory(new PropertyValueFactory<>("roomType"));
        roomBedsColumn.setCellValueFactory(new PropertyValueFactory<>("numberOfBeds"));
        roomPriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        roomStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        try {
            RoomDAO roomDAO = new RoomDAO();
            ObservableList<Room> availableRooms = roomDAO.getAllAvailableRooms();
            roomTableView.setItems(availableRooms);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleExitAction(ActionEvent actionEvent) {
        Stage currentStage = (Stage) exitButton.getScene().getWindow();
        currentStage.close();
    }
}
