package com.example.museumcatalog.Controllers;

import com.example.museumcatalog.MainApplication;
import com.example.museumcatalog.Service;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.util.Objects;

public class BaseFormController {
    @FXML
    private Button btn;

    @FXML
    private Label documentsLabel;

    @FXML
    private Label employeesLabel;

    @FXML
    private Label exhibitsLabel;

    @FXML
    private Button exitLabel;

    @FXML
    private Label fioLabel;

    @FXML
    private Label guidesLabel;

    @FXML
    private Label mainLabel;

    @FXML
    private Label ownersLabel;

    @FXML
    private Label roleLabel;

    @FXML
    private AnchorPane contentContainer;

    private Label activeLabel;

    Service service = new Service();

    public void initialize() throws IOException {
        activeLabel = mainLabel;
        mainLabel.getStyleClass().add("nav-item-active");
        switchContent("MainForm");
        exitLabel.setOnAction(actionEvent -> {
            try {
                Service.setCurrentUser(null);
                service.switchScene("АuthorizationForm", "Авторизация");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        mainLabel.setOnMouseClicked(e -> {
            setActive(mainLabel);
            try {
                switchContent("MainForm");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        exhibitsLabel.setOnMouseClicked(e -> {
            setActive(exhibitsLabel);
            try {
                switchContent("ExhibitsListForm");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        documentsLabel.setOnMouseClicked(e -> {
            setActive(documentsLabel);
            try {
                switchContent("DocumentsListForm");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        guidesLabel.setOnMouseClicked(e -> {
            setActive(guidesLabel);
        });

        employeesLabel.setOnMouseClicked(e -> {
            setActive(employeesLabel);
        });

        ownersLabel.setOnMouseClicked(e -> {
            setActive(ownersLabel);
        });

        if (Service.getCurrentUser() != null) {
            fioLabel.setText(Service.getCurrentUser().getLastName() + " " +
                    Service.getCurrentUser().getFirstName().charAt(0) + "." +
                    Service.getCurrentUser().getMiddleName().charAt(0) + ".");
            roleLabel.setText(Service.getCurrentUser().getRole());
        }
    }

    private void setActive(Label clickedLabel) {
        if (activeLabel != null) {
            activeLabel.getStyleClass().remove("nav-item-active");
        }

        if (!clickedLabel.getStyleClass().contains("nav-item-active")) {
            clickedLabel.getStyleClass().add("nav-item-active");
        }

        activeLabel = clickedLabel;
    }

    public void switchContent(String fileName) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("fxml/" + fileName + ".fxml"));
        Node node = fxmlLoader.load();
        contentContainer.getChildren().setAll(node);
    }
}
