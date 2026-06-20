package com.example.museumcatalog.Controllers;

import com.example.museumcatalog.MainApplication;
import com.example.museumcatalog.Models.User;
import com.example.museumcatalog.Service;
import com.example.museumcatalog.Storages.AuthLogRepository;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

public class BaseFormController {
    @FXML
    private Button btn;

    @FXML
    private Label documentsLabel;

    @FXML
    private Label employeesLabel;

    @FXML
    public Label exhibitsLabel;

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
    private Label usersLabel;

    @FXML
    private Label roleLabel;

    @FXML
    private AnchorPane contentContainer;

    private Label activeLabel;

    Service service = new Service();

    public void initialize() throws IOException {
        employeesLabel.setVisible(false);
        employeesLabel.setManaged(false);

        usersLabel.setVisible(false);
        usersLabel.setManaged(false);

        Service.setBaseFormController(this);
        activeLabel = mainLabel;
        mainLabel.getStyleClass().add("nav-item-active");
        switchContent("MainForm");
        Service.setUserChangedListener(() -> {
            updateUserInfo(Service.getCurrentUser());
        });

        User currentUser = Service.getCurrentUser();
        updateUserInfo(currentUser);
        exitLabel.setOnAction(actionEvent -> {
            try {
                if (currentUser != null) {
                    AuthLogRepository.addLog(
                            currentUser,
                            currentUser.getLogin(),
                            true,
                            "LOGOUT"
                    );
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            Service.setCurrentUser(null);
            try {
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
            Service.setExhibit(null);
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
            try {
                switchContent("ReferenceBooksForm");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        employeesLabel.setOnMouseClicked(e -> {
            setActive(employeesLabel);
            try {
                switchContent("EmployeesListForm");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        usersLabel.setOnMouseClicked(e -> {
            setActive(usersLabel);
            try {
                switchContent("UsersListForm");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        ownersLabel.setOnMouseClicked(e -> {
            setActive(ownersLabel);
            try {
                switchContent("OwnersListForm");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    private void updateUserInfo(User user) {
        if (user == null) {
            fioLabel.setText("— — —");
            roleLabel.setText("");
            return;
        }

        String fio = user.getEmployeeFio();
        if (fio != null && !fio.isEmpty()) {
            fioLabel.setText(user.getShortFio());
        } else {
            fioLabel.setText("— — —");
        }
        roleLabel.setText(user.getRole());

        service.setupAccessRights(employeesLabel, usersLabel);
    }

    public void setActive(Label clickedLabel) {
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
