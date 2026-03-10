package com.example.museumcatalog.Controllers;

import com.example.museumcatalog.DBHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class authorizationFormController {
    DBHandler dbHandler = new DBHandler();

    public void initialize() throws ClassNotFoundException {
        dbHandler.setConnection();
    }
}