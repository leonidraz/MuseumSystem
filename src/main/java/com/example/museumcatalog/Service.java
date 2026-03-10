package com.example.museumcatalog;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

public class Service {

    public void openForm(String fileName, String nameForm) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("fxml/" + fileName + ".fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = new Stage();
        stage.setTitle(nameForm);
        stage.setScene(scene);
        stage.show();
    }

    public void openAlert(Alert.AlertType type, String msg, String title){
        Alert alert = new Alert(type, msg);
        alert.setTitle(title);
        alert.show();
    }
}
