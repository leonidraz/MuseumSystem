package com.example.museumcatalog.Controllers;

import com.example.museumcatalog.Models.Employee;
import com.example.museumcatalog.Models.Exhibit;
import com.example.museumcatalog.Models.User;
import com.example.museumcatalog.Service;
import com.example.museumcatalog.Storages.EmployeeRepository;
import com.example.museumcatalog.Storages.UserRepository;
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

public class UsersListFormController {
    // Таблица usersTable
    @FXML private TableView<User> usersTable;

    // Колонки таблицы usersTable
    @FXML private TableColumn<User, String> loginUserColumn;
    @FXML private TableColumn<User, String> hashPaswordUserColumn;
    @FXML private TableColumn<User, String> employeeUserColumn;
    @FXML private TableColumn<User, String> roleUserColumn;
    @FXML private TableColumn<User, String> statusUserColumn;

    // Кнопки
    @FXML private Button addUserBtn;
    @FXML private Button deleteUserBtn;
    @FXML private Button editUserBtn;
    @FXML private Button authLogsBtn;
    @FXML private Button refreshBtn;

    // Элементы фильтрации/поиска
    @FXML private ComboBox<String> filterUserRole;
    @FXML private ComboBox<String> filterUserStatus;
    @FXML private TextField search;

    //Другие элементы
    @FXML private BorderPane rootPane;
    @FXML private Label totalCountLabel;

    private FilteredList<User> filteredUsers;
    Service service = new Service();

    public void initialize() throws SQLException {
        setupTableColumns();
        loadUsers();
        initComboBoxes();
        setupListeners();
        setupButtonHandlers();
    }

    private void setupTableColumns() {
        loginUserColumn.setCellValueFactory(cell -> cell.getValue().loginProperty());
        hashPaswordUserColumn.setCellValueFactory(cell -> cell.getValue().passwordProperty());
        employeeUserColumn.setCellValueFactory(cell -> cell.getValue().employeeFioProperty());
        roleUserColumn.setCellValueFactory(cell -> cell.getValue().roleProperty());
        statusUserColumn.setCellValueFactory(cell -> cell.getValue().statusProperty());
    }

    private void loadUsers() throws SQLException {
        UserRepository.loadAll();
        filteredUsers = new FilteredList<>(
                UserRepository.getUsers(),
                p -> true
        );
        SortedList<User> sortedList = new SortedList<>(filteredUsers);
        sortedList.comparatorProperty().bind(usersTable.comparatorProperty());
        usersTable.setItems(sortedList);
        updateUsersCount();
    }

    private void initComboBoxes() throws SQLException {
        filterUserRole.getItems().add("Все роли");
        filterUserRole.getItems().addAll(service.getValuesComboBox("roles", "role_name", null));
        filterUserRole.setValue(filterUserRole.getItems().getFirst());
        filterUserStatus.getItems().add("Все статусы");
        filterUserStatus.setValue(filterUserStatus.getItems().getFirst());
        filterUserStatus.getItems().addAll(service.getValuesComboBox("user_statuses", "status_name", null));
    }

    private void setupListeners() {
        search.textProperty().addListener((obs, oldV, newV) -> applyFilters());
        filterUserRole.valueProperty().addListener((obs, oldV, newV) -> applyFilters());
        filterUserStatus.valueProperty().addListener((obs, oldV, newV) -> applyFilters());
    }

    private void applyFilters() {
        filteredUsers.setPredicate(getUsersPredicate());
        updateUsersCount();
    }

    private Predicate<User> getUsersPredicate() {
        return user -> {
            if (!checkUserFilters(user)) return false;
            if (!checkUserSearch(user)) return false;
            return true;
        };
    }

    private boolean checkUserFilters(User user) {
        String role = filterUserRole.getValue();
        String status = filterUserStatus.getValue();

        if (role != null && !"Все роли".equals(role) && !role.equals(user.getRole())) {
            return false;
        }
        if (status != null && !"Все статусы".equals(status) && !status.equals(user.getStatus())) {
            return false;
        }

        return true;
    }

    private boolean checkUserSearch(User user) {
        String q = search.getText();
        if (q == null || q.trim().isEmpty()) return true;

        String query = q.toLowerCase().trim();

        String fullText = safe(user.getLogin()) + " " + safe(user.getFullFio());

        return fullText.toLowerCase().contains(query);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private void setupButtonHandlers() {
        refreshBtn.setOnAction(actionEvent -> {
            search.clear();
            filterUserRole.setValue("Все роли");
            filterUserStatus.setValue("Все статусы");
            usersTable.getSelectionModel().clearSelection();
            usersTable.getSortOrder().clear();
            applyFilters();
            filteredUsers.setPredicate(User -> true);
        });

        addUserBtn.setOnAction(actionEvent -> {
            try {
                Service.openModal("UserRegistraionForm", "Добавление пользователя",
                        (Stage) addUserBtn.getScene().getWindow());
                updateUsersCount();
                applyFilters();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        editUserBtn.setOnAction(actionEvent -> {
            User selected = usersTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Service.setUser(selected);
                try {
                    Service.openModal("UserRegistraionForm", "Редактирование пользователя",
                            (Stage) editUserBtn.getScene().getWindow());
                    applyFilters();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                service.openAlert(Alert.AlertType.WARNING, "Выберите пользователя из таблицы для редактирования", "Предупреждение!");
            }
        });
        deleteUserBtn.setOnAction(actionEvent -> {
            User selected = usersTable.getSelectionModel().getSelectedItem();

            if (selected == null) {
                service.openAlert(Alert.AlertType.WARNING, "Выберите пользователя из таблицы для удаления", "Предупреждение!");
                return;
            }

            boolean isSelf = selected.getId() == Service.getCurrentUser().getId();

            if (isSelf) {
                service.openAlert(Alert.AlertType.WARNING, "Нельзя удалить свою учетную запись", "Предупреждение!");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Удалить пользователя:\n" + selected.getLogin() + "?", ButtonType.YES, ButtonType.NO);

            confirm.setTitle("Подтверждение удаления");

            if (confirm.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) return;

            try {
                boolean hasDocs = UserRepository.hasUserDocuments(selected.getId());
                if (hasDocs) {
                    service.openAlert(Alert.AlertType.WARNING, "Невозможно удалить пользователя: он создавал документы", "Ошибка удаления");
                    return;
                }
                boolean deleted = UserRepository.deleteUser(selected.getId());
                if (deleted) {
                    UserRepository.getUsers().remove(selected);
                    applyFilters();
                    updateUsersCount();
                    service.openAlert(Alert.AlertType.INFORMATION, "Пользователь успешно удалён", "Успех");

                } else {
                    service.openAlert(Alert.AlertType.WARNING, "Пользователь не найден или уже удалён", "Ошибка");
                }

            } catch (SQLException ex) {
                service.openAlert(Alert.AlertType.ERROR, "Ошибка базы данных: " + ex.getMessage(), "Ошибка");
            }
        });
        authLogsBtn.setOnAction(actionEvent -> {
            try {
                Service.openModal("AuthLogsListForm", "Cписок логов авторизации",
                        (Stage) addUserBtn.getScene().getWindow());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void updateUsersCount() {
        totalCountLabel.setText("Всего: " + filteredUsers.size());
    }
}
