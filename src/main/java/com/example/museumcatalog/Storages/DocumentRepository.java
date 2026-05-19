package com.example.museumcatalog.Storages;

import com.example.museumcatalog.DBHandler;
import com.example.museumcatalog.Models.*;
import com.example.museumcatalog.SecurityUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.crypto.SecretKey;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class DocumentRepository {
    private static final SecretKey KEY = SecurityUtil.loadKeyFromEnv("MUSEUM_KEY");

    private static final ObservableList<Document> documents = FXCollections.observableArrayList();

    public static ObservableList<Document> getDocuments() {
        return documents;
    }

    public static void loadAll() throws SQLException {
        documents.clear();
        String query = """
                SELECT
                    d.id,
                    d.document_number,
                    dt.type_name AS doc_type,
                    d.creation_date AS doc_date,
                    o.last_name,
                    o.first_name,
                    o.middle_name,
                    d.owner_id,
                    (SELECT COUNT(*) FROM documents_exhibits de WHERE de.document_id = d.id) AS exhibits_count,
                    e.last_name || ' ' || LEFT(e.first_name,1) || '.' || LEFT(e.middle_name,1) || '.' AS created_by,
                    e2.last_name || ' ' || LEFT(e2.first_name,1) || '.' || LEFT(e2.middle_name,1) || '.' AS updated_by,
                    ds.status_name,
                    d.change_date,
                    d.conducted_date  AS conducted_at,
                    d.base_id,
                    b.document_number AS base_doc_number
                FROM documents d
                LEFT JOIN document_types dt ON d.type_id = dt.id
                LEFT JOIN document_statuses ds ON d.status_id = ds.id
                LEFT JOIN owners o ON d.owner_id = o.id
                LEFT JOIN users u ON d.creator_id = u.id
                LEFT JOIN employees e ON u.employee_id = e.id
                LEFT JOIN users u2 ON d.updater_id = u2.id
                LEFT JOIN employees e2 ON u2.employee_id = e2.id
                LEFT JOIN documents b ON d.base_id = b.id
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

            java.sql.Timestamp docDateTs = rs.getTimestamp("doc_date");
            java.sql.Timestamp changeTs = rs.getTimestamp("change_date");
            java.sql.Timestamp conductedTs = rs.getTimestamp("conducted_at");
            Integer ownerId = rs.getInt("owner_id");
            if (rs.wasNull()) ownerId = null;
            documents.add(new Document(
                    rs.getInt("id"),
                    rs.getString("document_number"),
                    rs.getString("doc_type"),
                    docDateTs != null ? docDateTs.toLocalDateTime() : null,
                    ownerFio,
                    ownerId,
                    rs.getInt("exhibits_count"),
                    rs.getString("status_name"),
                    changeTs != null ? changeTs.toLocalDateTime() : null,
                    conductedTs != null ? conductedTs.toLocalDateTime() : null,
                    rs.getString("created_by"),
                    rs.getString("updated_by"),
                    rs.getString("base_doc_number")
            ));
        }
    }

    public static int addOrEdit(Document doc, User us) throws SQLException {
        boolean isUpdate = doc.getId() != null;
        int result;

        String insertQuery = """
                INSERT INTO documents
                (
                    document_number,
                    type_id,
                    creation_date,
                    change_date,
                    conducted_date,
                    owner_id,
                    status_id,
                    creator_id,
                    updater_id,
                    base_id
                )
                VALUES (
                    ?,
                    (SELECT id FROM document_types WHERE type_name = ?),
                    ?,
                    ?,
                    ?,
                    ?,
                    (SELECT id FROM document_statuses WHERE status_name = ?),
                    ?,
                    ?,
                    ?
                )
                RETURNING id
                """;

        String updateQuery = """
                UPDATE documents SET
                    document_number = ?,
                    type_id = (SELECT id FROM document_types WHERE type_name = ?),
                    change_date = ?,
                    conducted_date = ?,
                    owner_id = ?,
                    status_id = (SELECT id FROM document_statuses WHERE status_name = ?),
                    updater_id = ?,
                    base_id = ?
                WHERE id = ?
                """;

        if (!isUpdate) {
            result = (int) DBHandler.executeReturning(
                    insertQuery,
                    "id",
                    doc.getDocNumber(),
                    doc.getDocType(),
                    doc.getDocDate(),
                    doc.getChangeDate(),
                    doc.getConductedDate() != null ? doc.getConductedDate() : null,
                    doc.getOwnerId() != null ? doc.getOwnerId() : null,
                    doc.getDocStatus(),

                    us != null ? us.getId() : null,
                    us != null ? us.getId() : null,

                    null
            );

        } else {
            result = DBHandler.executeUpdate(
                    updateQuery,
                    doc.getDocNumber(),
                    doc.getDocType(),
                    doc.getChangeDate(),
                    doc.getConductedDate() != null ? doc.getConductedDate() : null,
                    doc.getOwnerId() != null ? doc.getOwnerId() : null,
                    doc.getDocStatus(),
                    us != null ? us.getId() : null,
                    null,
                    doc.getId()
            );
        }

        return result;
    }

    public static boolean deleteDocument(int id) throws SQLException {
        String query = "DELETE FROM documents WHERE id = ?";
        return DBHandler.executeUpdate(query, id) > 0;
    }

    public static String generateDocNumber(String docTypeCode) {
        int year = LocalDateTime.now().getYear();
        String sql = """ 
                INSERT INTO document_counters (doc_type_code, year, last_value) 
                VALUES (?, ?, 1) 
                ON CONFLICT (doc_type_code, year) 
                DO UPDATE SET last_value = document_counters.last_value + 1 
                RETURNING last_value 
                """;
        try {
            long seq = (long) DBHandler.executeReturning(sql, "last_value", docTypeCode, year);
            return String.format("%s-%d-%04d", docTypeCode, year, seq);
        } catch (Exception e) {
            e.printStackTrace();
            return String.format("%s-%d-%04d", docTypeCode, year, 1);
        }
    }

    public static boolean hasConductedDocumentForExhibit(int exhibitId, String docType) throws SQLException {

        String query = """
                    SELECT 1
                    FROM documents_exhibits de
                    JOIN documents d ON d.id = de.document_id
                    join document_types dt on dt.id = d.type_id
                    WHERE de.exhibit_id = ?
                    AND dt.type_name = ?
                    AND d.conducted_date IS NOT NULL
                    LIMIT 1
                """;

        ResultSet rs = DBHandler.executeQuery(query, exhibitId, docType);
        return rs.next();
    }
}
