package com.example.museumcatalog.Storages;

import com.example.museumcatalog.DBHandler;
import com.example.museumcatalog.Models.Owner;
import com.example.museumcatalog.SecurityUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.crypto.SecretKey;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class OwnerRepository {

    private static final SecretKey KEY = SecurityUtil.loadKeyFromEnv("MUSEUM_KEY");

    private static final ObservableList<Owner> owners = FXCollections.observableArrayList();

    public static ObservableList<Owner> getOwners() {
        return owners;
    }

    public static void loadAll() throws SQLException {

        owners.clear();

        String query = """
                SELECT id, last_name, first_name, middle_name,
                       address, passport_series, passport_number,
                       issued_by, date_of_issue, phone, notice
                FROM owners
                ORDER BY last_name, first_name
                """;

        ResultSet rs = DBHandler.executeQuery(query);

        while (rs.next()) {

            owners.add(new Owner(
                    rs.getInt("id"),
                    SecurityUtil.decryptSafe(rs.getString("last_name"), KEY),
                    SecurityUtil.decryptSafe(rs.getString("first_name"), KEY),
                    SecurityUtil.decryptSafe(rs.getString("middle_name"), KEY),
                    SecurityUtil.decryptSafe(rs.getString("address"), KEY),
                    SecurityUtil.decryptSafe(rs.getString("passport_series"), KEY),
                    SecurityUtil.decryptSafe(rs.getString("passport_number"), KEY),
                    SecurityUtil.decryptSafe(rs.getString("issued_by"), KEY),
                    LocalDate.parse(SecurityUtil.decryptSafe(rs.getString("date_of_issue"), KEY)),
                    SecurityUtil.decryptSafe(rs.getString("phone"), KEY),
                    SecurityUtil.decryptSafe(rs.getString("notice"), KEY)
            ));
        }
    }

    public static int addOrUpdate(Owner owner) throws SQLException {

        boolean isUpdate = owner.getId() != 0;

        if (!isUpdate) {

            String query = """
                    INSERT INTO owners (
                        last_name, first_name, middle_name,
                        passport_series, passport_number,
                        issued_by, date_of_issue,
                        phone, address, notice
                    )
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    RETURNING id
                    """;

            return (int) DBHandler.executeReturning(query, "id",
                    SecurityUtil.encryptSafe(owner.getLastName(), KEY),
                    SecurityUtil.encryptSafe(owner.getFirstName(), KEY),
                    SecurityUtil.encryptSafe(owner.getMiddleName(), KEY),
                    SecurityUtil.encryptSafe(owner.getPassportSeries(), KEY),
                    SecurityUtil.encryptSafe(owner.getPassportNumber(), KEY),
                    SecurityUtil.encryptSafe(owner.getIssuedBy(), KEY),
                    SecurityUtil.encryptSafe(owner.getDateOfIssue().toString(), KEY),
                    SecurityUtil.encryptSafe(owner.getPhone(), KEY),
                    SecurityUtil.encryptSafe(owner.getAddress(), KEY),
                    SecurityUtil.encryptSafe(owner.getNotice(), KEY)
            );
        }

        String query = """
                UPDATE owners SET
                    last_name = ?,
                    first_name = ?,
                    middle_name = ?,
                    passport_series = ?,
                    passport_number = ?,
                    issued_by = ?,
                    date_of_issue = ?,
                    phone = ?,
                    address = ?,
                    notice = ?
                WHERE id = ?
                """;

        return DBHandler.executeUpdate(query,
                SecurityUtil.encryptSafe(owner.getLastName(), KEY),
                SecurityUtil.encryptSafe(owner.getFirstName(), KEY),
                SecurityUtil.encryptSafe(owner.getMiddleName(), KEY),
                SecurityUtil.encryptSafe(owner.getPassportSeries(), KEY),
                SecurityUtil.encryptSafe(owner.getPassportNumber(), KEY),
                SecurityUtil.encryptSafe(owner.getIssuedBy(), KEY),
                SecurityUtil.encryptSafe(owner.getDateOfIssue().toString(), KEY),
                SecurityUtil.encryptSafe(owner.getPhone(), KEY),
                SecurityUtil.encryptSafe(owner.getAddress(), KEY),
                SecurityUtil.encryptSafe(owner.getNotice(), KEY),
                owner.getId()
        );
    }

    public static boolean deleteOwner(int id) throws SQLException {
        String query = "DELETE FROM owners WHERE id = ?";
        return DBHandler.executeUpdate(query, id) > 0;
    }
}