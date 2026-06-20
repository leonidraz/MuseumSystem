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

import java.sql.PreparedStatement;
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
        setupListeners();
        setupButtonHandlers();
        try {
            AuthLogRepository.deleteOldLogs();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
        eventTypeFilter.getItems().addAll("Все события", "LOGIN", "LOGOUT");
        eventTypeFilter.setValue("Все события");

        successFilter.getItems().addAll("Все попытки", "успешно", "неуспешно");
        successFilter.setValue("Все попытки");
    }

    private void setupListeners() {
        globalSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        eventTypeFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        successFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        dateFrom.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        dateTo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void setupButtonHandlers() {
        closeBtn.setOnAction(actionEvent -> {
            ((Stage) closeBtn.getScene().getWindow()).close();
        });
    }

    private void applyFilters() {
        filteredLogs.setPredicate(log -> {
            // Поиск логину, IP, пользователю
            String searchText = globalSearch.getText();
            if (searchText != null && !searchText.isBlank()) {
                String q = searchText.toLowerCase().trim();
                boolean match = contains(log.getLogin(), q)
                                || contains(log.getUserFio(), q)
                                || contains(log.getIpAddress(), q);
                if (!match) {
                    return false;
                }
            }
            //Фильтр по типу события
            String eventType = eventTypeFilter.getValue();
            if (eventType != null && !"Все события".equals(eventType) && !eventType.equals(log.getEventType())) {
                return false;
            }

            //Фильтр по типу попытки (успешно/неуспешно)
            String success = successFilter.getValue();
            if (success != null && !"Все попытки".equals(success)) {
                boolean requiredSuccess = "успешно".equals(success);
                if (log.isSuccess() != requiredSuccess) {
                    return false;
                }
            }
            //Дата с
            if (dateFrom.getValue() != null) {
                if (log.getDateTime() == null || log.getDateTime().toLocalDate().isBefore(dateFrom.getValue())) {
                    return false;
                }
            }
            //Дата по
            if (dateTo.getValue() != null) {
                if (log.getDateTime() == null || log.getDateTime().toLocalDate().isAfter(dateTo.getValue())) {
                    return false;
                }
            }
            return true;
        });
        updateCount();
    }

    private boolean contains(String value, String query) {
        return value != null &&
                value.toLowerCase().contains(query);
    }

    private void updateCount() {
        totalCountLabel.setText("Всего: " + filteredLogs.size());
    }
}
