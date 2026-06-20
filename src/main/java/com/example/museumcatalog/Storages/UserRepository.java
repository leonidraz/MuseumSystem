package com.example.museumcatalog.Storages;

import com.example.museumcatalog.DBHandler;
import com.example.museumcatalog.Models.User;
import com.example.museumcatalog.SecurityUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRepository {
    private static final ObservableList<User> users = FXCollections.observableArrayList();

    public static ObservableList<User> getUsers() {
        return users;
    }

    public static User checkUser(String login, String password) throws SQLException {

        String query = """
            SELECT
                u.id,
                u.login,
                u.password,
                r.role_name,
                us.status_name,
                u.employee_id,
                CONCAT(e.last_name, ' ', e.first_name, ' ', COALESCE(e.middle_name, '')) AS employee_fio
            FROM users u
            INNER JOIN roles r ON u.role_id = r.id
            LEFT JOIN employees e ON u.employee_id = e.id
            INNER JOIN user_statuses us ON u.status_id = us.id
            WHERE u.login = ?
            """;

        ResultSet rs = DBHandler.executeQuery(query, login);

        if (rs.next()) {

            String hashedPassword = rs.getString("password");

            if (!SecurityUtil.verifyPassword(password, hashedPassword)) {
                return null;
            }

            return new User(
                    rs.getInt("id"),
                    rs.getString("role_name"),
                    rs.getString("status_name"),
                    rs.getString("employee_fio"),
                    rs.getInt("employee_id"),
                    rs.getString("login"),
                    null
            );
        }

        return null;
    }

    public static void loadAll() throws SQLException {
        users.clear();

        String query = """
                SELECT
                    u.id,
                    u.login,
                    u.password,
                    r.role_name,
                    us.status_name,
                    u.employee_id,
                    CONCAT(
                        COALESCE(e.last_name, ''), ' ',
                        COALESCE(e.first_name, ''), ' ',
                        COALESCE(e.middle_name, '')
                    ) AS employee_fio
                FROM users u
                INNER JOIN roles r ON u.role_id = r.id
                LEFT JOIN employees e ON u.employee_id = e.id
                INNER JOIN user_statuses us ON u.status_id = us.id
                """;

        ResultSet rs = DBHandler.executeQuery(query);

        while (rs.next()) {
            Integer employeeId = rs.getInt("employee_id");
            if (rs.wasNull()) employeeId = null;
            users.add(new User(
                    rs.getInt("id"),
                    rs.getString("role_name"),
                    rs.getString("status_name"),
                    rs.getString("employee_fio"),
                    employeeId,
                    rs.getString("login"),
                    rs.getString("password")
            ));
        }
    }

    public static String hashPassword(String password) {
        return SecurityUtil.hashPassword(password);
    }

    public static int addOrUpdate(User user) throws SQLException {

        boolean isUpdate = user.getId() != 0;

        if (!isUpdate) {

            String query = """
                INSERT INTO users (login, password, role_id, employee_id, status_id)
                VALUES (
                    ?,
                    ?,
                    (SELECT id FROM roles WHERE role_name = ?),
                    ?,
                    (SELECT id FROM user_statuses WHERE status_name = ?)
                )
                RETURNING id
                """;

            return (int) DBHandler.executeReturning(query, "id",
                    user.getLogin(),
                    user.getPassword(),
                    user.getRole(),
                    user.getEmployeeId() != null ? user.getEmployeeId() : null,
                    user.getStatus()
            );
        }

        String query = """
                UPDATE users SET
                    login = ?,
                    password = ?,
                    role_id = (SELECT id FROM roles WHERE role_name = ?),
                    employee_id = ?,
                    status_id = (SELECT id FROM user_statuses WHERE status_name = ?)
                WHERE id = ?
                """;

        return DBHandler.executeUpdate(query,
                user.getLogin(),
                user.getPassword(),
                user.getRole(),
                user.getEmployeeId() != null ? user.getEmployeeId() : null,
                user.getStatus(),
                user.getId()
        );
    }

    public static boolean hasUserDocuments(int userId) throws SQLException {
        String query = """
        SELECT 1
        FROM documents
        WHERE creator_id = ?
        LIMIT 1
        """;

        ResultSet rs = DBHandler.executeQuery(query, userId);

        return rs.next();
    }

    public static boolean deleteUser(int id) throws SQLException {
        String query = "DELETE FROM users WHERE id = ?";
        return DBHandler.executeUpdate(query, id) > 0;
    }
}