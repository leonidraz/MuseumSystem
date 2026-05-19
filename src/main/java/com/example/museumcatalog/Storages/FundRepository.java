package com.example.museumcatalog.Storages;

import com.example.museumcatalog.DBHandler;
import com.example.museumcatalog.Models.Fund;
import com.example.museumcatalog.Models.Owner;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FundRepository {
    private static final ObservableList<Fund> funds = FXCollections.observableArrayList();
    public static ObservableList<Fund> getFunds() {
        return funds;
    }

    public static void loadAll() throws SQLException {
        funds.clear();
        String query = """
                SELECT id, fund_name, fund_description, kp_prefix
                FROM funds
                """;

        ResultSet rs = DBHandler.executeQuery(query);

        while (rs.next()) {
            funds.add(new Fund(
                    rs.getInt("id"),
                    rs.getString("fund_name"),
                    rs.getString("fund_description"),
                    rs.getString("kp_prefix")));
        }
    }

    public static int addOrUpdate(Fund fund) throws SQLException {
        boolean isUpdate = fund.getId() != 0;
        int result = 0;

        if (!isUpdate) {
            String query = """
            INSERT INTO funds (
                fund_name, fund_description, kp_prefix
            )
            VALUES (?, ?, ?)
            RETURNING id
        """;

            result = (int) DBHandler.executeReturning(query, "id",
                    fund.getFundName(),
                    fund.getFundDescription(),
                    fund.getKpPrefix()
            );
        } else {
            String query = """
            UPDATE funds SET
                fund_name = ?,
                fund_description = ?,
                kp_prefix = ?
            WHERE id = ?
        """;

            result = DBHandler.executeUpdate(query,
                    fund.getFundName(),
                    fund.getFundDescription(),
                    fund.getKpPrefix(),
                    fund.getId()
            );
        }
        return result;
    }

    public static boolean deleteFund(int id) throws SQLException {
        String query = "DELETE FROM funds WHERE id = ?";
        return DBHandler.executeUpdate(query, id) > 0;
    }

}
