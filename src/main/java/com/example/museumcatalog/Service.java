package com.example.museumcatalog;

import com.example.museumcatalog.Models.Exhibit;
import com.example.museumcatalog.Models.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Service {
    private static User currentUser;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User currentUser) {
        Service.currentUser = currentUser;
    }

    private static Exhibit exhibit;

    public static Exhibit getExhibit() {
        return exhibit;
    }

    public static void setExhibit(Exhibit exhibit) {
        Service.exhibit = exhibit;
    }

    public void switchScene(String fileName, String nameForm) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("fxml/" + fileName + ".fxml"));
        Stage stage = MainApplication.getCurrentStage();
        stage.setScene(new Scene(fxmlLoader.load()));
        stage.setTitle(nameForm);
        if (!nameForm.equals("Авторизация")) {
            stage.setMaximized(true);
        } else {
            stage.setMaximized(false);
            stage.setWidth(466);
            stage.setHeight(539);
        }
    }

    public static <T> T openModal(String fxmlPath, String title, Stage ownerStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(Service.class.getResource("fxml/" + fxmlPath + ".fxml"));
        Parent root = loader.load();

        Stage stage = new Stage();
        stage.initStyle(StageStyle.UNDECORATED);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(ownerStage);
        stage.setScene(new Scene(root));
        stage.showAndWait();

        return loader.getController();
    }

    public void openAlert(Alert.AlertType type, String msg, String title){
        Alert alert = new Alert(type, msg);
        alert.setTitle(title);
        alert.show();
    }

    public ArrayList<String> getValuesComboBox(String table, String column, String where, Object... params) throws SQLException {
        ArrayList<String> list = new ArrayList<>();
        String query = "SELECT " + column + " FROM " + table;
        if (where != null && !where.isEmpty()) {
            query += " WHERE " + where;
        }

        ResultSet rs = DBHandler.executeQuery(query, params);

        while (rs.next()) {
            list.add(rs.getString(column));
        }

        return list;
    }
}
