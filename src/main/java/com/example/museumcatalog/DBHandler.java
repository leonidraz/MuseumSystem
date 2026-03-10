package com.example.museumcatalog;

import javafx.scene.control.Alert;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBHandler {

    private Connection connection;

    Service service = new Service();

    public void setConnection() throws ClassNotFoundException {
        Class.forName("org.postgresql.Driver");
        String url = "jdbc:postgresql://localhost:5432/MuseumDB";
        String user = "postgres";
        String password = "13068";
        try {
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            service.openAlert(Alert.AlertType.ERROR, "Нет подключения к БД!", "Ошибка!");
        }
    }

    public ResultSet executeQuery(String query) throws SQLException {
        if (connection == null || connection.isClosed()) {
            System.out.println("Подключение к БД закрыто!");
        } else {
            return connection.createStatement().executeQuery(query);
        }
        return null;
    }

    public int executeUpdate(String query) throws SQLException {
        if (connection == null || connection.isClosed()) {
            System.out.println("Подключение к БД закрыто!");
        } else {
            return connection.createStatement().executeUpdate(query);
        }
        return 0;
    }
}
