package com.example.museumcatalog.Storages;

import com.example.museumcatalog.DBHandler;
import com.example.museumcatalog.Models.Exhibit;
import com.example.museumcatalog.SecurityUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.crypto.SecretKey;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ExhibitRepository {

    private static final SecretKey KEY = SecurityUtil.loadKeyFromEnv("MUSEUM_KEY");
    private static final ObservableList<Exhibit> exhibits = FXCollections.observableArrayList();
    public static ObservableList<Exhibit> getExhibits() {
        return exhibits;
    }

    public static void loadAll() throws SQLException {
        exhibits.clear();
        String query = """
                SELECT e.id,
                       e.photo,
                       e."name",
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
                       ec.condition_name,
                       e."source",
                       e.arrival_date,
                       e.inscriptions,
                       e.place_of_production,
                       e.production_time,
                       e."publication",
                       e."usage",
                       e.museum_value,
                       es.status_name,
                       f.fund_name,
                       c.collection_name,
                       e.number_kp,
                       o.last_name,
                       o.first_name,
                       o.middle_name,
                       o.id AS owner_id,
                       e.location,
                       e.condition_details
                FROM exhibits e
                LEFT JOIN exhibit_statuses es ON e.status_id = es.id
                LEFT JOIN funds f ON e.fund_id = f.id
                LEFT JOIN collections c ON e.collection_id = c.id
                LEFT JOIN units u_sizes ON e.unit_sizes_id = u_sizes.id
                LEFT JOIN units u_weight ON e.unit_weight_id = u_weight.id
                LEFT JOIN exhibit_conditions ec ON e.condition_id = ec.id
                LEFT JOIN owners o ON e.owner_id = o.id
                """;

        ResultSet rs = DBHandler.executeQuery(query);

        while (rs.next()) {
            String lastName = SecurityUtil.decryptSafe(rs.getString("last_name"), KEY);
            String firstName = SecurityUtil.decryptSafe(rs.getString("first_name"), KEY);
            String middleName = SecurityUtil.decryptSafe(rs.getString("middle_name"), KEY);

            String ownerFio = String.join(" ",
                    lastName != null ? lastName : "",
                    firstName != null ? firstName : "",
                    middleName != null ? middleName : ""
            ).trim();

            Double weight = rs.getDouble("weight");
            if (rs.wasNull()) weight = null;
            exhibits.add(new Exhibit(
                    rs.getInt("id"),
                    rs.getString("photo"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDouble("length"),
                    rs.getDouble("width"),
                    rs.getDouble("height"),
                    rs.getString("unit_sizes"),
                    weight,
                    rs.getString("unit_weight"),
                    rs.getString("color"),
                    rs.getString("material"),
                    rs.getString("dating_material"),
                    rs.getString("technique"),
                    rs.getString("condition_name"),
                    rs.getString("source"),
                    rs.getDate("arrival_date").toLocalDate(),
                    rs.getString("inscriptions"),
                    rs.getString("place_of_production"),
                    rs.getString("production_time"),
                    rs.getString("publication"),
                    rs.getString("usage"),
                    rs.getString("museum_value"),
                    rs.getString("status_name"),
                    rs.getString("fund_name"),
                    rs.getString("collection_name"),
                    rs.getString("number_kp"),
                    ownerFio,
                    rs.getInt("owner_id"),
                    rs.getString("location"),
                    rs.getString("condition_details")));
        }
    }

    public static int addOrEdit(Exhibit exhibit) throws SQLException {
        boolean isUpdate = exhibit.getIdValue() != null;
        int result = 0;

        String insertQuery = """
        INSERT INTO public.exhibits
        (photo, "name", description, length, width, height, weight, color, material,
         dating_material, technique, condition_id, "source", arrival_date, inscriptions,
         place_of_production, production_time, "publication", "usage", museum_value,
         status_id, owner_id, unit_sizes_id, unit_weight_id, location, condition_details)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
         (SELECT id FROM exhibit_conditions WHERE condition_name = ?),
         ?, ?, ?, ?, ?, ?, ?, ?,
         (SELECT id FROM exhibit_statuses WHERE status_name = ?),
         ?,
         (SELECT id FROM units WHERE unit_name = ?),
         (SELECT id FROM units WHERE unit_name = ?), ?, ?)
         RETURNING id
    """;

        String updateQuery = """
        UPDATE public.exhibits SET
            photo = ?, "name" = ?, description = ?, length = ?, width = ?, height = ?,
            weight = ?, color = ?, material = ?, dating_material = ?, technique = ?,
            condition_id = (SELECT id FROM exhibit_conditions WHERE condition_name = ?),
            "source" = ?, arrival_date = ?, inscriptions = ?, place_of_production = ?,
            production_time = ?, "publication" = ?, "usage" = ?, museum_value = ?,
            status_id = (SELECT id FROM exhibit_statuses WHERE status_name = ?),
            owner_id = ?, unit_sizes_id = (SELECT id FROM units WHERE unit_name = ?),
            unit_weight_id = (SELECT id FROM units WHERE unit_name = ?), location = ?, condition_details = ?
        WHERE id = ?
    """;

        if (!isUpdate) {
            result = (int) DBHandler.executeReturning(insertQuery, "id",
                    exhibit.getPhoto(), exhibit.getName(), exhibit.getDescription(),
                    exhibit.getLength(), exhibit.getWidth(), exhibit.getHeight(),
                    exhibit.getWeight(), exhibit.getColor(), exhibit.getMaterial(),
                    exhibit.getDatingMaterial(), exhibit.getTechnique(), exhibit.getCondition(),
                    exhibit.getSource(), exhibit.getArrivalDate(), exhibit.getInscriptions(),
                    exhibit.getPlaceOfProduction(), exhibit.getProductionTime(),
                    exhibit.getPublication(), exhibit.getUsage(), exhibit.getMuseumValue(),
                    "В обработке", exhibit.getOwnerId(), exhibit.getUnitSizes(), exhibit.getUnitWeight(), exhibit.getLocation(),
                    exhibit.getConditionDetails()
            );

        } else {
            result = DBHandler.executeUpdate(updateQuery,
                    exhibit.getPhoto(), exhibit.getName(), exhibit.getDescription(),
                    exhibit.getLength(), exhibit.getWidth(), exhibit.getHeight(),
                    exhibit.getWeight(), exhibit.getColor(), exhibit.getMaterial(),
                    exhibit.getDatingMaterial(), exhibit.getTechnique(), exhibit.getCondition(),
                    exhibit.getSource(), exhibit.getArrivalDate(), exhibit.getInscriptions(),
                    exhibit.getPlaceOfProduction(), exhibit.getProductionTime(),
                    exhibit.getPublication(), exhibit.getUsage(), exhibit.getMuseumValue(),
                    exhibit.getStatus(), exhibit.getOwnerId(), exhibit.getUnitSizes(),
                    exhibit.getUnitWeight(), exhibit.getLocation(),
                    exhibit.getConditionDetails(), exhibit.getId()
            );
        }

        return result;
    }
    public static int delete(int id) {
        String checkQuery = "SELECT COUNT(*) FROM documents_exhibits WHERE exhibit_id = ?";
        String deleteQuery = "DELETE FROM exhibits WHERE id = ?";

        try {
            ResultSet rs = DBHandler.executeQuery(checkQuery, id);
            if (rs.next() && rs.getInt(1) > 0) {
                return -2;
            }

            return DBHandler.executeUpdate(deleteQuery, id);

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static void archiveExhibit(int id) throws SQLException {
        String query = """
        UPDATE exhibits SET
            previous_status_id = status_id,
            status_id = (SELECT id FROM exhibit_statuses WHERE status_name = 'Архивирован')
        WHERE id = ?
    """;
        DBHandler.executeUpdate(query, id);
    }

    public static String unarchiveExhibit(int id) throws SQLException {
        String query = """
        UPDATE exhibits SET
            status_id = previous_status_id,
            previous_status_id = NULL
        WHERE id = ?
    """;

        DBHandler.executeUpdate(query, id);

        String queryStatus = """
        SELECT es.status_name
        FROM exhibits e
        LEFT JOIN exhibit_statuses es ON e.status_id = es.id
        WHERE e.id = ?
    """;

        ResultSet rs = DBHandler.executeQuery(queryStatus, id);
        if (rs.next()) {
            return rs.getString("status_name");
        }

        return null;
    }

    public static void updateStatus(int exhibitId, String statusName) throws SQLException {
        String sql = """
        UPDATE exhibits
        SET status_id = (
            SELECT id
            FROM exhibit_statuses
            WHERE status_name = ?
        )
        WHERE id = ?
    """;

        DBHandler.executeUpdate(sql, statusName, exhibitId);
    }

    public static String generateKPNumber(String fundName) {
        String prefix = "KP";

        try {
            String sqlFund = """
            SELECT kp_prefix
            FROM funds
            WHERE fund_name = ?
        """;

            ResultSet rs = DBHandler.executeQuery(sqlFund, fundName);

            if (rs.next()) {
                String dbPrefix = rs.getString("kp_prefix");
                if (dbPrefix != null && !dbPrefix.isEmpty()) {
                    prefix = dbPrefix;
                }
            }

            String sqlCounter = """
            INSERT INTO kp_counters(prefix, last_value)
            VALUES (?, 1)
            ON CONFLICT(prefix)
            DO UPDATE SET last_value = kp_counters.last_value + 1
            RETURNING last_value
        """;

            ResultSet rs2 = DBHandler.executeQuery(sqlCounter, prefix);

            long seq = 1;
            if (rs2.next()) {
                seq = rs2.getLong("last_value");
            }

            return String.format("%s%04d", prefix, seq);

        } catch (Exception e) {
            e.printStackTrace();
            return String.format("KP%04d", 1);
        }
    }

    public static void updateFundData(
            int exhibitId,
            String fund,
            String collection,
            String kpNumber
    ) throws SQLException {

        String sql = """
        UPDATE exhibits
        SET fund_id = (
                SELECT f.id
                FROM funds f
                WHERE f.fund_name = ?
        ),
            collection_id = (
                SELECT c.id
                FROM collections c
                JOIN funds f ON f.id = c.fund_id
                WHERE c.collection_name = ?
                  AND f.fund_name = ?
        ),
            number_kp = ?
        WHERE id = ?
        """;
        DBHandler.executeUpdate(sql, fund, collection, fund, kpNumber, exhibitId);
    }
}