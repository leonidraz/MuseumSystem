package com.example.museumcatalog.Controllers;

import com.example.museumcatalog.Models.*;
import com.example.museumcatalog.Service;
import com.example.museumcatalog.Storages.CollectionRepository;
import com.example.museumcatalog.Storages.EmployeePositionRepository;
import com.example.museumcatalog.Storages.FundRepository;
import com.example.museumcatalog.Storages.OwnerRepository;
import javafx.application.Platform;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.function.Predicate;

public class ReferenceBooksFormController {
    // Таблица fundsTable
    @FXML private TableView<Fund> fundsTable;

    // Колонка таблицы fundsTable
    @FXML private TableColumn<Fund, String> fundNameCol;
    @FXML private TableColumn<Fund, String> kpPrefixCol;

    // Таблица collectionsTable
    @FXML private TableView<Collection> collectionsTable;

    // Колонки таблицы collectionsTable
    @FXML private TableColumn<Collection, String> collectionNameCol;
    @FXML private TableColumn<Collection, String> collectionFundCol;
    @FXML private TableColumn<Collection, String> collectionDescriptionCol;

    // Таблица positionsTable
    @FXML private TableView<EmployeePosition> positionsTable;

    // Колонки таблицы positionsTable
    @FXML private TableColumn<EmployeePosition, String> positionNameCol;

    // Кнопки
    @FXML private Button addBtn;
    @FXML private Button deleteBtn;
    @FXML private Button editBtn;
    @FXML private Button resetCollectionsBtn;
    @FXML private Button resetFundsBtn;
    @FXML private Button resetPositionsBtn;

    // Элементы поиска
    @FXML private TextField collectionSearchField;
    @FXML private TextField fundSearchField;
    @FXML private TextField positionSearchField;

    //Другие элементы
    @FXML private BorderPane rootPane;
    @FXML private VBox fundInfoPanel;
    @FXML private Label selectedFundName;
    @FXML private Label selectedFundDesc;
    @FXML private Label totalCountCollections;
    @FXML private Label totalCountFunds;
    @FXML private Label totalCountPositions;

    private FilteredList<Fund> filteredFunds;
    private FilteredList<Collection> filteredCollections;
    private FilteredList<EmployeePosition> filteredPositions;
    private Fund selectedFund;

    Service service = new Service();

    public void initialize() throws SQLException {
        setupTableColumns();
        loadTables();
        setupListeners();
        setupButtonHandlers();

    }

    private void setupTableColumns() {
        // Таблица фондов
        fundNameCol.setCellValueFactory(cell -> cell.getValue().fundNameProperty());
        kpPrefixCol.setCellValueFactory(cell -> cell.getValue().kpPrefixProperty());

        // Таблица коллекций
        collectionNameCol.setCellValueFactory(cell -> cell.getValue().collectionNameProperty());
        collectionFundCol.setCellValueFactory(cell -> cell.getValue().fundNameProperty());
        collectionDescriptionCol.setCellValueFactory(cell -> cell.getValue().collectionDescriptionProperty());

        // Таблица должностей
        positionNameCol.setCellValueFactory(cell -> cell.getValue().positionNameProperty());
    }

    private void loadTables() throws SQLException {
        loadFunds();
        loadCollections();
        loadPositions();
    }

    private void loadFunds() throws SQLException {
        FundRepository.loadAll();
        filteredFunds = new FilteredList<>(
                FundRepository.getFunds(),
                p -> true
        );
        SortedList<Fund> sortedList = new SortedList<>(filteredFunds);
        sortedList.comparatorProperty().bind(fundsTable.comparatorProperty());
        fundsTable.setItems(sortedList);
        updateFundsCount();
    }

    private void loadCollections() throws SQLException {
        CollectionRepository.loadAll();
        filteredCollections = new FilteredList<>(
                CollectionRepository.getCollections(),
                p -> true
        );
        SortedList<Collection> sortedList = new SortedList<>(filteredCollections);
        sortedList.comparatorProperty().bind(collectionsTable.comparatorProperty());
        collectionsTable.setItems(sortedList);
        updateCollectionsCount();
    }

    private void loadPositions() throws SQLException {
        EmployeePositionRepository.loadAll();
        filteredPositions = new FilteredList<>(
                EmployeePositionRepository.getPositions(),
                p -> true
        );
        SortedList<EmployeePosition> sortedList = new SortedList<>(filteredPositions);
        sortedList.comparatorProperty().bind(positionsTable.comparatorProperty());
        positionsTable.setItems(sortedList);
        updatePositionsCount();
    }

    private void setupListeners() {
        fundsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null) return;
            selectedFund = newValue;
            displaySelectedFundInfo(selectedFund);
            filterCollectionsByFund(selectedFund);
            if (positionsTable.getSelectionModel().getSelectedItem() != null) {
                clearPositionSelection();
            }
        });
        collectionsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null) return;
            if (positionsTable.getSelectionModel().getSelectedItem() != null) {
                clearPositionSelection();
            }
        });
        positionsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null) return;
            if (fundsTable.getSelectionModel().getSelectedItem() != null) {
                clearFundSelection();
            }
            if (collectionsTable.getSelectionModel().getSelectedItem() != null) {
                clearCollectionSelection();
            }
        });

        fundSearchField.textProperty().addListener((obs, oldVal, newVal) -> applyFundSearchFilter(newVal));
        collectionSearchField.textProperty().addListener((obs, oldVal, newVal) -> applyCollectionSearchFilter(newVal));
        positionSearchField.textProperty().addListener((obs, oldVal, newVal) -> applyPositionSearchFilter(newVal));
    }

    private void clearCollectionSelection() {
        collectionsTable.getSelectionModel().clearSelection();
        Service.setCollection(null);
    }

    private void clearPositionSelection() {
        positionsTable.getSelectionModel().clearSelection();
        Service.setEmployeePosition(null);
    }

    private void clearFundSelection() {
        fundsTable.getSelectionModel().clearSelection();
        Service.setFund(null);
    }

    private void applyFundSearchFilter(String searchText) {
        String lowerCaseFilter = searchText.toLowerCase().trim();
        filteredFunds.setPredicate(fund -> {
            String fundName = fund.getFundName() == null ? "" : fund.getFundName().toLowerCase();
            String fundDesc = fund.getFundDescription() == null ? "" : fund.getFundDescription().toLowerCase();

            if (!fundName.contains(lowerCaseFilter) && !fundDesc.contains(lowerCaseFilter)) {
                return false;
            }
            return true;
        });
        updateFundsCount();
    }

    private void applyPositionSearchFilter(String searchText) {
        String lowerCaseFilter = searchText.toLowerCase().trim();

        filteredPositions.setPredicate(position -> {

            String positionName =
                    position.getPositionName() == null
                            ? ""
                            : position.getPositionName().toLowerCase();

            return positionName.contains(lowerCaseFilter);
        });

        updatePositionsCount();
    }

    private void applyCollectionSearchFilter(String searchText) {
        applyCombinedFilter();
    }

    private void applyCombinedFilter() {
        filteredCollections.setPredicate(collection -> {
            if (selectedFund != null) {
                if (collection.getFundName() == null ||
                        !collection.getFundName().equals(selectedFund.getFundName())) {
                    return false;
                }
            }
            String searchText = collectionSearchField.getText();
            if (searchText != null && !searchText.isEmpty()) {
                String lowerCaseFilter = searchText.toLowerCase().trim();
                String collectionName = collection.getCollectionName() == null ? "" : collection.getCollectionName().toLowerCase();
                String collectionDesc = collection.getCollectionDescription() == null ? "" : collection.getCollectionDescription().toLowerCase();

                if (!collectionName.contains(lowerCaseFilter) && !collectionDesc.contains(lowerCaseFilter)) {
                    return false;
                }
            }
            return true;
        });
        updateCollectionsCount();
    }

    private void setupButtonHandlers() {
        resetFundsBtn.setOnAction(actionEvent -> {
            try {
                loadFunds();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        resetCollectionsBtn.setOnAction(actionEvent -> {
            try {
                loadCollections();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        resetPositionsBtn.setOnAction(actionEvent -> {
            try {
                loadPositions();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        addBtn.setOnAction(actionEvent -> {
            try {
                Service.openModal("ReferenceBooksRegistrationForm", "Добавление записи в справочник",
                        (Stage) addBtn.getScene().getWindow());
                updateFundsCount();
                updateCollectionsCount();
                updatePositionsCount();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        editBtn.setOnAction(actionEvent -> {
            Fund selectedFund = fundsTable.getSelectionModel().getSelectedItem();
            Collection selectedCollection = collectionsTable.getSelectionModel().getSelectedItem();
            EmployeePosition selectedPosition = positionsTable.getSelectionModel().getSelectedItem();
            if (selectedFund != null || selectedCollection != null || selectedPosition != null) {
                Service.setFund(selectedFund);
                Service.setCollection(selectedCollection);
                Service.setEmployeePosition(selectedPosition);
                try {
                    Service.openModal("ReferenceBooksRegistrationForm", "Редактирование справочника",
                            (Stage) editBtn.getScene().getWindow());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                service.openAlert(Alert.AlertType.WARNING,
                        "Выберите фонд, коллекцию или должность сотрудника из таблиц для редактирования", "Предупреждение!");
            }
        });
        deleteBtn.setOnAction(actionEvent -> {
            Fund selectedFund = fundsTable.getSelectionModel().getSelectedItem();
            Collection selectedCollection = collectionsTable.getSelectionModel().getSelectedItem();
            EmployeePosition selectedPosition = positionsTable.getSelectionModel().getSelectedItem();

            if (selectedFund == null && selectedCollection == null && selectedPosition == null) {
                service.openAlert(Alert.AlertType.WARNING,
                        "Выберите фонд, коллекцию или должность для удаления",
                        "Предупреждение!");
                return;
            }

            boolean isFund = selectedFund != null;
            boolean isCollection = selectedCollection != null;

            int itemId;
            String name;
            String type;

            if (isFund) {
                itemId = selectedFund.getId();
                name = selectedFund.getFundName();
                type = "фонд";

            } else if (isCollection) {
                itemId = selectedCollection.getId();
                name = selectedCollection.getCollectionName();
                type = "коллекцию";

            } else {
                itemId = selectedPosition.getId();
                name = selectedPosition.getPositionName();
                type = "должность";
            }

            Alert confirm = new Alert(
                    Alert.AlertType.CONFIRMATION,
                    "Удалить " + type + ":\n" + name + "?",
                    ButtonType.YES,
                    ButtonType.NO
            );
            confirm.setTitle("Подтверждение удаления");

            if (confirm.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
                return;
            }

            try {
                boolean deleted;
                String successMsg;
                String errorMsg;

                if (isFund) {

                    deleted = FundRepository.deleteFund(itemId);
                    successMsg = "Фонд успешно удалён";
                    errorMsg = "Ошибка удаления фонда";
                    FundRepository.getFunds().remove(selectedFund);

                } else if (isCollection) {

                    deleted = CollectionRepository.deleteCollection(itemId);
                    successMsg = "Коллекция успешно удалена";
                    errorMsg = "Ошибка удаления коллекции";
                    CollectionRepository.getCollections().remove(selectedCollection);

                } else {

                    deleted = EmployeePositionRepository.deletePosition(itemId);
                    successMsg = "Должность успешно удалена";
                    errorMsg = "Ошибка удаления должности";
                    EmployeePositionRepository.getPositions().remove(selectedPosition);
                }

                if (deleted) {
                    service.openAlert(Alert.AlertType.INFORMATION, successMsg, "Успех");
                    updateFundsCount();
                    updateCollectionsCount();
                    updatePositionsCount();
                } else {
                    service.openAlert(Alert.AlertType.WARNING, errorMsg, "Ошибка удаления");
                }
            } catch (SQLException ex) {

                if ("23503".equals(ex.getSQLState())) {
                    service.openAlert(Alert.AlertType.WARNING,
                            "Невозможно удалить элемент: он связан с другими записями в базе данных.",
                            "Ошибка удаления");
                } else {
                    service.openAlert(Alert.AlertType.ERROR,
                            "Ошибка базы данных: " + ex.getMessage(),
                            "Ошибка");
                }
            }
        });
    }


    private void displaySelectedFundInfo(Fund fund) {
        selectedFundName.setText(fund.fundNameProperty().get());
        selectedFundDesc.setText(fund.fundDescriptionProperty().get());
    }

    private void clearFundInfo() {
        selectedFundName.setText("Фонд не выбран");
        selectedFundDesc.setText("Выберите фонд для просмотра коллекций по фонду");
    }

    private void filterCollectionsByFund(Fund fund) {
        selectedFund = fund;
        applyCombinedFilter();
    }

    private void resetCollectionsFilter() {
        filteredCollections.setPredicate(p -> true);
        updateCollectionsCount();
    }

    private void updateCollectionsCount() {
        totalCountCollections.setText("Всего: " + filteredCollections.size());
    }

    private void updateFundsCount() {
        totalCountFunds.setText("Всего: " + filteredFunds.size());
    }

    private void updatePositionsCount() {
        totalCountPositions.setText("Всего: " + filteredPositions.size());
    }
}