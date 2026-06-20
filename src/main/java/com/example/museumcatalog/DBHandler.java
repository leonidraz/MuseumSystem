package com.example.museumcatalog;

import javafx.scene.control.Alert;

import java.sql.*;

public class DBHandler {

    private static Connection connection;

    static Service service = new Service();

    public static void setConnection() throws ClassNotFoundException {
        Class.forName("org.postgresql.Driver");
        try {
            connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/MuseumDB2", "postgres", "13068");
        } catch (SQLException e) {
            service.openAlert(Alert.AlertType.ERROR, "Нет подключения к БД!", "Ошибка!");
        }
    }

    public static ResultSet executeQuery(String query, Object...params) throws SQLException {
        if (connection == null || connection.isClosed()) {
            System.out.println("Подключение к БД закрыто!");
            return null;
        }
        PreparedStatement stmt = connection.prepareStatement(query);

        if (params != null) {
            for (int i = 0; i< params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
        }
        return stmt.executeQuery();
    }

    public static int executeUpdate(String query, Object... params) throws SQLException {
        if (connection == null || connection.isClosed()) {
            System.out.println("Подключение к БД закрыто!");
            return -1;
        }

        PreparedStatement stmt = connection.prepareStatement(query);

        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
        }

        return stmt.executeUpdate();
    }
    public static Object executeReturning(String sql, String column, Object... params) throws SQLException {

        if (connection == null || connection.isClosed()) {
            throw new SQLException("Подключение к БД закрыто!");
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getObject(column);
                }
            }

            return -1;
        }
    }
}
