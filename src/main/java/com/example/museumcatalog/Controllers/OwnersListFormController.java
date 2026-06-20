package com.example.museumcatalog.Controllers;

import com.example.museumcatalog.Models.Owner;
import com.example.museumcatalog.Service;
import com.example.museumcatalog.Storages.OwnerRepository;
import javafx.beans.binding.Bindings;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class OwnersListFormController {

    //Центральная таблица ownersTable
    @FXML private TableView<Owner> ownersTable;

    //Колонки таблицы ownersTable
    @FXML private TableColumn<Owner, String> fioColumn;
    @FXML private TableColumn<Owner, String> passportColumn;
    @FXML private TableColumn<Owner, String> phoneColumn;
    @FXML private TableColumn<Owner, String> addressColumn;
    @FXML private TableColumn<Owner, String> issuedByColumn;
    @FXML private TableColumn<Owner, String> dateOfIssueColumn;
    @FXML private TableColumn<Owner, String> noticeColumn;

    //Элементы поиска/фильтрации
    @FXML private TextField fioSearch;
    @FXML private TextField passportSeriesSearch;
    @FXML private TextField passportNumberSearch;
    @FXML private TextField phoneSearch;
    @FXML private TextField addressSearch;
    @FXML private TextField globalSearch;

    //Кнопки
    @FXML private Button addOwnerBtn;
    @FXML private Button editOwnerBtn;
    @FXML private Button deleteOwnerBtn;
    @FXML private Button refreshBtn;

    //Другие элементы
    @FXML private Label totalCountLabel;

    private final Service service = new Service();
    private FilteredList<Owner> filteredOwners;

    @FXML
    public void initialize() throws SQLException {
        setupTableColumns();
        loadOwners();
        setupListeners();
        setupButtonHandlers();
    }

    //Связываем колонки с полями модели
    private void setupTableColumns() {
        fioColumn.setCellValueFactory(cell ->
                Bindings.createStringBinding(
                        () -> cell.getValue().getFullFio(),
                        cell.getValue().lastNameProperty(),
                        cell.getValue().firstNameProperty(),
                        cell.getValue().middleNameProperty()
                )
        );
        passportColumn.setCellValueFactory(cell ->
                Bindings.createStringBinding(
                        () -> cell.getValue().getPassport(),
                        cell.getValue().passportSeriesProperty(),
                        cell.getValue().passportNumberProperty()
                )
        );
        phoneColumn.setCellValueFactory(cell -> cell.getValue().phoneProperty());
        addressColumn.setCellValueFactory(cell -> cell.getValue().addressProperty());
        issuedByColumn.setCellValueFactory(cell -> cell.getValue().issuedByProperty());
        dateOfIssueColumn.setCellValueFactory(cell -> cell.getValue().dateOfIssueProperty().asString());
        noticeColumn.setCellValueFactory(cell -> cell.getValue().noticeProperty());
    }

    private void loadOwners() throws SQLException {
        OwnerRepository.loadAll();
        filteredOwners = new FilteredList<>(
                OwnerRepository.getOwners(),
                p -> true
        );
        SortedList<Owner> sortedList = new SortedList<>(filteredOwners);

        sortedList.comparatorProperty().bind(ownersTable.comparatorProperty());
        ownersTable.setItems(sortedList);
        totalCountLabel.setText("Всего: " + filteredOwners.size());
    }

    private void setupListeners() {
        // Расширенный поиск (фильтры слева)
        fioSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        passportSeriesSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        passportNumberSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        phoneSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        addressSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // Быстрый поиск (верхняя панель)
        globalSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void applyFilters() {
        filteredOwners.setPredicate(owner -> {
            String globalText = globalSearch.getText();
            if (globalText != null && !globalText.trim().isEmpty()) {
                String q = normalize(globalText);
                String fio = normalize(owner.getLastName() + " " + owner.getFirstName() + " " + owner.getMiddleName());
                String passport = normalize(owner.getPassportSeries() + " " + owner.getPassportNumber());
                String phone = normalize(owner.getPhone());
                String address = normalize(owner.getAddress());
                String issuedBy = normalize(owner.getIssuedBy());
                String notice = normalize(owner.getNotice());
                String dateOfIssue = normalize(owner.getDateOfIssue() == null ? "" : owner.getDateOfIssue().toString());
                boolean globalMatch =
                        fio.contains(q) ||
                                passport.contains(q) ||
                                phone.contains(q) ||
                                address.contains(q) ||
                                issuedBy.contains(q) ||
                                notice.contains(q) ||
                                dateOfIssue.contains(q);
                if (!globalMatch) {
                    return false;
                }
            }

            //Поиск по ФИО (фамилия / имя / отчество)
            String fioText = fioSearch.getText();
            if (fioText != null && !fioText.isEmpty()) {
                String t = fioText.toLowerCase().trim().replaceAll("\\s+", " ");
                String fio = ((owner.getLastName() == null ? "" : owner.getLastName()) + " " +
                                (owner.getFirstName() == null ? "" : owner.getFirstName()) + " " +
                                (owner.getMiddleName() == null ? "" : owner.getMiddleName())
                ).toLowerCase().replaceAll("\\s+", " ");
                if (!fio.contains(t)) {
                    return false;
                }
            }
            //Поиск по серии паспорта
            String seriesText = passportSeriesSearch.getText();
            if (seriesText != null && !seriesText.isEmpty()) {
                String series = owner.getPassportSeries() == null ? "" : owner.getPassportSeries().toLowerCase();
                if (!series.contains(seriesText.toLowerCase().trim())) {
                    return false;
                }
            }
            //Поиск по номеру паспорта
            String numberText = passportNumberSearch.getText();
            if (numberText != null && !numberText.isEmpty()) {
                String number = owner.getPassportNumber() == null ? "" : owner.getPassportNumber().toLowerCase();
                if (!number.contains(numberText.toLowerCase().trim())) {
                    return false;
                }
            }
            //Поиск по телефону
            String phoneText = phoneSearch.getText();
            if (phoneText != null && !phoneText.isEmpty()) {
                String phone = owner.getPhone() == null ? "" : owner.getPhone().toLowerCase();
                if (!phone.contains(phoneText.toLowerCase().trim())) {
                    return false;
                }
            }
            //Поиск по адресу
            String addressText = addressSearch.getText();
            if (addressText != null && !addressText.isEmpty()) {
                String search = normalize(addressText);
                String address = normalize(owner.getAddress());
                if (!address.contains(search)) {
                    return false;
                }
            }
            return true;
        });
        totalCountLabel.setText("Всего: " + filteredOwners.size());
    }
    private void setupButtonHandlers() {
        refreshBtn.setOnAction(actionEvent -> {
            fioSearch.clear();
            passportSeriesSearch.clear();
            passportNumberSearch.clear();
            phoneSearch.clear();
            addressSearch.clear();
            globalSearch.clear();
            ownersTable.getSelectionModel().clearSelection();
            ownersTable.getSortOrder().clear();
            filteredOwners.setPredicate(doc -> true);
        });

        addOwnerBtn.setOnAction(actionEvent -> {
            try {
                Service.openModal("OwnerRegistrationForm", "Создание владельца",
                        (Stage) addOwnerBtn.getScene().getWindow());
                totalCountLabel.setText("Всего: " + filteredOwners.size());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        editOwnerBtn.setOnAction(actionEvent -> {
            Owner selected = ownersTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Service.setOwner(selected);
                try {
                    Service.openModal("OwnerRegistrationForm", "Редактирование владельца",
                            (Stage) editOwnerBtn.getScene().getWindow());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                service.openAlert(Alert.AlertType.WARNING, "Выберите владельца из таблицы для редактирования", "Предупреждение!");
            }
        });

        deleteOwnerBtn.setOnAction(actionEvent -> {
            Owner selected = ownersTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                service.openAlert(Alert.AlertType.WARNING, "Выберите владельца из таблицы для удаления", "Предупреждение!");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Удалить владельца:\n" + selected.getFullFio() + "?", ButtonType.YES, ButtonType.NO);
            confirm.setTitle("Подтверждение удаления");

            if (confirm.showAndWait().get() != ButtonType.YES) return;

            try {
                boolean deleted = OwnerRepository.deleteOwner(selected.getId());

                if (deleted) {
                    OwnerRepository.getOwners().remove(selected);
                    totalCountLabel.setText("Всего: " + filteredOwners.size());
                    service.openAlert(Alert.AlertType.INFORMATION, "Владелец успешно удалён", "Успех");
                } else {
                    service.openAlert(Alert.AlertType.WARNING, "Владелец не найден или уже удалён", "Ошибка");
                }

            } catch (SQLException ex) {
                if ("23503".equals(ex.getSQLState())) {
                    service.openAlert(Alert.AlertType.WARNING, "Невозможно удалить владельца: он связан с предметами или документами.", "Ошибка удаления");
                } else {
                    service.openAlert(Alert.AlertType.ERROR, "Ошибка базы данных: " + ex.getMessage(), "Ошибка");
                }
            }
        });
    }
    private String normalize(String s) {
        if (s == null) return "";
        return s.toLowerCase()
                .replace('\u00A0', ' ')
                .replaceAll("[.,]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}