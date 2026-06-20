package com.example.museumcatalog.Storages;

import com.example.museumcatalog.DBHandler;
import com.example.museumcatalog.DocumentEmployeeRole;
import com.example.museumcatalog.Models.DocumentEmployeeRelation;
import com.example.museumcatalog.Models.Employee;
import com.example.museumcatalog.Models.Exhibit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DocumentRelationsRepository {

    public static void addEmployees(int documentId, List<DocumentEmployeeRelation> relations) throws SQLException {
        String sql = """
        INSERT INTO documents_employees
        (document_id, employee_id, role_in_document)
        VALUES (?, ?, ?)
    """;
        for (DocumentEmployeeRelation r : relations) {
            DBHandler.executeUpdate(
                    sql,
                    documentId,
                    r.getEmployee().getId(),
                    r.getRole().name()
            );
        }
    }

    public static List<DocumentEmployeeRelation> getEmployees(int documentId) throws SQLException {
        List<DocumentEmployeeRelation> list = new ArrayList<>();
        ResultSet rs = DBHandler.executeQuery("""
        SELECT employee_id, role_in_document
        FROM documents_employees
        WHERE document_id = ?
    """, documentId);
        while (rs.next()) {
            int employeeId = rs.getInt("employee_id");
            Employee employee = EmployeeRepository.getActiveEmployees()
                    .stream()
                    .filter(e -> e.getId() == employeeId)
                    .findFirst()
                    .orElse(null);
            if (employee != null) {
                DocumentEmployeeRole role = DocumentEmployeeRole.valueOf(rs.getString("role_in_document"));
                list.add(new DocumentEmployeeRelation(employee, role));
            }
        }
        return list;
    }


    public static void addExhibits(int documentId, List<Exhibit> exhibits) throws SQLException {
        String sql = """
            INSERT INTO documents_exhibits (document_id, exhibit_id)
            VALUES (?, ?)
        """;
        for (Exhibit ex : exhibits) {
            if (ex.isSelected()) {
                DBHandler.executeUpdate(sql, documentId, ex.getId());
            }
        }
    }

    public static List<Exhibit> getExhibits(int documentId) throws SQLException {
        List<Exhibit> list = new ArrayList<>();
        ResultSet rs = DBHandler.executeQuery("""
        SELECT exhibit_id
        FROM documents_exhibits
        WHERE document_id = ?
    """, documentId);

        while (rs.next()) {

            int exhibitId = rs.getInt("exhibit_id");

            Exhibit exhibit = ExhibitRepository.getExhibits()
                    .stream()
                    .filter(e -> e.getId() == exhibitId)
                    .findFirst()
                    .orElse(null);

            if (exhibit != null) {
                list.add(exhibit);
            }
        }

        return list;
    }


    public static void saveAll(int documentId, List<DocumentEmployeeRelation> employees, List<Exhibit> exhibits) throws SQLException {
        addEmployees(documentId, employees);
        addExhibits(documentId, exhibits);
    }


    public static void loadAllRelations(int documentId, List<DocumentEmployeeRelation> employeesTarget, List<Exhibit> exhibitsTarget) throws SQLException {
        employeesTarget.clear();
        employeesTarget.addAll(getEmployees(documentId));

        exhibitsTarget.clear();
        exhibitsTarget.addAll(getExhibits(documentId));
    }

    public static void clearRelations(int documentId) throws SQLException {
        DBHandler.executeUpdate("DELETE FROM documents_employees WHERE document_id = ?", documentId);
        DBHandler.executeUpdate("DELETE FROM documents_exhibits WHERE document_id = ?", documentId);
    }
}