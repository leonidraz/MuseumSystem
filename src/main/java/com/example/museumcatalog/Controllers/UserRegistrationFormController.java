package com.example.museumcatalog.Controllers;

import com.example.museumcatalog.Models.Employee;
import com.example.museumcatalog.Models.User;
import com.example.museumcatalog.Service;
import com.example.museumcatalog.Storages.EmployeeRepository;
import com.example.museumcatalog.Storages.UserRepository;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;

public class UserRegistrationFormController {

    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<Employee> employeeCombo;
    @FXML private ComboBox<String> roleCombo;
    @FXML private ComboBox<String> statusCombo;

    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;
    @FXML private Label titleLabel;
    @FXML private Label passwordTitle;
    @FXML private Label employeeErrorLabel;

    private final Service service = new Service();
    private User editingUser = Service.getUser();

    public void initialize() throws Exception {
        initComboBoxes();
        initErrorLabel();

        if (editingUser == null) {
            initNewUserForm();
        } else {
            initEditUserForm();
        }

        initButtons();
    }

    private void initNewUserForm() {
        statusCombo.setValue(statusCombo.getItems().getFirst());
        statusCombo.setDisable(true);
    }

    private void initComboBoxes() throws SQLException {
        EmployeeRepository.loadActive();
        employeeCombo.setItems(EmployeeRepository.getActiveEmployees());
        setupEmployeeComboBox(employeeCombo);
        roleCombo.getItems().addAll(service.getValuesComboBox("roles", "role_name", null));
        statusCombo.getItems().addAll(service.getValuesComboBox("user_statuses", "status_name", null));
    }

    private void initEditUserForm() throws Exception {
        titleLabel.setText("Редактирование пользователя");
        passwordTitle.setText("Пароль");

        boolean selfEdit = editingUser.getId() == Service.getCurrentUser().getId();

        if (selfEdit) {
            roleCombo.setDisable(true);
            statusCombo.setDisable(true);
        }

        boolean hasDocs = UserRepository.hasUserDocuments(editingUser.getId());

        if (hasDocs) {
            showEmployeeRestriction();
        }

        loadData();
    }

    private void showEmployeeRestriction() {
        employeeErrorLabel.setVisible(true);
        employeeErrorLabel.setManaged(true);
        employeeCombo.setDisable(true);
    }

    private void initErrorLabel() {
        employeeErrorLabel.setVisible(false);
        employeeErrorLabel.setManaged(false);
    }

    private void setupEmployeeComboBox(ComboBox<Employee> cb) {
        cb.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Employee emp, boolean empty) {
                super.updateItem(emp, empty);
                setText((empty || emp == null) ? null : emp.getFullFio() + ", " + emp.getPosition().toLowerCase());
            }
        });
        cb.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Employee emp, boolean empty) {
                super.updateItem(emp, empty);
                setText((empty || emp == null) ? "Выберите сотрудника" : emp.getFullFio() + ", " + emp.getPosition().toLowerCase());
            }
        });
    }

    private void loadData() {
        loginField.setText(editingUser.getLogin());
        if (editingUser.getEmployeeId() != null) {
            employeeCombo.setValue(EmployeeRepository.getActiveEmployees().stream()
                    .filter(emp -> emp.getId() == editingUser.getEmployeeId())
                    .findFirst()
                    .orElse(null));
        }
        roleCombo.setValue(editingUser.getRole());
        statusCombo.setValue(editingUser.getStatus());

    }

    private void initButtons() {
        cancelBtn.setOnAction(e -> {
            Service.setUser(null);
            ((Stage) cancelBtn.getScene().getWindow()).close();
        });

        saveBtn.setOnAction(e -> {
            if (!validate()) return;
            boolean isUpdate = editingUser != null;
            User temp = new User();
            if (isUpdate) {
                temp.setId(editingUser.getId());
            }

            temp.setLogin(loginField.getText());
            temp.setRole(roleCombo.getValue());
            temp.setStatus(statusCombo.getValue());

            if (employeeCombo.getValue() != null) {
                temp.setEmployeeId(employeeCombo.getValue().getId());
                temp.setEmployeeFio(employeeCombo.getValue().getFullFio());
            }

            String newPassword = passwordField.getText();
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                temp.setPassword(UserRepository.hashPassword(newPassword));
            } else if (isUpdate) {
                temp.setPassword(editingUser.getPassword());
            }

            try {
                int result = UserRepository.addOrUpdate(temp);
                if (result > 0) {
                    if (isUpdate) {
                        int index = UserRepository.getUsers().indexOf(editingUser);
                        if (index >= 0) {
                            temp.setId(editingUser.getId());
                            UserRepository.getUsers().set(index, temp);
                        }
                    } else {
                        temp.setId(result);
                        UserRepository.getUsers().add(temp);
                    }
                    if (editingUser != null
                            && editingUser.getId() == Service.getCurrentUser().getId()) {
                        Service.setCurrentUser(temp);
                        Service.notifyUserChanged();
                    }

                    editingUser = temp;

                    service.openAlert(Alert.AlertType.INFORMATION, isUpdate ? "Пользователь обновлён" : "Пользователь добавлен", "Успех");
                    titleLabel.setText("Редактирование пользователя");
                    if (!(editingUser.getId() == Service.getCurrentUser().getId())) {
                        statusCombo.setDisable(false);
                    }
                    passwordTitle.setText("Пароль");
                } else {
                    service.openAlert(Alert.AlertType.ERROR, "Ошибка сохранения", "Неуспешно");
                }

            } catch (SQLException ex) {
                if ("23505".equals(ex.getSQLState())) {
                    service.openAlert(Alert.AlertType.WARNING, "Этот сотрудник уже имеет учетную запись", "Предупреждение");
                } else {
                    service.openAlert(Alert.AlertType.ERROR, "Ошибка БД: " + ex.getMessage(), "Ошибка");
                }
            }
        });
    }

    private boolean validate() {
        boolean valid = true;

        if (editingUser == null) {
            if (isBlank(passwordField)) {
                error(passwordField);
                valid = false;
            }
        }
        if (isBlank(loginField)) {error(loginField);valid = false;}
        if (roleCombo.getValue() == null) {error(roleCombo);valid = false;}
        if (statusCombo.getValue() == null) {error(statusCombo);valid = false;}

        return valid;
    }

    private void error(Control c) {
        service.markFieldAsError(c);
    }

    private boolean isBlank(TextField f) {
        return f.getText() == null || f.getText().trim().isEmpty();
    }
}