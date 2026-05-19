package com.example.museumcatalog.Storages;

import com.example.museumcatalog.DBHandler;
import com.example.museumcatalog.Models.Employee;
import com.example.museumcatalog.Models.Exhibit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DocumentRelationsRepository {

    public static void addEmployees(int documentId, List<Employee> employees) throws SQLException {

        String sql = """
            INSERT INTO documents_employees (document_id, employee_id)
            VALUES (?, ?)
        """;

        for (Employee e : employees) {
            if (e.isSelected()) {
                DBHandler.executeUpdate(sql, documentId, e.getId());
            }
        }
    }

    public static List<Employee> getEmployees(int documentId) throws SQLException {

        List<Employee> list = new ArrayList<>();

        ResultSet rs = DBHandler.executeQuery("""
            SELECT e.id, e.last_name, e.first_name, e.middle_name, ep.position_name
            FROM employees e
            JOIN documents_employees de ON e.id = de.employee_id
            JOIN employee_positions ep ON e.position_id = ep.id
            WHERE de.document_id = ?
        """, documentId);

        while (rs.next()) {
            Employee e = new Employee();
            e.setId(rs.getInt("id"));
            e.setLastName(rs.getString("last_name"));
            e.setFirstName(rs.getString("first_name"));
            e.setMiddleName(rs.getString("middle_name"));
            e.setPosition(rs.getString("position_name"));

            list.add(e);
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
        SELECT
            e.id,
            e.photo,
            e.name,
            e.description,

            e.length,
            e.width,
            e.height,
            u_sizes.unit_name AS unit_sizes,

            e.weight,
            u_weight.unit_name AS unit_weight,

            e.color,
            e.material,
            e.dating_material,
            e.technique,

            ec.condition_name AS condition_name,
            e.condition_details,

            e.source,
            e.arrival_date,
            e.inscriptions,
            e.place_of_production,
            e.production_time,
            e.publication,
            e.usage,
            e.museum_value,

            es.status_name,
            f.fund_name,
            c.collection_name,

            e.number_kp,

            o.id AS owner_id,

            e.location

        FROM exhibits e

        JOIN documents_exhibits de ON e.id = de.exhibit_id

        LEFT JOIN exhibit_statuses es ON e.status_id = es.id
        LEFT JOIN funds f ON e.fund_id = f.id
        LEFT JOIN collections c ON e.collection_id = c.id
        LEFT JOIN units u_sizes ON e.unit_sizes_id = u_sizes.id
        LEFT JOIN units u_weight ON e.unit_weight_id = u_weight.id
        LEFT JOIN exhibit_conditions ec ON e.condition_id = ec.id
        LEFT JOIN owners o ON e.owner_id = o.id

        WHERE de.document_id = ?
    """, documentId);

        while (rs.next()) {

            Exhibit ex = new Exhibit();

            ex.setId(rs.getInt("id"));
            ex.setPhoto(rs.getString("photo"));

            ex.setName(rs.getString("name"));
            ex.setDescription(rs.getString("description"));

            ex.setLength(rs.getObject("length") != null ? rs.getDouble("length") : null);
            ex.setWidth(rs.getObject("width") != null ? rs.getDouble("width") : null);
            ex.setHeight(rs.getObject("height") != null ? rs.getDouble("height") : null);
            ex.setUnitSizes(rs.getString("unit_sizes"));

            ex.setWeight(rs.getObject("weight") != null ? rs.getDouble("weight") : null);
            ex.setUnitWeight(rs.getString("unit_weight"));

            ex.setColor(rs.getString("color"));
            ex.setMaterial(rs.getString("material"));
            ex.setDatingMaterial(rs.getString("dating_material"));
            ex.setTechnique(rs.getString("technique"));

            ex.setCondition(rs.getString("condition_name"));
            ex.setConditionDetails(rs.getString("condition_details"));

            ex.setSource(rs.getString("source"));

            java.sql.Date date = rs.getDate("arrival_date");
            if (date != null) {
                ex.setArrivalDate(date.toLocalDate());
            }

            ex.setInscriptions(rs.getString("inscriptions"));
            ex.setPlaceOfProduction(rs.getString("place_of_production"));
            ex.setProductionTime(rs.getString("production_time"));
            ex.setPublication(rs.getString("publication"));
            ex.setUsage(rs.getString("usage"));
            ex.setMuseumValue(rs.getString("museum_value"));

            ex.setStatus(rs.getString("status_name"));

            ex.setFund(rs.getString("fund_name"));
            ex.setCollection(rs.getString("collection_name"));

            ex.setNumberKP(rs.getString("number_kp"));

            ex.setOwnerId(rs.getInt("owner_id"));

            ex.setLocation(rs.getString("location"));

            list.add(ex);
        }

        return list;
    }


    public static void saveAll(int documentId,
                               List<Employee> employees,
                               List<Exhibit> exhibits) throws SQLException {

        addEmployees(documentId, employees);
        addExhibits(documentId, exhibits);
    }


    public static void loadAllRelations(int documentId,
                                        List<Employee> employeesTarget,
                                        List<Exhibit> exhibitsTarget) throws SQLException {

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