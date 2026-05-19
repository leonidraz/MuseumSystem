package com.example.museumcatalog;

import com.example.museumcatalog.Controllers.BaseFormController;
import com.example.museumcatalog.Models.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Control;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Service {
    private static BaseFormController baseFormController;

    public static void setBaseFormController(BaseFormController controller) {
        baseFormController = controller;
    }

    public static BaseFormController getBaseFormController() {
        return baseFormController;
    }

    private static User currentUser;
    public static User getCurrentUser() {
        return currentUser;
    }
    public static void setCurrentUser(User currentUser) {
        Service.currentUser = currentUser;
    }

    private static Runnable userChangedListener;

    public static void setUserChangedListener(Runnable listener) {
        userChangedListener = listener;
    }

    public static void notifyUserChanged() {
        if (userChangedListener != null) {
            userChangedListener.run();
        }
    }

    private static User user;
    public static User getUser() {
        return user;
    }
    public static void setUser(User user) {
        Service.user = user;
    }

    private static Exhibit exhibit;
    public static Exhibit getExhibit() {
        return exhibit;
    }
    public static void setExhibit(Exhibit exhibit) {
        Service.exhibit = exhibit;
    }

    private static Employee employee;
    public static Employee getEmployee() {
        return employee;
    }
    public static void setEmployee(Employee employee) {
        Service.employee = employee;
    }

    private static Owner owner;
    public static Owner getOwner() {
        return owner;
    }
    public static void setOwner(Owner owner) {
        Service.owner = owner;
    }

    private static Document editingDocument = null;
    public static void setEditingDocument(Document doc) {
        editingDocument = doc;
    }
    public static Document getEditingDocument() {
        return editingDocument;
    }

    private static Fund fund;
    public static Fund getFund() {
        return fund;
    }

    public static void setFund(Fund fund) {
        Service.fund = fund;
    }

    private static Collection collection;

    public static Collection getCollection() {
        return collection;
    }

    public static void setCollection(Collection collection) {
        Service.collection = collection;
    }

    private static EmployeePosition employeePosition;
    public static EmployeePosition getEmployeePosition() {
        return employeePosition;
    }

    public static void setEmployeePosition(EmployeePosition employeePosition) {
        Service.employeePosition = employeePosition;
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

    //Подсвечивает обязательное поле, если оно не заполнено и убирает подсветку при получении фокуса
    public void markFieldAsError(Control field) {
        field.getStyleClass().add("error-field");
        field.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                field.getStyleClass().remove("error-field");
            }
        });
    }

    public void clearAllErrorStyles(Node node) {
        if (node instanceof Control control) {
            control.getStyleClass().remove("error-field");
        }

        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                clearAllErrorStyles(child);
            }
        }
    }
}
