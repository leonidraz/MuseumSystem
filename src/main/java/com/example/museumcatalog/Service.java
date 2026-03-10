package com.example.museumcatalog;

import com.example.museumcatalog.Models.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

public class Service {
    private static User currentUser;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User currentUser) {
        Service.currentUser = currentUser;
    }

    public void switchScene(String fileName, String nameForm) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("fxml/" + fileName + ".fxml"));
        MainApplication.getCurrentStage().setScene(new Scene(fxmlLoader.load()));
        MainApplication.getCurrentStage().setTitle(nameForm);
        MainApplication.getCurrentStage().show();
    }

    public void openAlert(Alert.AlertType type, String msg, String title){
        Alert alert = new Alert(type, msg);
        alert.setTitle(title);
        alert.show();
    }
}
