package com.example.museumcatalog.Controllers;

import com.example.museumcatalog.DBHandler;
import com.example.museumcatalog.MainApplication;
import com.example.museumcatalog.Models.Exhibit;
import com.example.museumcatalog.Service;
import com.example.museumcatalog.Storages.EmployeeRepository;
import com.example.museumcatalog.Storages.ExhibitRepository;
import com.example.museumcatalog.Storages.OwnerRepository;
import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

public class MainFormController {

    @FXML private Button btnRegisterExhibit;
    @FXML private Button btnRegisterOwner;
    @FXML private Button btnCreateDocument;
    @FXML private Button btnAddEmployee;

    // Статистические лейблы
    @FXML private Label processedCountLabel;
    @FXML private Label inFundsCountLabel;
    @FXML private Label exhibitionsCountLabel;
    @FXML private Label ownersCountLabel;
    @FXML private Label employeesCountLabel;

    @FXML private HBox exhibitsContainer;
    @FXML private VBox bottomVBox;

    Service service = new Service();

    public static final String STATUS_PROCESSING = "В обработке";
    public static final String STATUS_IN_FUNDS = "В фонде";
    public static final String STATUS_ON_EXHIBITION = "На выставке";

    public void initialize() throws SQLException {
        btnAddEmployee.setVisible(false);
        btnAddEmployee.setManaged(false);

        addHoverAnimation(btnRegisterExhibit);
        addHoverAnimation(btnRegisterOwner);
        addHoverAnimation(btnCreateDocument);
        addHoverAnimation(btnAddEmployee);

        service.setupAccessRights(btnAddEmployee);
        loadStatistics();
        loadLastExhibits();
        setupButtonHandlers();
    }

    private void loadLastExhibits() {
        exhibitsContainer.getChildren().clear();

        ExhibitRepository.getExhibits()
                .stream()
                .sorted((a, b) -> Integer.compare(b.getId(), a.getId()))
                .limit(10)
                .forEach(exhibit -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource("fxml/ExhibitCard.fxml"));
                        VBox card = loader.load();
                        ExhibitCardController controller = loader.getController();
                        controller.setData(exhibit);
                        exhibitsContainer.getChildren().add(card);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    private void loadStatistics() throws SQLException {
        ExhibitRepository.loadAll();
        OwnerRepository.loadAll();
        EmployeeRepository.loadActive();

        long inProcessing = ExhibitRepository.getExhibits()
                .stream()
                .filter(exhibit ->
                        exhibit.getStatus() != null &&
                                exhibit.getStatus().equalsIgnoreCase(STATUS_PROCESSING)
                )
                .count();

        long inFunds = ExhibitRepository.getExhibits()
                .stream()
                .filter(exhibit ->
                        exhibit.getStatus() != null &&
                                exhibit.getStatus().equalsIgnoreCase(STATUS_IN_FUNDS)
                )
                .count();

        long onExhibition = ExhibitRepository.getExhibits()
                .stream()
                .filter(exhibit ->
                        exhibit.getStatus() != null &&
                                exhibit.getStatus().equalsIgnoreCase(STATUS_ON_EXHIBITION)
                )
                .count();

        processedCountLabel.setText(String.valueOf(inProcessing));
        inFundsCountLabel.setText(String.valueOf(inFunds));
        exhibitionsCountLabel.setText(String.valueOf(onExhibition));
        ownersCountLabel.setText(String.valueOf(OwnerRepository.getOwners().size()));
        employeesCountLabel.setText(String.valueOf(EmployeeRepository.getActiveEmployees().size()));
    }

    private void setupButtonHandlers() {
        btnRegisterExhibit.setOnAction(actionEvent -> {
            Service.setExhibit(null);
            try {
                Service.openModal("ExhibitRegistrationForm", "Регистрация предмета",
                        (Stage) btnRegisterExhibit.getScene().getWindow());
                loadStatistics();
                loadLastExhibits();
            } catch (IOException | SQLException e) {
                service.openAlert(Alert.AlertType.ERROR, "Не удалось открыть форму", "Ошибка");
            }
        });
        btnRegisterOwner.setOnAction(actionEvent -> {
            try {
                Service.openModal("OwnerRegistrationForm", "Регистрация владельца",
                        (Stage) btnRegisterExhibit.getScene().getWindow());
                loadStatistics();
            } catch (IOException | SQLException e) {
                service.openAlert(Alert.AlertType.ERROR, "Не удалось открыть форму", "Ошибка");
            }
        });
        btnCreateDocument.setOnAction(actionEvent -> {
            try {
                Service.openModal("UniversalDocumentForm", "Создание документа",
                        (Stage) btnRegisterExhibit.getScene().getWindow());
                loadStatistics();
            } catch (IOException | SQLException e) {
                service.openAlert(Alert.AlertType.ERROR, "Не удалось открыть форму", "Ошибка");
            }
        });
        btnAddEmployee.setOnAction(actionEvent -> {
            try {
                Service.openModal("EmployeeRegistrationForm", "Добавление сотрудника",
                        (Stage) btnAddEmployee.getScene().getWindow());
                loadStatistics();
            } catch (IOException | SQLException e) {
                service.openAlert(Alert.AlertType.ERROR, "Не удалось открыть форму", "Ошибка");
            }
        });
    }

    //Метод для плавного выделения кнопок на панели быстрого доступа при наведении
    private void addHoverAnimation(Button button) {
        ScaleTransition enter = new ScaleTransition(Duration.millis(150), button);
        enter.setToX(1.02);
        enter.setToY(1.02);

        ScaleTransition exit = new ScaleTransition(Duration.millis(150), button);
        exit.setToX(1);
        exit.setToY(1);

        button.setOnMouseEntered(e -> enter.playFromStart());
        button.setOnMouseExited(e -> exit.playFromStart());
    }
}