package com.example.museumcatalog.Storages;

import com.example.museumcatalog.DBHandler;
import com.example.museumcatalog.Models.Collection;
import com.example.museumcatalog.Models.Fund;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CollectionRepository {
    private static final ObservableList<Collection> collections = FXCollections.observableArrayList();
    public static ObservableList<Collection> getCollections() {
        return collections;
    }

    public static void loadAll() throws SQLException {
        collections.clear();
        String query = """
                SELECT c.id, c.collection_name, f.fund_name, c.collection_description
                FROM collections c
                inner join funds f on c.fund_id = f.id
                """;

        ResultSet rs = DBHandler.executeQuery(query);

        while (rs.next()) {
            collections.add(new Collection(
                    rs.getInt("id"),
                    rs.getString("collection_name"),
                    rs.getString("fund_name"),
                    rs.getString("collection_description")));
        }
    }

    public static int addOrUpdate(Collection collection) throws SQLException {
        boolean isUpdate = collection.getId() != 0;
        int result = 0;

        if (!isUpdate) {
            String query = """
            INSERT INTO collections (
                collection_name, fund_id, collection_description
            )
            VALUES (
                ?,
                (SELECT id FROM funds WHERE fund_name = ?),
                ?
            )
            RETURNING id
        """;

            result = (int) DBHandler.executeReturning(query, "id",
                    collection.getCollectionName(),
                    collection.getFundName(),
                    collection.getCollectionDescription()
            );
        } else {
            String query = """
            UPDATE collections SET
                collection_name = ?,
                fund_id = (SELECT id FROM funds WHERE fund_name = ?),
                collection_description = ?
            WHERE id = ?
        """;

            result = DBHandler.executeUpdate(query,
                    collection.getCollectionName(),
                    collection.getFundName(),
                    collection.getCollectionDescription(),
                    collection.getId()
            );
        }
        return result;
    }

    public static boolean deleteCollection(int id) throws SQLException {
        String query = "DELETE FROM collections WHERE id = ?";
        return DBHandler.executeUpdate(query, id) > 0;
    }

}
