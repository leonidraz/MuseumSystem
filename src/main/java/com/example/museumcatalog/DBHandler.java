package com.example.museumcatalog;

import javafx.scene.control.Alert;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBHandler {

    private static Connection connection;

    static Service service = new Service();

    public static void setConnection() throws ClassNotFoundException {
        Class.forName("org.postgresql.Driver");
        try {
            connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/MuseumDB", "postgres", "13068");
        } catch (SQLException e) {
            service.openAlert(Alert.AlertType.ERROR, "Нет подключения к БД!", "Ошибка!");
        }
    }

    public static ResultSet executeQuery(String query) throws SQLException {
        if (connection == null || connection.isClosed()) {
            System.out.println("Подключение к БД закрыто!");
            return null;
        }
        return connection.createStatement().executeQuery(query);
    }

    public static int executeUpdate(String query) throws SQLException {
        if (connection == null || connection.isClosed()) {
            System.out.println("Подключение к БД закрыто!");
            return 0;
        }
        return connection.createStatement().executeUpdate(query);
    }
}
