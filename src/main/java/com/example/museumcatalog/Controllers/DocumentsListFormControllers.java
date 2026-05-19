package com.example.museumcatalog.Controllers;

import com.example.museumcatalog.DocumentExportService;
import com.example.museumcatalog.Models.Document;
import com.example.museumcatalog.Models.Exhibit;
import com.example.museumcatalog.Models.FullDocumentData;
import com.example.museumcatalog.Models.Owner;
import com.example.museumcatalog.Service;
import com.example.museumcatalog.Storages.DocumentRepository;
import com.example.museumcatalog.Storages.ExhibitRepository;
import com.example.museumcatalog.Storages.OwnerRepository;
import com.example.museumcatalog.WordTemplateService;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DocumentsListFormControllers {

    //Центральная таблица documentsTable
    @FXML private TableView<Document> documentsTable;

    //Колонки таблицы documentsTable
    @FXML private TableColumn<Document, String> docNumberColumn;
    @FXML private TableColumn<Document, String> docTypeColumn;
    @FXML private TableColumn<Document, LocalDateTime> docDateColumn;
    @FXML private TableColumn<Document, String> ownerColumn;
    @FXML private TableColumn<Document, Integer> exhibitsCountColumn;
    @FXML private TableColumn<Document, String> docStatusColumn;
    @FXML private TableColumn<Document, LocalDateTime> conductedDateColumn;
    @FXML private TableColumn<Document, String> createdByColumn;
    @FXML private TableColumn<Document, String> updatedByColumn;

    //Элементы поиска/фильтрации
    @FXML private TextField search;
    @FXML private TextField ownerSearch;
    @FXML private TextField exhibitSearch;
    @FXML private ComboBox<String> filterDocStatus;
    @FXML private ComboBox<String> filterDocType;
    @FXML private DatePicker dateFrom;
    @FXML private DatePicker dateTo;

    //Кнопки
    @FXML private Button createDocBtn;
    @FXML private Button editDocBtn;
    @FXML private Button deleteDocBtn;
    @FXML private Button exportBtn;

    //Другие элементы
    @FXML private Label totalCountLabel;

    Service service = new Service();

    private FilteredList<Document> filteredDocuments;

    public void initialize() throws SQLException {
        setupTableColumns();
        loadDocuments();
        setupListeners();
        setupButtonHandlers();
    }

    private void setupTableColumns() {
        docNumberColumn.setCellValueFactory(cell -> cell.getValue().docNumberProperty());
        docTypeColumn.setCellValueFactory(cell -> cell.getValue().docTypeProperty());
        ownerColumn.setCellValueFactory(cell -> cell.getValue().ownerProperty());
        exhibitsCountColumn.setCellValueFactory(cell -> cell.getValue().exhibitsCountProperty());
        docStatusColumn.setCellValueFactory(cell -> cell.getValue().docStatusProperty());
        createdByColumn.setCellValueFactory(cell -> cell.getValue().createdByProperty());
        updatedByColumn.setCellValueFactory(cell -> cell.getValue().updatedByProperty());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        docDateColumn.setCellValueFactory(cell -> cell.getValue().docDateProperty());
        docDateColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(formatter));
                }
            }
        });

        conductedDateColumn.setCellValueFactory(cell -> cell.getValue().conductedDateProperty());
        conductedDateColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(formatter));
                }
            }
        });
    }

    private void loadDocuments() throws SQLException {
        DocumentRepository.loadAll();
        filteredDocuments = new FilteredList<>(
                DocumentRepository.getDocuments(),
                p -> true
        );

        SortedList<Document> sortedList = new SortedList<>(filteredDocuments);

        sortedList.comparatorProperty().bind(documentsTable.comparatorProperty());
        documentsTable.setItems(sortedList);

        totalCountLabel.setText("Всего: " + filteredDocuments.size());
    }

    private void setupListeners() {
        search.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        ownerSearch.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        exhibitSearch.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        filterDocStatus.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        filterDocType.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        dateFrom.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        dateTo.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        documentsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldDoc, newDoc) -> updateEditButtonText(newDoc)
        );
    }

    private void applyFilters() {
        filteredDocuments.setPredicate(doc -> {
            //Поиск по номеру документа
            if (search != null && !search.getText().isBlank()) {
                String q = search.getText().toLowerCase();
                if (doc.getDocNumber() == null || !doc.getDocNumber().toLowerCase().contains(q)) {
                    return false;
                }
            }

            //Поиск по владельцу
            if (ownerSearch != null && !ownerSearch.getText().isBlank()) {
                String q = ownerSearch.getText().toLowerCase();
                if (doc.getOwner() == null || !doc.getOwner().toLowerCase().contains(q)) {
                    return false;
                }
            }

            //По количеству предметов
            if (exhibitSearch != null && !exhibitSearch.getText().isBlank()) {
                try {
                    int count = Integer.parseInt(exhibitSearch.getText());
                    if (doc.getExhibitsCount() == null || doc.getExhibitsCount() != count) {
                        return false;
                    }
                } catch (NumberFormatException e) {
                    return false;
                }
            }

            //Фильтр по статусу
            if (filterDocStatus != null && filterDocStatus.getValue() != null
                    && !filterDocStatus.getValue().equals("Все статусы")) {

                if (doc.getDocStatus() == null ||
                        !doc.getDocStatus().equals(filterDocStatus.getValue())) {
                    return false;
                }
            }

            //Фильтр по типу
            if (filterDocType != null && filterDocType.getValue() != null
                    && !filterDocType.getValue().equals("Все типы")) {

                if (doc.getDocType() == null ||
                        !doc.getDocType().equals(filterDocType.getValue())) {
                    return false;
                }
            }

            //Дата "от"
            if (dateFrom != null && dateFrom.getValue() != null) {
                if (doc.getDocDate() == null ||
                        doc.getDocDate().toLocalDate().isBefore(dateFrom.getValue())) {
                    return false;
                }
            }

            //Дата "до"
            if (dateTo != null && dateTo.getValue() != null) {
                if (doc.getDocDate() == null ||
                        doc.getDocDate().toLocalDate().isAfter(dateTo.getValue())) {
                    return false;
                }
            }
            return true;
        });

        totalCountLabel.setText("Всего: " + filteredDocuments.size());
    }

    private void setupButtonHandlers() {
        createDocBtn.setOnAction(actionEvent -> {
            try {
                Service.openModal("UniversalDocumentForm", "Создание документа",
                        (Stage) createDocBtn.getScene().getWindow());
                totalCountLabel.setText("Всего: " + filteredDocuments.size());
                applyFilters();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });


        editDocBtn.setOnAction(actionEvent -> {
            Document selected = documentsTable.getSelectionModel().getSelectedItem();

            if (selected != null) {
                if ("Проведен".equals(selected.getDocStatus())) {
                    editDocBtn.setText("Просмотреть");
                } else {
                    editDocBtn.setText("Редактировать");
                }
            } else {
                service.openAlert(Alert.AlertType.WARNING,
                        "Выберите документ из таблицы для редактирования", "Внимание");
                return;
            }

            // Сохраняем выбранный документ в Service
            Service.setEditingDocument(selected);

            try {
                // Открываем модальное окно
                Service.openModal("UniversalDocumentForm", "Редактирование документа",
                        (Stage) editDocBtn.getScene().getWindow());
                applyFilters();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        deleteDocBtn.setOnAction(actionEvent -> {
            Document selected = documentsTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                service.openAlert(Alert.AlertType.WARNING,
                        "Выберите черновик документа из таблицы для удаления",
                        "Предупреждение!");
                return;
            }
            if ("Проведен".equals(selected.getDocStatus())) {
                service.openAlert(Alert.AlertType.WARNING,
                        "Проведенные документы удалять нельзя",
                        "Предупреждение!");
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Удалить черновик документа?\n",
                    ButtonType.YES, ButtonType.NO);

            confirm.setTitle("Подтверждение удаления");

            if (confirm.showAndWait().get() != ButtonType.YES) return;

            try {
                boolean deleted = DocumentRepository.deleteDocument(selected.getId());

                if (deleted) {
                    DocumentRepository.getDocuments().remove(selected);
                    totalCountLabel.setText("Всего: " + filteredDocuments.size());

                    service.openAlert(Alert.AlertType.INFORMATION,
                            "Черновик документа успешно удалён",
                            "Успех");
                } else {
                    service.openAlert(Alert.AlertType.WARNING,
                            "Черновик документа не найден или уже удалён",
                            "Ошибка");
                }

            } catch (SQLException ex) {
                service.openAlert(Alert.AlertType.ERROR,
                            "Ошибка базы данных: " + ex.getMessage(),
                            "Ошибка");
            }
        });
        exportBtn.setOnAction(e -> {
            try {
                Document selected = documentsTable.getSelectionModel().getSelectedItem();

                FullDocumentData data =
                        DocumentExportService.load(selected);

                Path path = Path.of(
                        "C:/Users/Leonid/Documents/exports/" +
                                data.getDocument().getDocNumber() +
                                ".docx"
                );

                WordTemplateService.export(data, path);

                System.out.println("Документ экспортирован");

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

    }

    private void updateEditButtonText(Document doc) {
        if (doc == null) {
            editDocBtn.setText("Редактировать");
            return;
        }

        if ("Проведен".equals(doc.getDocStatus())) {
            editDocBtn.setText("Просмотреть");
        } else {
            editDocBtn.setText("Редактировать");
        }
    }
}