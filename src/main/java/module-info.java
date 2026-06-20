module com.example.museumcatalog {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.postgresql.jdbc;
    requires java.desktop;
    requires jbcrypt;
    requires org.docx4j.core;
    requires jakarta.xml.bind;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;


    opens com.example.museumcatalog to javafx.fxml;
    exports com.example.museumcatalog;

    exports com.example.museumcatalog.Controllers;
    opens com.example.museumcatalog.Models to javafx.base, javafx.fxml;
    opens com.example.museumcatalog.Controllers to javafx.fxml;
}