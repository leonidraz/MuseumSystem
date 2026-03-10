module com.example.museumcatalog {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.postgresql.jdbc;


    opens com.example.museumcatalog to javafx.fxml;
    exports com.example.museumcatalog;

    exports com.example.museumcatalog.Controllers;
    opens com.example.museumcatalog.Controllers to javafx.fxml;
}