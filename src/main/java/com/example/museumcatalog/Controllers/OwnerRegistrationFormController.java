package com.example.museumcatalog.Controllers;

import com.example.museumcatalog.Models.Owner;
import com.example.museumcatalog.Service;
import com.example.museumcatalog.Storages.OwnerRepository;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;

public class OwnerRegistrationFormController {

    @FXML private TextField lastName, firstName, middleName;
    @FXML private TextField passportSeries, passportNumber, phone, issuedBy;
    @FXML private DatePicker dateOfIssue;
    @FXML private TextArea address, notice;
    @FXML private Button saveBtn, cancelBtn;
    @FXML private Label titleLabel;

    private final Service service = new Service();

    @FXML
    public void initialize() {
        Owner owner = Service.getOwner();
        if (owner != null) {
            loadOwner(owner);
            titleLabel.setText("Редактирование владельца");
            saveBtn.setText("Сохранить изменения");
        }
        setupButtonHandlers();
    }

    private void setupButtonHandlers() {
        cancelBtn.setOnAction(e -> {
            Service.setOwner(null);
            closeForm();
        });
        saveBtn.setOnAction(e -> saveOwner());
    }

    // Заполняет форму, защищаясь от null
    private void loadOwner(Owner owner) {
        lastName.setText(nonNullOrDash(owner.getLastName()));
        firstName.setText(nonNullOrDash(owner.getFirstName()));
        middleName.setText(nonNullOrDash(owner.getMiddleName()));
        passportSeries.setText(nonNullOrDash(owner.getPassportSeries()));
        passportNumber.setText(nonNullOrDash(owner.getPassportNumber()));
        phone.setText(nonNullOrDash(owner.getPhone()));
        issuedBy.setText(nonNullOrDash(owner.getIssuedBy()));
        dateOfIssue.setValue(owner.getDateOfIssue());
        address.setText(nonNullOrDash(owner.getAddress()));
        notice.setText(nonNullOrDash(owner.getNotice()));
    }

    // Безопасное получение строки
    private String nonNullOrDash(String s) {
        return s == null ? "" : s;
    }

    private void saveOwner() {
        if (!validateRequiredFields()) {
            service.openAlert(Alert.AlertType.WARNING, "Не все обязательные поля заполнены. Поля, подсвеченные красным, необходимо заполнить.", "Проверка обязательных полей");
            return;
        };

        // Валидация формата
        if (!passportSeries.getText().matches("\\d{4}")) {
            service.openAlert(Alert.AlertType.WARNING, "Серия паспорта — 4 цифры", "Предупреждение!");
            service.markFieldAsError(passportSeries);
            return;
        }
        if (!passportNumber.getText().matches("\\d{6}")) {
            service.openAlert(Alert.AlertType.WARNING, "Номер паспорта — 6 цифр", "Предупреждение!");
            service.markFieldAsError(passportNumber);
            return;
        }

        String digits = phone.getText().replaceAll("\\D", "");
        if (phone.getText() != null && !phone.getText().isEmpty()) {
            if (!digits.matches("\\d{10,11}")) {
                service.markFieldAsError(phone);
                service.openAlert(Alert.AlertType.WARNING, "Номер телефона должен содержать от 10 до 11 цифр", "Предупреждение!");
                return;
            }
        }

        try {
            int series = Integer.parseInt(passportSeries.getText());
            int number = Integer.parseInt(passportNumber.getText());

            Owner owner = Service.getOwner();
            boolean isUpdate = owner != null;

            if (!isUpdate) {
                owner = new Owner();
                Service.setOwner(owner);
            }

            owner.setLastName(lastName.getText());
            owner.setFirstName(firstName.getText());
            owner.setMiddleName(toNull(middleName.getText()));
            owner.setPassportSeries(String.valueOf(series));
            owner.setPassportNumber(String.valueOf(number));
            owner.setIssuedBy(issuedBy.getText());
            owner.setDateOfIssue(dateOfIssue.getValue());
            owner.setPhone(phone.getText() != null ? phone.getText() : null);
            owner.setAddress(toNull(address.getText()));
            owner.setNotice(toNull(notice.getText()));

            int result = OwnerRepository.addOrUpdate(owner);

            if (result > 0) {
                if (!isUpdate) {
                    owner.setId(result);
                    OwnerRepository.getOwners().add(owner);
                }
                service.openAlert(Alert.AlertType.INFORMATION, isUpdate ? "Данные обновлены" : "Владелец добавлен", "Успешно!");
                titleLabel.setText("Редактирование владельца");
            } else {
                service.openAlert(Alert.AlertType.ERROR, "Ошибка при сохранении владельца", "Неуспешно!");
            }

        } catch (NumberFormatException e) {
            service.openAlert(Alert.AlertType.WARNING, "Проверьте формат числовых полей", "Ошибка");
        } catch (SQLException e) {
            service.openAlert(Alert.AlertType.ERROR, "Ошибка базы данных: " + e.getMessage(), "Ошибка!");
            e.printStackTrace();
        }
    }

    // Валидация обязательных полей с защитой от null
    private boolean validateRequiredFields() {
        boolean allFilled = true;
        if (isBlank(lastName)) { service.markFieldAsError(lastName); allFilled = false; }
        if (isBlank(firstName)) { service.markFieldAsError(firstName); allFilled = false; }
        if (isBlank(passportSeries)) { service.markFieldAsError(passportSeries); allFilled = false; }
        if (isBlank(passportNumber)) { service.markFieldAsError(passportNumber); allFilled = false; }
        if (isBlank(issuedBy)) { service.markFieldAsError(issuedBy); allFilled = false; }
        if (dateOfIssue.getValue() == null) { service.markFieldAsError(dateOfIssue); allFilled = false; }
        return allFilled;
    }

    //Проверка на пустоту
    private boolean isBlank(TextField field) {
        return field.getText() == null || field.getText().trim().isEmpty();
    }

    // Пустая строка - null для БД
    private String toNull(String s) {
        return s == null || s.trim().isEmpty() ? null : s;
    }

    private void closeForm() {
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }
}