package com.example.museumcatalog.Controllers;

import com.example.museumcatalog.DateTimeUtil;
import com.example.museumcatalog.Models.*;
import com.example.museumcatalog.Service;
import com.example.museumcatalog.Storages.*;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class UniversalDocumentFormController {

    // Основные поля
    @FXML private Label docNumberLabel, statusLabel, docDateLabel, conductedDateLabel, changeDateLabel;
    @FXML private ComboBox<String> docTypeCombo, efzkFundCombo, efzkCollectionCombo, loanRecipientType;
    @FXML private ComboBox<Employee> transferFromEmployee, transferToEmployee;
    @FXML private DatePicker efzkPeriodFrom, efzkPeriodTo;
    @FXML private TextField transferPurpose, loanRecipientName, loanRecipientIdentifier, loanRecipientAddress, loanPurpose, ownerSearch, exhibitSearch, employeeSearch;

    // Блоки
    @FXML private VBox efzkBlock, internalTransferBlock, loanBlock, ownerBlock, exhibitsBlock, staffBlock, basisBlock;

    // Таблицы
    @FXML private TableView<Owner> ownersTable;
    @FXML private TableColumn<Owner, Boolean> selectOwnerColumn;
    @FXML private TableColumn<Owner, String> ownerFioColumn, ownerPassportColumn, ownerPhoneColumn;

    @FXML private TableView<Exhibit> exhibitsTable;
    @FXML private TableColumn<Exhibit, Boolean> selectExhibitColumn;
    @FXML private TableColumn<Exhibit, String> exhibitNameColumn, exhibitStatusColumn, exhibitNumberKPColumn;
    @FXML private TableColumn<Exhibit, LocalDate> exhibitDateColumn;

    @FXML private TableView<Employee> employeesTable;
    @FXML private TableColumn<Employee, Boolean> selectEmployeeColumn;
    @FXML private TableColumn<Employee, String> employeeFioColumn, employeePositionColumn;

    @FXML private Label selectedExhibitsCount, selectedEmployeesCount, formTitle;
    @FXML private Button cancelBtn, saveDraftBtn, completeBtn;

    private final ObservableList<Exhibit> exhibitsList = FXCollections.observableArrayList();
    private final ObservableList<Employee> employeesList = FXCollections.observableArrayList();
    private FilteredList<Owner> filteredOwners;
    private FilteredList<Exhibit> filteredExhibit;
    private FilteredList<Employee> filteredEmployee;
    private Document currentDocument = Service.getEditingDocument();
    private Owner currentSelectedOwner = null;
    private boolean isInitializing = false;
    private final Service service = new Service();

    private static final Map<String, String> DOC_TYPE_TO_STATUS = Map.of(
            "Акт ПП на ВХ", "В обработке",
            "Акт на рассмотрение ЭФЗК", "На временном хранении",
            "Протокол заседания ЭФЗК", "На рассмотрении ЭФЗК",
            "Акт ПП на ПП", "Принят ЭФЗК",
            "Договор пожертвования", "Принят ЭФЗК",
            "Акт ПП на ОХ", "Принят ЭФЗК",
            "Акт внутримузейной передачи", "В фонде",
            "Акт ВП на временное хранение", "В фонде"
    );

    private static final List<String> STRICT_CONDUCTED_CHECK_TYPES = List.of(
            "Акт ПП на ПП",
            "Договор пожертвования",
            "Акт ПП на ОХ"
    );

    private static final Set<String> FUND_REQUIRED_DOCS = Set.of(
            "Акт ПП на ПП",
            "Акт ПП на ОХ"
    );

    private static final Map<String, String> DOC_TYPE_TO_NEW_STATUS = Map.of(
            "Акт ПП на ВХ", "На временном хранении",
            "Акт на рассмотрение ЭФЗК", "На рассмотрении ЭФЗК",
            "Протокол заседания ЭФЗК", "Принят ЭФЗК",
            "Акт ПП на ПП", "Принят ЭФЗК",
            "Договор пожертвования", "Принят ЭФЗК",
            "Акт ПП на ОХ", "Принят ЭФЗК",
            "Акт внутримузейной передачи", "На выставке",
            "Акт ВП на временное хранение", "Выдан"
    );

    private static final Map<String, String> DOC_TYPE_CODES = Map.of(
            "Акт ПП на ВХ", "АВХ",
            "Акт на рассмотрение ЭФЗК", "АРЭФЗК",
            "Протокол заседания ЭФЗК", "ПЭФЗК",
            "Акт ПП на ПП", "АПП",
            "Договор пожертвования", "ДП",
            "Акт ПП на ОХ", "АОХ",
            "Акт внутримузейной передачи", "АВП",
            "Акт ВП на временное хранение", "АВПВХ"
    );


    @FunctionalInterface
    private interface SQLRunnable { void run() throws SQLException; }

    public void initialize() throws Exception {
        hideAllBlocks();
        initTables();
        filteredOwners.forEach(owner ->owner.setSelected(false));
        filteredEmployee.forEach(employee -> employee.setSelected(false));
        filteredExhibit.forEach(exhibit -> exhibit.setSelected(false));
        initComboBoxes();
        initListeners();
        initButtons();
        currentSelectedOwner = null;
        filteredExhibit.setPredicate(filterExhibitsByOwner(currentSelectedOwner));
        isInitializing = true;
        if (Service.getEditingDocument() != null) {
            formTitle.setText("Редактирование документа");
            Document doc = Service.getEditingDocument();
            updateFormByType(doc.getDocType());
            loadDocument(doc);
        }
        isInitializing = false;
    }

    private void loadDocument(Document doc) throws SQLException {
        if (doc.getDocStatus().equals("Проведен")){
            formTitle.setText("Просмотр документа");
            statusLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #4CAF50;");
            saveDraftBtn.setDisable(true);
            completeBtn.setDisable(true);
        }
        docNumberLabel.setText(doc.getDocNumber() != null ? doc.getDocNumber() : "— — —");
        docTypeCombo.setValue(doc.getDocType());
        docTypeCombo.setDisable(true);
        statusLabel.setText(doc.getDocStatus());
        docDateLabel.setText(DateTimeUtil.formatForDisplay(doc.getDocDate()));
        conductedDateLabel.setText(doc.getConductedDate() != null ? DateTimeUtil.formatForDisplay(doc.getConductedDate()) : "не проведён");
        changeDateLabel.setText(DateTimeUtil.formatForDisplay(doc.getChangeDate()));

        DocumentRelationsRepository.loadAllRelations(doc.getId(), employeesList, exhibitsList);
        System.out.println(employeesList);
        for (Employee item: EmployeeRepository.getActiveEmployees()) {
            for (Employee itemTarget: employeesList) {
                if (item.getId() == itemTarget.getId()) {
                    item.setSelected(true);
                    break;
                }
            }
        }
        selectedEmployeesCount.setText("Выбрано сотрудников: " + getSelectedEmployeesCount());
        for (Exhibit item: ExhibitRepository.getExhibits()) {
            for (Exhibit itemTarget: exhibitsList) {
                if (item.getId() == itemTarget.getId()) {
                    item.setSelected(true);
                    break;
                }
            }
        }
        selectedExhibitsCount.setText("Выбрано предметов: " + getSelectedExhibitsCount());
        switch (doc.getDocType()) {

            case "Протокол заседания ЭФЗК" -> {
                List<EfzkData> efzkList = DocumentTypeDetailsRepository.getEfzk(doc.getId());
                if (!efzkList.isEmpty()) {
                    EfzkData data = efzkList.getFirst();
                    efzkPeriodFrom.setValue(data.getStartDate());
                    efzkPeriodTo.setValue(data.getEndDate());
                    efzkFundCombo.setValue(data.getFundName());
                    efzkCollectionCombo.setValue(data.getCollectionName());
                }
            }

            case "Акт внутримузейной передачи" -> {
                List<InternalTransferData> internalList = DocumentTypeDetailsRepository.getInternalTransfer(doc.getId());
                if (!internalList.isEmpty()) {
                    InternalTransferData data = internalList.getFirst();
                    transferFromEmployee.setValue(filteredEmployee.stream()
                            .filter(emp -> emp.getId() == data.getFromEmployeeId())
                            .findFirst()
                            .orElse(null));

                    transferToEmployee.setValue(filteredEmployee.stream()
                            .filter(emp -> emp.getId() == data.getToEmployeeId())
                            .findFirst()
                            .orElse(null));
                    transferPurpose.setText(data.getTransferPurpose());
                }
            }

            case "Акт ВП на временное хранение" -> {
                List<TemporaryStorageData> storageList = DocumentTypeDetailsRepository.getTemporaryStorage(doc.getId());
                if (!storageList.isEmpty()) {
                    TemporaryStorageData data = storageList.getFirst();
                    loanRecipientType.setValue(data.getReceiverType());
                    loanRecipientName.setText(data.getReceiverName());
                    loanRecipientIdentifier.setText(data.getReceiverIdentifier());
                    loanRecipientAddress.setText(data.getReceiverAddress());
                    loanPurpose.setText(data.getAdmissionPurpose());
                }
            }
            default -> {
                if (doc.getOwnerId() != null) {
                    for (Owner item : OwnerRepository.getOwners()) {
                        if (item.getId() == doc.getOwnerId()) {
                            item.setSelected(true);
                            break;
                        }
                    }
                }
            }
        }
    }

    private <T> FilteredList<T> initTable(TableView<T> table, ObservableList<T> list, SQLRunnable loadAction) {
        table.setEditable(true);
        if (list.isEmpty() && loadAction != null) {
            try { loadAction.run(); } catch (SQLException e) { throw new RuntimeException(e); }
        }
        FilteredList<T> filtered = new FilteredList<>(list, p -> true);
        table.setItems(filtered);
        return filtered;
    }

    private void initTables() throws SQLException {
        filteredOwners = initTable(ownersTable, OwnerRepository.getOwners(), OwnerRepository::loadAll);
        setupOwnersTable();
        filteredExhibit = initTable(exhibitsTable, ExhibitRepository.getExhibits(), ExhibitRepository::loadAll);
        setupExhibitsTable();
        filteredEmployee = initTable(employeesTable, EmployeeRepository.getActiveEmployees(), EmployeeRepository::loadActive);
        setupEmployeeTable();
    }

    private void setupOwnersTable() {
        selectOwnerColumn.setCellValueFactory(cell -> cell.getValue().selectedProperty());
        selectOwnerColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectOwnerColumn));
        ownerFioColumn.setCellValueFactory(cell -> Bindings.createStringBinding(
                () -> cell.getValue().getFullFio(),
                cell.getValue().lastNameProperty(), cell.getValue().firstNameProperty(), cell.getValue().middleNameProperty()));
        ownerPassportColumn.setCellValueFactory(cell -> Bindings.createStringBinding(
                () -> cell.getValue().getPassport(),
                cell.getValue().passportSeriesProperty(), cell.getValue().passportNumberProperty()));
        ownerPhoneColumn.setCellValueFactory(cell -> cell.getValue().phoneProperty());
    }

    private void setupExhibitsTable() {
        selectExhibitColumn.setCellValueFactory(cell -> cell.getValue().selectedProperty());
        selectExhibitColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectExhibitColumn));
        exhibitNameColumn.setCellValueFactory(cell -> cell.getValue().nameProperty());
        exhibitDateColumn.setCellValueFactory(cell -> cell.getValue().arrivalDateProperty());
        exhibitStatusColumn.setCellValueFactory(cell -> cell.getValue().statusProperty());
        exhibitNumberKPColumn.setCellValueFactory(cell -> cell.getValue().numberKPProperty());
        exhibitsTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Exhibit item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setDisable(false);
                    setStyle("");
                    return;
                }
                String docType = docTypeCombo.getValue();
                boolean allowed;
                try {
                    allowed = docType != null &&
                            canExhibitBeUsed(item, docType);
                } catch (SQLException e) {
                    allowed = false;
                }
                setDisable(!allowed);
                setStyle(allowed ? "" : "-fx-opacity: 0.4;");
            }
        });
    }

    private void setupEmployeeTable() {
        selectEmployeeColumn.setCellValueFactory(cell -> cell.getValue().selectedProperty());
        selectEmployeeColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectEmployeeColumn));
        employeeFioColumn.setCellValueFactory(cell -> Bindings.createStringBinding(
                () -> cell.getValue().getFullFio(),
                cell.getValue().lastNameProperty(), cell.getValue().firstNameProperty(), cell.getValue().middleNameProperty()));
        employeePositionColumn.setCellValueFactory(cell -> cell.getValue().positionProperty());
    }

    private void initComboBoxes() throws SQLException {
        docTypeCombo.getItems().addAll(service.getValuesComboBox("document_types", "type_name", null));
        efzkFundCombo.getItems().addAll(service.getValuesComboBox("funds", "fund_name", null));
        loanRecipientType.getItems().addAll(service.getValuesComboBox("receiver_types", "type_name", null));
        transferFromEmployee.setItems(filteredEmployee);
        transferToEmployee.setItems(filteredEmployee);
        setupEmployeeComboBox(transferFromEmployee);
        setupEmployeeComboBox(transferToEmployee);
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

    private void initListeners() {
        efzkFundCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            efzkCollectionCombo.getItems().clear();
            try {
                efzkCollectionCombo.getItems().addAll(service.getValuesComboBox(
                        "collections c join funds f on c.fund_id = f.id",
                        "collection_name", "f.fund_name = ?", newVal));
            } catch (SQLException e) { throw new RuntimeException(e); }
        });

        ownersTable.getItems().forEach(owner ->
                owner.selectedProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal) {
                        ownersTable.getItems().forEach(o -> {
                            if (o != owner) {
                                o.setSelected(false);
                            }
                        });
                        currentSelectedOwner = owner;
                        exhibitsTable.refresh();
                    } else {
                        currentSelectedOwner = null;
                    }
                    filteredExhibit.setPredicate(filterExhibitsByOwner(currentSelectedOwner));
                }));

        docTypeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (isInitializing) return;
            exhibitsTable.refresh();
            currentDocument = null;
            resetForm();
            updateFormByType(newVal);
        });

        exhibitsTable.getItems().forEach(exhibit ->
                exhibit.selectedProperty().addListener((obs, oldVal, newVal) -> {
                    selectedExhibitsCount.setText("Выбрано предметов: " + getSelectedExhibitsCount());
                }));

        employeesTable.getItems().forEach(employee ->
                employee.selectedProperty().addListener((obs, oldVal, newVal) -> {
                    selectedEmployeesCount.setText("Выбрано сотрудников: " + getSelectedEmployeesCount());
                }));

        ownerSearch.textProperty().addListener((obs, oldVal, newVal) -> applySearch());
        exhibitSearch.textProperty().addListener((obs, oldVal, newVal) -> applySearch());
        employeeSearch.textProperty().addListener((obs, oldVal, newVal) -> applySearch());

    }

    private Predicate<Exhibit> filterExhibitsByOwner(Owner owner) {
        return exhibit -> {
            if (owner != null) {
                return exhibit.getOwnerId() == owner.getId();
            } else {
                Integer ownerId = exhibit.getOwnerId();
                return ownerId == null || ownerId == 0;
            }
        };
    }

    private void applySearch() {
        //Поиск по владельцу (ФИО или паспорт)
        filteredOwners.setPredicate(owner -> {
            String text = ownerSearch.getText();
            if (text != null && !text.isEmpty()) {
                String t = text.toLowerCase();
                String fio = owner.getFullFio() == null ? "" : owner.getFullFio().toLowerCase();
                String passport = owner.getPassport() == null ? "" : owner.getPassport().toLowerCase();
                if (!fio.contains(t) && !passport.contains(t)) {
                    return false;
                }
            }
            return true;
        });
        //Поиск по предмету (название или номер КП)
        filteredExhibit.setPredicate(exhibit -> {
            String text = exhibitSearch.getText();
            if (text != null && !text.isEmpty()) {
                String t = text.toLowerCase();
                String name = exhibit.getName() == null ? "" : exhibit.getName().toLowerCase();
                String kp = exhibit.getNumberKP() == null ? "" : exhibit.getNumberKP().toLowerCase();
                if (!name.contains(t) && !kp.contains(t)) {
                    return false;
                }
            }
            return true;
        });
        //Поиск по сотруднику (ФИО или должность)
        filteredEmployee.setPredicate(employee -> {
            String text = employeeSearch.getText();
            if (text != null && !text.isEmpty()) {
                String t = text.toLowerCase();
                String fio = employee.getFullFio() == null ? "" : employee.getFullFio().toLowerCase();
                String position = employee.getPosition() == null ? "" : employee.getPosition().toLowerCase();
                if (!fio.contains(t) && !position.contains(t)) {
                    return false;
                }
            }
            return true;
        });
    }

    private void initButtons() {
        cancelBtn.setOnAction(e -> {
            Service.setEditingDocument(null);
            ((Stage) cancelBtn.getScene().getWindow()).close();
        });
        saveDraftBtn.setOnAction(actionEvent -> {
            try {
                saveDocument(false);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        completeBtn.setOnAction(actionEvent -> {
            try {
                saveDocument(true);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void saveDocument(boolean isConducted) throws SQLException {
        if (docTypeCombo.getValue() == null) {
            service.markFieldAsError(docTypeCombo);
            service.openAlert(Alert.AlertType.WARNING,
                    "Выберите тип документа",
                    "Ошибка");
            return;
        }

        if (!validateRequiredFields()) {
            service.openAlert(Alert.AlertType.WARNING,
                    "Заполните обязательные поля",
                    "Ошибка");
            return;
        }

        boolean isUpdate = currentDocument != null;
        String docStatus = isConducted ? "Проведен" : "Черновик";
        LocalDateTime currentDateTime = DateTimeUtil.nowWithSecondPrecision();
        if (!isUpdate){
            currentDocument = new Document();
            currentDocument.setDocDate(currentDateTime);
            currentDocument.setCreatedBy(Service.getCurrentUser().getShortFio());
            Service.setEditingDocument(currentDocument);
        }

        currentDocument.setDocType(docTypeCombo.getValue());
        currentDocument.setChangeDate(currentDateTime);
        currentDocument.setConductedDate(isConducted ? currentDateTime : null);
        currentDocument.setDocStatus(docStatus);
        currentDocument.setUpdatedBy(Service.getCurrentUser().getShortFio());
        currentDocument.setExhibitsCount(getSelectedExhibitsCount());

        if (isConducted) {
            String docTypeCode = DOC_TYPE_CODES.getOrDefault(docTypeCombo.getValue(), "DOC");
            currentDocument.setDocNumber(DocumentRepository.generateDocNumber(docTypeCode));
        }

        Owner selectedOwner = ownersTable.getItems().stream()
                .filter(Owner::isSelected)
                .findFirst()
                .orElse(null);
        System.out.println("Обьект: " + selectedOwner);
        if (selectedOwner != null) {
            currentDocument.setOwnerId(selectedOwner.getId());
            currentDocument.setOwner(selectedOwner.getFullFio());
        }

        int result = DocumentRepository.addOrEdit(currentDocument, Service.getCurrentUser());

        if (result > 0) {
            if (!isUpdate) {
                currentDocument.setId(result);
                DocumentRepository.getDocuments().add(currentDocument);
            }
            // --- связи ---
            DocumentRelationsRepository.clearRelations(currentDocument.getId());
            DocumentRelationsRepository.saveAll(
                    currentDocument.getId(),
                    employeesTable.getItems().stream()
                            .filter(Employee::isSelected)
                            .toList(),
                    exhibitsTable.getItems().stream()
                            .filter(Exhibit::isSelected)
                            .toList()
            );

            // --- специфичные поля ---
            DocumentTypeDetailsRepository.clearRelations(currentDocument.getId());
            switch (docTypeCombo.getValue()) {

                case "Протокол заседания ЭФЗК" -> {
                    DocumentTypeDetailsRepository.saveEfzk(
                            currentDocument.getId(),
                            efzkPeriodFrom.getValue(),
                            efzkPeriodTo.getValue(),
                            efzkFundCombo.getValue(),
                            efzkCollectionCombo.getValue()
                    );
                }

                case "Акт внутримузейной передачи" -> {
                    DocumentTypeDetailsRepository.saveInternalTransfer(
                            currentDocument.getId(),
                            transferFromEmployee.getValue().getId(),
                            transferToEmployee.getValue().getId(),
                            transferPurpose.getText()
                    );
                }

                case "Акт ВП на временное хранение" -> {
                    DocumentTypeDetailsRepository.saveTemporaryStorage(
                            currentDocument.getId(),
                            loanRecipientType.getValue(),
                            loanRecipientName.getText(),
                            loanRecipientIdentifier.getText(),
                            loanRecipientAddress.getText(),
                            loanPurpose.getText()
                    );
                }
            }
            service.openAlert(Alert.AlertType.INFORMATION,
                    isUpdate ? "Данные обновлены" : "Документ добавлен",
                    "Успешно!");
        } else {
            service.openAlert(Alert.AlertType.ERROR,
                    "Ошибка при сохранении документа",
                    "Неуспешно!");
        }

        if (currentDocument.getDocNumber() != null) {
            docNumberLabel.setText(currentDocument.getDocNumber());
        } else {
            docNumberLabel.setText("— — —");
        }
        statusLabel.setText(currentDocument.getDocStatus());
        docDateLabel.setText(DateTimeUtil.formatForDisplay(currentDocument.getDocDate()));
        changeDateLabel.setText(DateTimeUtil.formatForDisplay(currentDocument.getChangeDate()));
        if (isConducted) {
            for (Exhibit ex : exhibitsTable.getItems()) {
                if (ex.isSelected()) {
                    String newStatus = resolveExhibitStatus(
                            currentDocument.getDocType(),
                            ex.getId()
                    );
                    if (newStatus != null) {
                        ex.setStatus(newStatus);
                        ExhibitRepository.updateStatus(ex.getId(), newStatus);
                    }
                    if (currentDocument.getDocType().equals("Акт ПП на ПП")) {
                        EfzkData efzkData = DocumentTypeDetailsRepository.getLastEfzkForExhibit(ex.getId());

                        if (efzkData != null) {
                            String kpNumber = ExhibitRepository.generateKPNumber(
                                    efzkData.getFundName()
                            );
                            ex.setNumberKP(kpNumber);
                            ex.setFund(efzkData.getFundName());
                            ex.setCollection(efzkData.getCollectionName());
                            ExhibitRepository.updateFundData(
                                    ex.getId(),
                                    efzkData.getFundName(),
                                    efzkData.getCollectionName(),
                                    kpNumber
                            );
                        }
                    }
                }
            }
            conductedDateLabel.setText(DateTimeUtil.formatForDisplay(currentDocument.getConductedDate()));
            statusLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #4CAF50;");
            saveDraftBtn.setDisable(true);
            completeBtn.setDisable(true);
        } else {
            conductedDateLabel.setText("не проведён");
        }

    }

    private void toggleBlock(VBox block, boolean visible) {
        block.setVisible(visible);
        block.setManaged(visible);
    }

    private void hideAllBlocks() {
        for (VBox block : new VBox[]{efzkBlock, internalTransferBlock, loanBlock, ownerBlock, exhibitsBlock, staffBlock, basisBlock}) {
            toggleBlock(block, false);
        }
    }

    public static boolean canExhibitBeUsed(Exhibit exhibit, String docType) throws SQLException {
        if (exhibit == null || docType == null) return false;
        String exhibitStatus = exhibit.getStatus();

        String requiredStatus = DOC_TYPE_TO_STATUS.get(docType);

        if (requiredStatus != null && !requiredStatus.equals(exhibitStatus)) {
            return false;
        }

        if (STRICT_CONDUCTED_CHECK_TYPES.contains(docType)) {
            boolean alreadyUsed = DocumentRepository.hasConductedDocumentForExhibit(
                    exhibit.getId(),
                    docType
            );

            if (alreadyUsed) {
                return false;
            }
        }
        return true;
    }

    public static String resolveExhibitStatus(String docType, int exhibitId) throws SQLException {
        if (docType == null) return null;
        String baseStatus = DOC_TYPE_TO_NEW_STATUS.get(docType);
        if (baseStatus == null) return null;
        boolean canBecomeFund = FUND_REQUIRED_DOCS.contains(docType);
        if (!canBecomeFund) {
            return baseStatus;
        }
        for (String requiredDoc : FUND_REQUIRED_DOCS) {
            boolean exists = DocumentRepository.hasConductedDocumentForExhibit(
                    exhibitId,
                    requiredDoc
            );
            if (!exists) {
                return baseStatus;
            }
        }
        return "В фонде";
    }

    private void updateFormByType(String type) {
        hideAllBlocks();
        if (type == null) return;

        switch (type) {
            case "Протокол заседания ЭФЗК" -> {
                toggleBlock(efzkBlock, true);
                toggleBlock(exhibitsBlock, true);
                toggleBlock(staffBlock, true);
                filteredExhibit.setPredicate(p -> true);
            }
            case "Акт внутримузейной передачи" -> {
                toggleBlock(internalTransferBlock, true);
                toggleBlock(exhibitsBlock, true);
                toggleBlock(staffBlock, true);
                filteredExhibit.setPredicate(p -> true);
            }
            case "Акт ВП на временное хранение" -> {
                toggleBlock(loanBlock, true);
                toggleBlock(exhibitsBlock, true);
                toggleBlock(staffBlock, true);
                filteredExhibit.setPredicate(p -> true);
            }
            default -> {
                toggleBlock(ownerBlock, true);
                toggleBlock(exhibitsBlock, true);
                toggleBlock(basisBlock, true);
                toggleBlock(staffBlock, true);
            }
        }
    }

    private void resetForm() {
        currentDocument = null;
        saveDraftBtn.setDisable(false);
        completeBtn.setDisable(false);

        // Очистка полей
        transferPurpose.clear(); loanRecipientName.clear(); loanRecipientIdentifier.clear();
        loanRecipientAddress.clear(); loanPurpose.clear();
        efzkFundCombo.setValue(null); efzkCollectionCombo.setValue(null);
        transferFromEmployee.setValue(null); transferToEmployee.setValue(null);
        loanRecipientType.setValue(null);
        efzkPeriodFrom.setValue(null); efzkPeriodTo.setValue(null);

        // Очистка таблиц
        employeesTable.getItems().forEach(e -> e.selectedProperty().set(false));
        exhibitsTable.getItems().forEach(ex -> ex.selectedProperty().set(false));
        ownersTable.getItems().forEach(o -> o.selectedProperty().set(false));
        employeesTable.refresh(); exhibitsTable.refresh(); ownersTable.refresh();
        service.clearAllErrorStyles(cancelBtn.getScene().getRoot());

        // Сброс лейблов
        docNumberLabel.setText("— — —");
        docDateLabel.setText("— — —");
        conductedDateLabel.setText("не проведён");
        changeDateLabel.setText("— — —");
        statusLabel.setText("Черновик");
        statusLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #FF9800");
    }

    private boolean validateRequiredFields() {
        boolean allFilled = true;
        String type = docTypeCombo.getValue();

        boolean hasExhibits = exhibitsTable.getItems().stream()
                .anyMatch(Exhibit::isSelected);

        if (!hasExhibits) {service.markFieldAsError(exhibitsTable); allFilled = false;}

        boolean hasEmployees = employeesTable.getItems().stream()
                .anyMatch(Employee::isSelected);

        if (!hasEmployees) {service.markFieldAsError(employeesTable); allFilled = false;}

        switch (type) {
            case "Протокол заседания ЭФЗК" -> {
                if (efzkPeriodFrom.getValue() == null) {service.markFieldAsError(efzkPeriodFrom); allFilled = false;}
                if (efzkPeriodTo.getValue() == null) {service.markFieldAsError(efzkPeriodTo); allFilled = false;}
                if (efzkFundCombo.getValue() == null) {service.markFieldAsError(efzkFundCombo); allFilled = false;}
                if (efzkCollectionCombo.getValue() == null) {service.markFieldAsError(efzkCollectionCombo); allFilled = false;}
            }
            case "Акт внутримузейной передачи" -> {
                if (transferFromEmployee.getValue() == null) {service.markFieldAsError(transferFromEmployee); allFilled = false;}
                if (transferToEmployee.getValue() == null) {service.markFieldAsError(transferToEmployee); allFilled = false;}
                if (isBlank(transferPurpose)) {service.markFieldAsError(transferPurpose); allFilled = false;}
            }
            case "Акт ВП на временное хранение" -> {
                if (loanRecipientType.getValue() == null) {service.markFieldAsError(loanRecipientType); allFilled = false;}
                if (isBlank(loanRecipientName)) {service.markFieldAsError(loanRecipientName); allFilled = false;}
                if (isBlank(loanRecipientIdentifier)) {service.markFieldAsError(loanRecipientIdentifier); allFilled = false;}
                if (isBlank(loanRecipientAddress)) {service.markFieldAsError(loanRecipientAddress); allFilled = false;}
                if (isBlank(loanPurpose)) {service.markFieldAsError(loanPurpose); allFilled = false;}
            }
        }
        return allFilled;
    }

    private boolean isBlank(TextField field) {
        return field.getText() == null || field.getText().trim().isEmpty();
    }

    private int getSelectedExhibitsCount() {
        return (int) ExhibitRepository.getExhibits().stream().filter(Exhibit::isSelected).count();
    }
    private int getSelectedEmployeesCount() {
        return (int) EmployeeRepository.getActiveEmployees().stream().filter(Employee::isSelected).count();
    }

}