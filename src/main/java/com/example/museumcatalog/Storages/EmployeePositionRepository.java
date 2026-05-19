package com.example.museumcatalog.Storages;

import com.example.museumcatalog.DBHandler;
import com.example.museumcatalog.Models.EmployeePosition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EmployeePositionRepository {

    private static final ObservableList<EmployeePosition> positions =
            FXCollections.observableArrayList();
    public static ObservableList<EmployeePosition> getPositions() {
        return positions;
    }

    public static void loadAll() throws SQLException {
        positions.clear();

        String sql = """
                SELECT id, position_name
                FROM employee_positions
                """;

        ResultSet rs = DBHandler.executeQuery(sql);

        while (rs.next()) {
            EmployeePosition position = new EmployeePosition();

            position.setId(rs.getInt("id"));
            position.setPositionName(rs.getString("position_name"));

            positions.add(position);
        }
    }

    public static int addOrUpdate(EmployeePosition position) throws SQLException {

        boolean isUpdate = position.getId() != 0;
        int result;

        if (!isUpdate) {

            String query = """
                    INSERT INTO employee_positions(position_name)
                    VALUES (?)
                    RETURNING id
                    """;

            result = (int) DBHandler.executeReturning(
                    query,
                    "id",
                    position.getPositionName()
            );

        } else {

            String query = """
                    UPDATE employee_positions
                    SET position_name = ?
                    WHERE id = ?
                    """;

            result = DBHandler.executeUpdate(
                    query,
                    position.getPositionName(),
                    position.getId()
            );
        }

        return result;
    }

    public static boolean deletePosition(int id) throws SQLException {
        String query = "DELETE FROM employee_positions WHERE id = ?";
        return DBHandler.executeUpdate(query, id) > 0;
    }
}