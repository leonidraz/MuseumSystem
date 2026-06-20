package com.example.museumcatalog.Controllers;

import com.example.museumcatalog.Models.Employee;
import com.example.museumcatalog.Service;
import com.example.museumcatalog.Storages.EmployeeRepository;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;

public class EmployeeRegistrationFormController {
    @FXML private TextField lastNameField;
    @FXML private TextField firstNameField;
    @FXML private TextField middleNameField;
    @FXML private ComboBox<String> positionCombo;
    @FXML private ComboBox<String> statusCombo;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;
    @FXML private Label titleLabel;

    private Employee editingEmployee = Service.getEmployee();

    private final Service service = new Service();

    public void initialize() throws SQLException {
        initComboBoxes();
        initButtons();
        if (editingEmployee != null) {
            titleLabel.setText("Редактирование сотрудника");
            loadEmployeeData();
        } else{
            statusCombo.setValue(statusCombo.getItems().getFirst());
            statusCombo.setDisable(true);
        }
    }

    private void initComboBoxes() throws SQLException {
        positionCombo.getItems().addAll(service.getValuesComboBox("employee_positions", "position_name", null));
        statusCombo.getItems().addAll(service.getValuesComboBox("employee_statuses", "status_name", null));
    }

    private void loadEmployeeData() {
        lastNameField.setText(editingEmployee.getLastName());
        firstNameField.setText(editingEmployee.getFirstName());
        middleNameField.setText(editingEmployee.getMiddleName());
        positionCombo.setValue(editingEmployee.getPosition());
        emailField.setText(editingEmployee.getEmail());
        phoneField.setText(editingEmployee.getPhone());
        statusCombo.setValue(editingEmployee.getStatus());
    }

    private void initButtons() {
        cancelBtn.setOnAction(event -> {
            Service.setEmployee(null);
            ((Stage) cancelBtn.getScene().getWindow()).close();
        });
        saveBtn.setOnAction(event -> {
            if (!validateRequiredFields()) {
                service.openAlert(Alert.AlertType.WARNING, "Заполните обязательные поля", "Ошибка");
                return;
            }
            if (phoneField.getText() != null && !phoneField.getText().isEmpty()) {
                String digits = phoneField.getText().replaceAll("\\D", "");
                if (!digits.matches("\\d{10,11}")) {
                    service.markFieldAsError(phoneField);
                    service.openAlert(Alert.AlertType.WARNING, "Номер телефона должен содержать от 10 до 11 цифр", "Предупреждение!");
                    return;
                }
            }

            boolean isUpdate = editingEmployee != null;
            if (!isUpdate) {
                editingEmployee = new Employee();
                Service.setEmployee(editingEmployee);
            }
            editingEmployee.setLastName(lastNameField.getText().trim());
            editingEmployee.setFirstName(firstNameField.getText().trim());
            editingEmployee.setMiddleName(toNull(middleNameField.getText()));
            editingEmployee.setPosition(positionCombo.getValue());
            editingEmployee.setEmail(toNull(emailField.getText()));
            editingEmployee.setPhone(toNull(phoneField.getText()));
            editingEmployee.setStatus(statusCombo.getValue());

            try {
                int result = EmployeeRepository.addOrUpdate(editingEmployee);
                if (result > 0) {
                    if (!isUpdate) {
                        editingEmployee.setId(result);
                        EmployeeRepository.getAllEmployees().add(editingEmployee);
                    }
                    service.openAlert(Alert.AlertType.INFORMATION, isUpdate ? "Данные сотрудника успешно обновлены" : "Сотрудник успешно добавлен", "Успех");
                    titleLabel.setText("Редактирование сотрудника");
                    statusCombo.setDisable(false);
                } else {
                    service.openAlert(Alert.AlertType.ERROR, "Ошибка при сохранении данных", "Неуспешно!");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                service.openAlert(Alert.AlertType.ERROR, "Ошибка базы данных", "Неуспешно!");
            }
        });
    }

    private boolean validateRequiredFields() {
        boolean valid = true;
        if (isBlank(lastNameField)) {service.markFieldAsError(lastNameField);valid = false;}
        if (isBlank(firstNameField)) {service.markFieldAsError(firstNameField);valid = false;}
        if (positionCombo.getValue() == null) {service.markFieldAsError(positionCombo);valid = false;}
        if (statusCombo.getValue() == null) {service.markFieldAsError(statusCombo);valid = false;}
        return valid;
    }

    private boolean isBlank(TextField field) {
        return field.getText() == null || field.getText().trim().isEmpty();
    }

    private String toNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
