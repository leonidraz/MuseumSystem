package com.example.museumcatalog.Controllers;

import com.example.museumcatalog.DateTimeUtil;
import com.example.museumcatalog.Models.AuthLog;
import com.example.museumcatalog.Service;
import com.example.museumcatalog.Storages.AuthLogRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;

public class AuthLogsFormController {

    // Таблица logsTable
    @FXML private TableView<AuthLog> logsTable;

    // Колонки таблицы logsTable
    @FXML private TableColumn<AuthLog, String> dateCol;
    @FXML private TableColumn<AuthLog, String> loginCol;
    @FXML private TableColumn<AuthLog, String> userCol;
    @FXML private TableColumn<AuthLog, String> eventCol;
    @FXML private TableColumn<AuthLog, Boolean> successCol;
    @FXML private TableColumn<AuthLog, String> ipCol;

    // Элементы фильтрации/поиска
    @FXML private TextField globalSearch;
    @FXML private DatePicker dateFrom;
    @FXML private DatePicker dateTo;
    @FXML private ComboBox<String> eventTypeFilter;
    @FXML private ComboBox<String> successFilter;

    //Кнопки
    @FXML private Button closeBtn;

    //Другие элементы
    @FXML private Label totalCountLabel;

    private FilteredList<AuthLog> filteredLogs;

    Service service = new Service();

    public void initialize() throws SQLException {
        setupColumns();
        loadLogs();
        initFilters();
//        setupListeners();
        setupButtonHandlers();
    }

    private void setupColumns() {
        loginCol.setCellValueFactory(cell -> cell.getValue().loginProperty());
        userCol.setCellValueFactory(cell -> cell.getValue().userFioProperty());
        eventCol.setCellValueFactory(cell -> cell.getValue().eventTypeProperty());
        successCol.setCellValueFactory(cell -> cell.getValue().successProperty());
        ipCol.setCellValueFactory(cell -> cell.getValue().ipAddressProperty());
        dateCol.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        DateTimeUtil.formatForDisplay(
                                cell.getValue().getDateTime()
                        )
                )
        );
    }

    private void loadLogs() throws SQLException {
        AuthLogRepository.loadAll();

        filteredLogs = new FilteredList<>(
                AuthLogRepository.getAllLogs(),
                p -> true
        );
        SortedList<AuthLog> sorted = new SortedList<>(filteredLogs);
        sorted.comparatorProperty().bind(logsTable.comparatorProperty());
        logsTable.setItems(sorted);
        updateCount();
    }

    private void initFilters() {
        eventTypeFilter.getItems().addAll("Все", "LOGIN", "LOGOUT");
        eventTypeFilter.setValue("Все");

        successFilter.getItems().addAll("Все", "успешно", "неуспешно");
        successFilter.setValue("Все");
    }

    private void setupButtonHandlers() {
        closeBtn.setOnAction(actionEvent -> {
            ((Stage) closeBtn.getScene().getWindow()).close();
        });
    }

    private void updateCount() {
        totalCountLabel.setText("Всего: " + filteredLogs.size());
    }

}
