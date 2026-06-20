package com.example.museumcatalog.Controllers;

import com.example.museumcatalog.Models.Collection;
import com.example.museumcatalog.Models.EmployeePosition;
import com.example.museumcatalog.Models.Fund;
import com.example.museumcatalog.Service;
import com.example.museumcatalog.Storages.CollectionRepository;
import com.example.museumcatalog.Storages.EmployeePositionRepository;
import com.example.museumcatalog.Storages.FundRepository;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

public class ReferenceBooksRegistrationController {
    @FXML private ComboBox<String> referenceTypeCombo;
    @FXML private VBox fundForm;
    @FXML private VBox collectionForm;
    @FXML private VBox positionForm;
    @FXML private TextField fundNameField;
    @FXML private TextField kpPrefixField;
    @FXML private TextArea fundDescriptionField;
    @FXML private TextField collectionNameField;
    @FXML private ComboBox<String> collectionFundCombo;
    @FXML private TextArea collectionDescriptionField;
    @FXML private TextField positionNameField;
    @FXML private Button saveBtn, cancelBtn;
    @FXML private Label titleLabel;
    private Fund editingFund = Service.getFund();
    private Collection editingCollection = Service.getCollection();
    private EmployeePosition editingPosition = Service.getEmployeePosition();

    Service service = new Service();

    public void initialize() throws SQLException {
        hideAllBlocks();
        initComboBoxes();
        initButtons();
        initListeners();

        if (editingFund != null || editingCollection != null || editingPosition != null) {
            titleLabel.setText("Редактирование справочника");
            loadDataForEditing();
        }
    }

    private void loadDataForEditing() {
        referenceTypeCombo.setDisable(true);

        if (editingFund != null && editingCollection != null) {
            loadCollectionData();
        } else if (editingFund != null) {
            loadFundData();
        } else if (editingCollection != null) {
            loadCollectionData();
        } else if (editingPosition != null) {
            loadPositionData();
        }
    }

    private void loadCollectionData() {
        referenceTypeCombo.setValue("Коллекции");
        updateFormByType(referenceTypeCombo.getValue());
        collectionNameField.setText(editingCollection.getCollectionName());
        collectionDescriptionField.setText(editingCollection.getCollectionDescription());
        String fundName = editingCollection.getFundName();
        if (fundName != null && !fundName.isEmpty()) {
            collectionFundCombo.setValue(fundName);
        }
    }

    private void loadFundData() {
        referenceTypeCombo.setValue("Фонды");
        updateFormByType(referenceTypeCombo.getValue());
        fundNameField.setText(editingFund.getFundName());
        kpPrefixField.setText(editingFund.getKpPrefix());
        kpPrefixField.setDisable(true);
        fundDescriptionField.setText(editingFund.getFundDescription());
    }

    private void loadPositionData() {
        referenceTypeCombo.setValue("Должности");
        updateFormByType(referenceTypeCombo.getValue());

        positionNameField.setText(editingPosition.getPositionName());
    }


    private void hideAllBlocks() {
        toggleBlock(fundForm, false);
        toggleBlock(collectionForm, false);
        toggleBlock(positionForm, false);
    }

    private void initComboBoxes() throws SQLException {
        referenceTypeCombo.getItems().addAll("Фонды", "Коллекции");
        boolean isAdmin = Service.getCurrentUser() != null && "Администратор".equals(Service.getCurrentUser().getRole());
        if (isAdmin) {
            referenceTypeCombo.getItems().add("Должности");
        }
        collectionFundCombo.getItems().addAll(service.getValuesComboBox("funds", "fund_name", null));
    }

    private void initButtons() {
        cancelBtn.setOnAction(e -> {
            Service.setFund(null);
            Service.setCollection(null);
            Service.setEmployeePosition(null);
            ((Stage) cancelBtn.getScene().getWindow()).close();
        });
        saveBtn.setOnAction(actionEvent -> {
            if (referenceTypeCombo.getValue() == null) {
                service.markFieldAsError(referenceTypeCombo);
                service.openAlert(Alert.AlertType.WARNING, "Выберите тип справочника", "Ошибка");
                return;
            }

            if (!validateRequiredFields()) {
                service.openAlert(Alert.AlertType.WARNING, "Заполните обязательные поля", "Ошибка");
                return;
            }
            boolean isUpdateFund = editingFund != null;
            boolean isUpdateCollection = editingCollection != null;
            boolean isUpdatePosition = editingPosition != null;

            if ("Фонды".equals(referenceTypeCombo.getValue())) {
                if (!isUpdateFund) {
                    editingFund = new Fund();
                    Service.setFund(editingFund);
                }
                try {
                    editingFund.setFundName(fundNameField.getText());
                    editingFund.setKpPrefix(kpPrefixField.getText());
                    editingFund.setFundDescription(fundDescriptionField.getText());
                    int result = FundRepository.addOrUpdate(editingFund);
                    if (result > 0) {
                        if (!isUpdateFund) {
                            editingFund.setId(result);
                            FundRepository.getFunds().add(editingFund);
                        }
                    } else {
                        service.openAlert(Alert.AlertType.ERROR, "Ошибка при сохранении данных", "Неуспешно!");
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else if ("Коллекции".equals(referenceTypeCombo.getValue())) {
                if (!isUpdateCollection) {
                    editingCollection = new Collection();
                    Service.setCollection(editingCollection);
                }
                editingCollection.setCollectionName(collectionNameField.getText());
                editingCollection.setFundName(collectionFundCombo.getValue());
                editingCollection.setCollectionDescription(collectionDescriptionField.getText());
                try {
                    int result = CollectionRepository.addOrUpdate(editingCollection);
                    if (result > 0) {
                        if (!isUpdateCollection) {
                            editingCollection.setId(result);
                            CollectionRepository.getCollections().add(editingCollection);
                        }
                    } else {
                        service.openAlert(Alert.AlertType.ERROR, "Ошибка при сохранении данных", "Неуспешно!");
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else if ("Должности".equals(referenceTypeCombo.getValue())) {
                if (!isUpdatePosition) {
                    editingPosition = new EmployeePosition();
                    Service.setEmployeePosition(editingPosition);
                }
                editingPosition.setPositionName(positionNameField.getText());
                try {
                    int result = EmployeePositionRepository.addOrUpdate(editingPosition);
                    if (result > 0) {
                        if (!isUpdatePosition) {
                            editingPosition.setId(result);
                            EmployeePositionRepository.getPositions().add(editingPosition);
                        }
                    } else {
                        service.openAlert(Alert.AlertType.ERROR, "Ошибка при сохранении должности", "Ошибка");
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }

            service.openAlert(Alert.AlertType.INFORMATION, isUpdateFund || isUpdateCollection || isUpdatePosition ? "Данные успешно обновлены" : "Новый элемент добавлен", "Успех");
            titleLabel.setText("Редактирование справочника");
        });
    }

    private boolean validateRequiredFields() {
        boolean allFilled = true;
        String type = referenceTypeCombo.getValue();

        switch (type) {
            case "Фонды" -> {
                if (isBlank(fundNameField)) { service.markFieldAsError(fundNameField); allFilled = false; }
                if (isBlank(kpPrefixField)) { service.markFieldAsError(kpPrefixField); allFilled = false; }
            }
            case "Коллекции" -> {
                if (isBlank(collectionNameField)) { service.markFieldAsError(collectionNameField); allFilled = false; }
                if (collectionFundCombo.getValue() == null) { service.markFieldAsError(collectionFundCombo); allFilled = false; }
            }
            case "Должности" -> {
                if (isBlank(positionNameField)) {
                    service.markFieldAsError(positionNameField);
                    allFilled = false;
                }
            }
        }
        return allFilled;
    }

    private boolean isBlank(TextField field) {
        return field.getText() == null || field.getText().trim().isEmpty();
    }

    private void initListeners() {
        referenceTypeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
//            currentDocument = null;
            resetForm();
            updateFormByType(newVal);
        });
    }
    private void updateFormByType(String type) {
        hideAllBlocks();
        if (type == null) return;

        switch (type) {
            case "Фонды" -> {
                toggleBlock(fundForm, true);
            }
            case "Коллекции" -> {
                toggleBlock(collectionForm, true);
            }
            case "Должности" -> {
                toggleBlock(positionForm, true);
            }
        }
    }

    private void resetForm() {
        fundNameField.clear();
        fundDescriptionField.clear();
        collectionNameField.clear();
        collectionDescriptionField.clear();
        collectionFundCombo.setValue(null);
        if (cancelBtn.getScene() != null) {
            service.clearAllErrorStyles(cancelBtn.getScene().getRoot());
        }
        positionNameField.clear();
    }

    private void toggleBlock(VBox block, boolean visible) {
        block.setVisible(visible);
        block.setManaged(visible);
    }
}
