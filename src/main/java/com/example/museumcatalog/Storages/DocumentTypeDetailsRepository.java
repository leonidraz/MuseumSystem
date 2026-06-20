package com.example.museumcatalog.Storages;

import com.example.museumcatalog.DBHandler;
import com.example.museumcatalog.Models.EfzkData;
import com.example.museumcatalog.Models.InternalTransferData;
import com.example.museumcatalog.Models.TemporaryStorageData;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DocumentTypeDetailsRepository {
    public static void saveEfzk(int documentId, LocalDate startDate, LocalDate endDate, String fundName, String collectionName) throws SQLException {
        String sql = """
            INSERT INTO protocol_efzk (
                document_id,
                start_date,
                end_date,
                fund_id,
                collection_id
            )
            VALUES (
                ?,
                ?,
                ?,
                (SELECT id FROM funds WHERE fund_name = ?),
                (SELECT id FROM collections WHERE collection_name = ? AND fund_id = (SELECT id FROM funds WHERE fund_name = ?))
            )
        """;

        DBHandler.executeUpdate(sql,
                documentId,
                startDate,
                endDate,
                fundName,
                collectionName,
                fundName
        );
    }

    public static List<EfzkData> getEfzk(int documentId) throws SQLException {
        List<EfzkData> list = new ArrayList<>();
        String sql = """
            SELECT p.start_date, p.end_date, f.fund_name, c.collection_name
            FROM protocol_efzk p
            JOIN funds f ON p.fund_id = f.id
            JOIN collections c ON p.collection_id = c.id
            WHERE p.document_id = ?
        """;

        try (ResultSet rs = DBHandler.executeQuery(sql, documentId)) {
            while (rs.next()) {
                list.add(new EfzkData(
                        rs.getDate("start_date").toLocalDate(),
                        rs.getDate("end_date").toLocalDate(),
                        rs.getString("fund_name"),
                        rs.getString("collection_name")
                ));
            }
        }
        return list;
    }



    public static void saveInternalTransfer(int documentId, int fromEmployeeId, int toEmployeeId, String purpose) throws SQLException {
        String sql = """
            INSERT INTO internal_transfer_act (
                document_id,
                from_employee_id,
                to_employee_id,
                transfer_purpose
            )
            VALUES (?, ?, ?, ?)
        """;

        DBHandler.executeUpdate(sql,
                documentId,
                fromEmployeeId,
                toEmployeeId,
                purpose
        );
    }

    public static List<InternalTransferData> getInternalTransfer(int documentId) throws SQLException {
        List<InternalTransferData> list = new ArrayList<>();
        String sql = """
                select
                ita.from_employee_id,
                ita.to_employee_id,
                ita.transfer_purpose
                FROM internal_transfer_act ita
                WHERE document_id = ?
                """;

        try (ResultSet rs = DBHandler.executeQuery(sql, documentId)) {
            while (rs.next()) {
                list.add(new InternalTransferData(
                        rs.getInt("from_employee_id"),
                        rs.getInt("to_employee_id"),
                        rs.getString("transfer_purpose")
                ));
            }
        }
        return list;
    }

    public static void saveTemporaryStorage(int documentId, String receiverType, String receiverName, String receiverIdentifier, String receiverAddress, String purpose) throws SQLException {
        String sql = """
            INSERT INTO temporary_storage_act (
                document_id,
                receiver_type_id,
                receiver_name,
                receiver_identifier,
                receiver_address,
                admission_purpose
            )
            VALUES (
                ?,
                (SELECT id FROM receiver_types WHERE type_name = ?),
                ?,
                ?,
                ?,
                ?
            )
        """;

        DBHandler.executeUpdate(sql,
                documentId,
                receiverType,
                receiverName,
                receiverIdentifier,
                receiverAddress,
                purpose
        );
    }

    public static List<TemporaryStorageData> getTemporaryStorage(int documentId) throws SQLException {
        List<TemporaryStorageData> list = new ArrayList<>();
        String sql = """
                SELECT rt.type_name, tsa.receiver_name, tsa.receiver_identifier, tsa.receiver_address, tsa.admission_purpose
                FROM temporary_storage_act tsa
                inner JOIN receiver_types rt ON tsa.receiver_type_id = rt.id
                WHERE tsa.document_id = ?
                """;
        try (ResultSet rs = DBHandler.executeQuery(sql, documentId)) {
            while (rs.next()) {
                list.add(new TemporaryStorageData(
                        rs.getString("type_name"),
                        rs.getString("receiver_name"),
                        rs.getString("receiver_identifier"),
                        rs.getString("receiver_address"),
                        rs.getString("admission_purpose")
                ));
            }
        }
        return list;
    }

    public static void clearRelations(int documentId) throws SQLException {
        DBHandler.executeUpdate("DELETE FROM protocol_efzk WHERE document_id = ?", documentId);
        DBHandler.executeUpdate("DELETE FROM internal_transfer_act WHERE document_id = ?", documentId);
        DBHandler.executeUpdate("DELETE FROM temporary_storage_act WHERE document_id = ?", documentId);
    }

    public static EfzkData getLastEfzkForExhibit(int exhibitId) throws SQLException {
        String query = """
                SELECT pe.*, f.fund_name, c.collection_name
                        FROM protocol_efzk pe
                        JOIN documents d ON d.id = pe.document_id
                        JOIN documents_exhibits de ON d.id = de.document_id\s
                        JOIN document_statuses ds ON d.status_id = ds.id
                        JOIN document_types dt ON d.type_id = dt.id
                        JOIN funds f ON f.id = pe.fund_id
                        JOIN collections c ON c.id = pe.collection_id
                        WHERE de.exhibit_id = ? AND dt.type_name = 'Протокол заседания ЭФЗК' AND ds.status_name = 'Проведен'
                        ORDER BY d.conducted_date DESC
                        LIMIT 1
        """;
        try (ResultSet rs = DBHandler.executeQuery(query, exhibitId)) {
            while (rs.next()) {
                return new EfzkData(
                        rs.getDate("start_date").toLocalDate(),
                        rs.getDate("end_date").toLocalDate(),
                        rs.getString("fund_name"),
                        rs.getString("collection_name")
                );
            }
        }
        return null;
    }
}