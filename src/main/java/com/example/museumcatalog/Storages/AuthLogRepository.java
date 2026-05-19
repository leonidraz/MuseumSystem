package com.example.museumcatalog.Storages;

import com.example.museumcatalog.DBHandler;
import com.example.museumcatalog.Models.AuthLog;
import com.example.museumcatalog.Models.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthLogRepository {

    private static final ObservableList<AuthLog> authLogs = FXCollections.observableArrayList();

    public static ObservableList<AuthLog> getAllLogs() {
        return authLogs;
    }

    public static void loadAll() throws SQLException {
        authLogs.clear();
        String query = """
                SELECT\s
                    l.id,
                    l.user_id,
                    l.login,
                    e.last_name || ' ' || e.first_name || ' ' || COALESCE(e.middle_name, '') AS fio,
                    l.event_type,
                    l.success,
                    l.ip_address,
                    l.attempt_time
                FROM auth_logs l
                LEFT JOIN users u ON l.user_id = u.id
                LEFT JOIN employees e ON u.employee_id = e.id
        """;

        ResultSet rs = DBHandler.executeQuery(query);

        while (rs.next()) {
            Integer userId = rs.getInt("user_id");
            if (rs.wasNull()) userId = null;
            AuthLog log = new AuthLog(
                    rs.getInt("id"),
                    userId,
                    rs.getString("login"),
                    rs.getString("fio"),
                    rs.getString("event_type"),
                    rs.getBoolean("success"),
                    rs.getString("ip_address"),
                    rs.getTimestamp("attempt_time").toLocalDateTime()
            );

            authLogs.add(log);
        }
    }
    public static void addLog(User user, String login, boolean success, String action) throws SQLException {

        String query = """
            INSERT INTO auth_logs (user_id, login, event_type, success, ip_address)
            VALUES (?, ?, ?, ?, ?)
        """;

        DBHandler.executeUpdate(
                query,
                user != null ? user.getId() : null,
                login,
                action,
                success,
                getIp()
        );
    }

    private static String getIp() {
        try {
            return InetAddress.getLocalHost().getHostName()  + ", " + InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }
}