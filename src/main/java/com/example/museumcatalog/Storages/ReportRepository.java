package com.example.museumcatalog.Storages;

import com.example.museumcatalog.DBHandler;
import com.example.museumcatalog.Models.ReportRow;
import com.example.museumcatalog.SecurityUtil;

import javax.crypto.SecretKey;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReportRepository {

    private static final SecretKey KEY = SecurityUtil.loadKeyFromEnv("MUSEUM_KEY");

    public static List<ReportRow> getMovementReport(List<Integer> exhibitIds) throws Exception {

        String sql = """
            SELECT e.id, e.number_kp, e.name, e.description,
                   d.conducted_date, dt.type_name, d.document_number,
                   o.last_name, o.first_name, o.middle_name,
                   d.id AS doc_id
            FROM exhibits e
            LEFT JOIN documents_exhibits de ON e.id = de.exhibit_id
            LEFT JOIN documents d ON d.id = de.document_id AND d.conducted_date IS NOT NULL
            LEFT JOIN document_types dt ON d.type_id = dt.id
            LEFT JOIN owners o ON o.id = e.owner_id
            WHERE e.id = ?
            ORDER BY e.id, d.conducted_date
        """;

        List<ReportRow> result = new ArrayList<>();

        for (Integer id : exhibitIds) {

            ResultSet rs = DBHandler.executeQuery(sql, id);
            boolean hasDocs = false;

            while (rs.next()) {

                String kp = rs.getString("number_kp");
                String name = rs.getString("name");
                String description = rs.getString("description");

                ReportRow r = new ReportRow();
                r.setKp(kp);
                r.setName(name);
                r.setDescription(description != null ? description : "");

                if (rs.getObject("doc_id") == null) {
                    if (!hasDocs) {
                        r.setDocument("нет сформированных документов");
                        result.add(r);
                        hasDocs = true;
                    }
                    continue;
                }

                LocalDateTime date = rs.getTimestamp("conducted_date").toLocalDateTime();
                r.setDate(date);

                String docType = rs.getString("type_name");
                String docNumber = rs.getString("document_number");
                r.setDocument(docType + " №" + docNumber);
                r.setStage(resolveStage(docType));

                r.setOwner(buildOwnerFio(
                        rs.getString("last_name"),
                        rs.getString("first_name"),
                        rs.getString("middle_name")
                ));

                result.add(r);
                hasDocs = true;
            }

            ReportRow separator = new ReportRow();
            separator.setKp(" ");
            separator.setName(" ");
            separator.setDescription(" ");
            result.add(separator);
        }

        return result;
    }

    public static List<ReportRow> getIncomingReport() throws Exception {

        String sql = """
            SELECT e.number_kp, e.name, e.description,
                   d.conducted_date,
                   o.last_name, o.first_name, o.middle_name
            FROM documents_exhibits de
            JOIN exhibits e ON e.id = de.exhibit_id
            JOIN documents d ON d.id = de.document_id
            JOIN document_types dt ON d.type_id = dt.id
            LEFT JOIN owners o ON o.id = e.owner_id
            WHERE dt.type_name = 'Акт ПП на ВХ'
              AND d.conducted_date IS NOT NULL
        """;

        ResultSet rs = DBHandler.executeQuery(sql);

        List<ReportRow> result = new ArrayList<>();

        while (rs.next()) {

            ReportRow r = new ReportRow();

            r.setKp(rs.getString("number_kp"));
            r.setName(rs.getString("name"));
            r.setDescription(rs.getString("description"));

            r.setDate(rs.getTimestamp("conducted_date").toLocalDateTime());

            r.setOwner(buildOwnerFio(
                    rs.getString("last_name"),
                    rs.getString("first_name"),
                    rs.getString("middle_name")
            ));

            result.add(r);
        }

        result.sort((a, b) -> {
            String kpA = safe(a.getKp());
            String kpB = safe(b.getKp());

            int cmp = kpA.compareTo(kpB);
            return cmp != 0 ? cmp : a.getDate().compareTo(b.getDate());
        });

        return result;
    }

    private static String buildOwnerFio(String last, String first, String middle) {
        return ((last == null ? "" : last) + " " +
                (first == null ? "" : first) + " " +
                (middle == null ? "" : middle)).trim();
    }

    private static String resolveStage(String docType) {
        return switch (docType) {
            case "Акт ПП на ВХ" -> "Принят на временное хранение";
            case "Акт на рассмотрение ЭФЗК" -> "Передан на рассмотрение ЭФЗК";
            case "Протокол заседания ЭФЗК" -> "Рассмотрен комиссией";
            case "Акт ПП на ПП" -> "Принят на постоянное хранение";
            case "Акт ПП на ОХ" -> "Передан на ответственное хранение";
            case "Акт внутримузейной передачи" -> "Внутримузейная передача";
            case "Акт ВП на временное хранение" -> "Выдан организации или частному лицу";
            default -> "";
        };
    }

    private static String safe(String s) {
        return (s == null || s.isBlank()) ? "—" : s;
    }
}