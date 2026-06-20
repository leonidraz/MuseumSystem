package com.example.museumcatalog.Controllers;

import com.example.museumcatalog.Models.Collection;
import com.example.museumcatalog.Models.Employee;
import com.example.museumcatalog.Models.Fund;
import com.example.museumcatalog.Models.Owner;
import com.example.museumcatalog.Service;
import com.example.museumcatalog.Storages.EmployeeRepository;
import com.example.museumcatalog.Storages.FundRepository;
import javafx.beans.binding.Bindings;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.function.Predicate;

public class EmployeesListFormController {
    // Таблица employeesTable
    @FXML private TableView<Employee> employeesTable;

    // Колонки таблицы employeesTable
    @FXML private TableColumn<Employee, String> fioEmployeeColumn;
    @FXML private TableColumn<Employee, String> positionEmployeeColumn;
    @FXML private TableColumn<Employee, String> emailEmployeeColumn;
    @FXML private TableColumn<Employee, String> phoneEmployeeColumn;
    @FXML private TableColumn<Employee, String> statusEmployeeColumn;

    // Кнопки
    @FXML private Button createEmployeeBtn;
    @FXML private Button deleteEmployeeBtn;
    @FXML private Button editEmployeeBtn;
    @FXML private Button refreshBtn;

    // Элементы фильтрации/поиска
    @FXML private ComboBox<String> filterEmployeePosition;
    @FXML private ComboBox<String> filterEmployeeStatus;
    @FXML private TextField search;

    //Другие элементы
    @FXML private BorderPane rootPane;
    @FXML private Label totalCountLabel;

    Service service = new Service();
    private FilteredList<Employee> filteredEmployees;

    public void initialize() throws SQLException {
        setupTableColumns();
        loadEmployees();
        initComboBoxes();
        setupListeners();
        setupButtonHandlers();
    }

    private void setupTableColumns() {
        fioEmployeeColumn.setCellValueFactory(cell -> Bindings.createStringBinding(
                () -> cell.getValue().getFullFio(),
                cell.getValue().lastNameProperty(), cell.getValue().firstNameProperty(), cell.getValue().middleNameProperty()));
        positionEmployeeColumn.setCellValueFactory(cell -> cell.getValue().positionProperty());
        emailEmployeeColumn.setCellValueFactory(cell -> cell.getValue().emailProperty());
        phoneEmployeeColumn.setCellValueFactory(cell -> cell.getValue().phoneProperty());
        statusEmployeeColumn.setCellValueFactory(cell -> cell.getValue().statusProperty());
    }

    private void loadEmployees() throws SQLException {
        EmployeeRepository.loadAll();
        filteredEmployees = new FilteredList<>(
                EmployeeRepository.getAllEmployees(),
                p -> true
        );
        SortedList<Employee> sortedList = new SortedList<>(filteredEmployees);
        sortedList.comparatorProperty().bind(employeesTable.comparatorProperty());
        employeesTable.setItems(sortedList);
        updateEmployeesCount();
    }

    private void initComboBoxes() throws SQLException {
        filterEmployeePosition.getItems().add("Все должности");
        filterEmployeePosition.getItems().addAll((service.getValuesComboBox("employee_positions", "position_name", null)));
        filterEmployeePosition.setValue(filterEmployeePosition.getItems().getFirst());
        filterEmployeeStatus.getItems().add("Все статусы");
        filterEmployeeStatus.getItems().addAll((service.getValuesComboBox("employee_statuses", "status_name", null)));
        filterEmployeeStatus.setValue(filterEmployeeStatus.getItems().getFirst());
    }

    private void setupListeners() {
        search.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });
        filterEmployeePosition.valueProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });
        filterEmployeeStatus.valueProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });
    }

    private void applyFilters() {
        filteredEmployees.setPredicate(getFiltersPredicate());
        updateEmployeesCount();
    }

    private Predicate<Employee> getFiltersPredicate() {
        return employee -> {
            if (!checkPositionAndStatus(employee)) return false;
            if (!checkSearch(employee)) return false;
            return true;
        };
    }

    private boolean checkPositionAndStatus(Employee employee) {
        String position = filterEmployeePosition.getValue();
        String status = filterEmployeeStatus.getValue();
        if (position != null && !"Все должности".equals(position) && !position.equals(employee.getPosition())) {
            return false;
        }
        if (status != null && !"Все статусы".equals(status) && !status.equals(employee.getStatus())) {
            return false;
        }
        return true;
    }

    private boolean checkSearch(Employee employee) {
        String q = search.getText();
        if (q == null || q.trim().isEmpty()) return true;
        String query = q.toLowerCase().trim();
        String fullText = safe(employee.getFullFio()) + " " +
                        safe(employee.getPosition()) + " " +
                        safe(employee.getEmail()) + " " +
                        safe(employee.getPhone()) + " " +
                        safe(employee.getStatus());
        return fullText.toLowerCase().contains(query);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
    private void setupButtonHandlers() {
        refreshBtn.setOnAction(actionEvent -> {
            search.clear();
            filterEmployeePosition.setValue("Все должности");
            filterEmployeeStatus.setValue("Все статусы");
            employeesTable.getSelectionModel().clearSelection();
            employeesTable.getSortOrder().clear();
            applyFilters();
            filteredEmployees.setPredicate(doc -> true);
        });

        createEmployeeBtn.setOnAction(actionEvent -> {
            try {
                Service.openModal("EmployeeRegistrationForm", "Добавление сотрудника",
                        (Stage) createEmployeeBtn.getScene().getWindow());
                updateEmployeesCount();
                applyFilters();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        editEmployeeBtn.setOnAction(actionEvent -> {
            Employee selected = employeesTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Service.setEmployee(selected);
                try {
                    Service.openModal("EmployeeRegistrationForm", "Редактирование сотрудника",
                            (Stage) editEmployeeBtn.getScene().getWindow());
                    applyFilters();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                service.openAlert(Alert.AlertType.WARNING, "Выберите сотрудника из таблицы для редактирования", "Предупреждение!");
            }
        });
        deleteEmployeeBtn.setOnAction(event -> {
            Employee selected = employeesTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                service.openAlert(Alert.AlertType.WARNING, "Выберите сотрудника из таблицы", "Предупреждение");
                return;
            }
            try {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Удалить сотрудника:\n" + selected.getFullFio() + "?", ButtonType.YES, ButtonType.NO);
                confirm.setTitle("Подтверждение удаления");
                if (confirm.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
                    return;
                }
                boolean deleted = EmployeeRepository.deleteEmployee(selected.getId());
                if (deleted) {
                    EmployeeRepository.getAllEmployees().remove(selected);
                    applyFilters();
                    updateEmployeesCount();
                    service.openAlert(Alert.AlertType.INFORMATION, "Сотрудник удалён", "Успех");
                } else {
                    service.openAlert(Alert.AlertType.WARNING, "Сотрудник не найден", "Предупреждение!");
                }
            } catch (SQLException ex) {
                if ("23503".equals(ex.getSQLState())) {
                    service.openAlert(Alert.AlertType.WARNING, "Нельзя удалить сотрудника: он связан с учетной записью или другими данными", "Предупреждение!");
                    return;
                }
                service.openAlert(Alert.AlertType.ERROR, "Ошибка базы данных: " + ex.getMessage(), "Ошибка");
            }
        });
    }
    private void updateEmployeesCount() {
        totalCountLabel.setText("Всего: " + filteredEmployees.size());
    }
}
