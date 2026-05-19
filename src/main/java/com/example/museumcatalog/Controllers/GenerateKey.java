package com.example.museumcatalog.Controllers;

import com.example.museumcatalog.DBHandler;
import com.example.museumcatalog.SecurityUtil;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.sql.ResultSet;
import java.util.Base64;

//public class GenerateKey {
//
//    public static void main(String[] args) throws Exception {
//        DBHandler.setConnection();
//
//        SecretKey key = SecurityUtil.loadKeyFromEnv("MUSEUM_KEY");
//
//        String select = """
//            SELECT id, last_name, first_name, middle_name,
//                   passport_series, passport_number,
//                   issued_by, date_of_issue,
//                   phone, address, notice
//            FROM owners
//            """;
//
//        ResultSet rs = DBHandler.executeQuery(select);
//
//        while (rs.next()) {
//
//            int id = rs.getInt("id");
//
//            String update = """
//                UPDATE owners SET
//                    last_name = ?,
//                    first_name = ?,
//                    middle_name = ?,
//                    passport_series = ?,
//                    passport_number = ?,
//                    issued_by = ?,
//                    date_of_issue = ?,
//                    phone = ?,
//                    address = ?,
//                    notice = ?
//                WHERE id = ?
//                """;
//
//            DBHandler.executeUpdate(update,
//
//                    SecurityUtil.encryptSafe(rs.getString("last_name"), key),
//                    SecurityUtil.encryptSafe(rs.getString("first_name"), key),
//                    SecurityUtil.encryptSafe(rs.getString("middle_name"), key),
//
//                    SecurityUtil.encryptSafe(rs.getString("passport_series"), key),
//                    SecurityUtil.encryptSafe(rs.getString("passport_number"), key),
//
//                    SecurityUtil.encryptSafe(rs.getString("issued_by"), key),
//
//                    SecurityUtil.encryptSafe(rs.getString("date_of_issue"), key),
//
//                    SecurityUtil.encryptSafe(rs.getString("phone"), key),
//
//                    SecurityUtil.encryptSafe(rs.getString("address"), key),
//
//                    SecurityUtil.encryptSafe(rs.getString("notice"), key),
//
//                    id
//            );
//        }
//
//        System.out.println("Все данные зашифрованы");
//    }
//}