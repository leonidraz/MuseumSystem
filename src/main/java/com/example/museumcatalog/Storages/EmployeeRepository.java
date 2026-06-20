package com.example.museumcatalog.Storages;

import com.example.museumcatalog.DBHandler;
import com.example.museumcatalog.Models.Employee;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EmployeeRepository {

    private static final ObservableList<Employee> allEmployees = FXCollections.observableArrayList();
    private static final ObservableList<Employee> activeEmployees = FXCollections.observableArrayList();

    public static ObservableList<Employee> getAllEmployees() {
        return allEmployees;
    }

    public static ObservableList<Employee> getActiveEmployees() {
        return activeEmployees;
    }

    //Загрузка всех сотрудников
    public static void loadAll() throws SQLException {
        allEmployees.clear();

        String query = """
                SELECT
                    e.id,
                    e.last_name,
                    e.first_name,
                    e.middle_name,
                    ep.position_name,
                    e.email,
                    e.phone,
                    es.status_name
                FROM employees e
                INNER JOIN employee_statuses es ON e.status_id = es.id
                INNER JOIN employee_positions ep ON e.position_id = ep.id
                """;

        ResultSet rs = DBHandler.executeQuery(query);

        while (rs.next()) {
            allEmployees.add(mapEmployee(rs));
        }
    }

    //Загрузка только активных
    public static void loadActive() throws SQLException {
        activeEmployees.clear();

        String query = """
                SELECT 
                    e.id,
                    e.last_name,
                    e.first_name,
                    e.middle_name,
                    ep.position_name,
                    e.email,
                    e.phone,
                    es.status_name
                FROM employees e
                INNER JOIN employee_statuses es ON e.status_id = es.id
                INNER JOIN employee_positions ep ON e.position_id = ep.id
                WHERE e.status_id = 1
                """;

        ResultSet rs = DBHandler.executeQuery(query);

        while (rs.next()) {
            activeEmployees.add(mapEmployee(rs));
        }
    }

    private static Employee mapEmployee(ResultSet rs) throws SQLException {
        return new Employee(
                rs.getInt("id"),
                rs.getString("last_name"),
                rs.getString("first_name"),
                rs.getString("middle_name"),
                rs.getString("position_name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("status_name")
        );
    }

    public static int addOrUpdate(Employee employee) throws SQLException {

        boolean isUpdate = employee.getId() != 0;
        int result = 0;

        if (!isUpdate) {

            String query = """
                INSERT INTO employees (
                    last_name,
                    first_name,
                    middle_name,
                    position_id,
                    email,
                    phone,
                    status_id
                )
                VALUES (
                    ?, ?, ?,
                    (SELECT id FROM employee_positions WHERE position_name = ?),
                    ?, ?,
                    (SELECT id FROM employee_statuses WHERE status_name = ?)
                )
                RETURNING id
                """;

            result = (int) DBHandler.executeReturning(
                    query,
                    "id",
                    employee.getLastName(),
                    employee.getFirstName(),
                    employee.getMiddleName(),
                    employee.getPosition(),
                    employee.getEmail(),
                    employee.getPhone(),
                    employee.getStatus()
            );

        } else {

            String query = """
                UPDATE employees SET
                    last_name = ?,
                    first_name = ?,
                    middle_name = ?,
                    position_id = (SELECT id FROM employee_positions WHERE position_name = ?),
                    email = ?,
                    phone = ?,
                    status_id = (SELECT id FROM employee_statuses WHERE status_name = ?)
                WHERE id = ?
                """;

            result = DBHandler.executeUpdate(
                    query,
                    employee.getLastName(),
                    employee.getFirstName(),
                    employee.getMiddleName(),
                    employee.getPosition(),
                    employee.getEmail(),
                    employee.getPhone(),
                    employee.getStatus(),
                    employee.getId()
            );
        }

        return result;
    }

    public static boolean deleteEmployee(int id) throws SQLException {
        String query = "DELETE FROM employees WHERE id = ?";
        return DBHandler.executeUpdate(query, id) > 0;
    }
}