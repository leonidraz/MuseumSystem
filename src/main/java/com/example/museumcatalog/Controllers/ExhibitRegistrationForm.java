package com.example.museumcatalog.Controllers;

import com.example.museumcatalog.DBHandler;
import com.example.museumcatalog.Models.Exhibit;
import com.example.museumcatalog.Models.Owner;
import com.example.museumcatalog.Service;
import com.example.museumcatalog.Storages.ExhibitRepository;
import com.example.museumcatalog.Storages.OwnerRepository;
import javafx.beans.binding.Bindings;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ExhibitRegistrationForm {

    //Основные данные предмета
    @FXML private TextField name;
    @FXML private TextArea description;
    @FXML private TextField material;
    @FXML private TextField color;
    @FXML private TextField datingMaterial;
    @FXML private TextField technique;
    @FXML private DatePicker arrivalDate;
    @FXML private TextField storageLocation;

    @FXML private VBox storageLocationBlock;
    @FXML private VBox conditionDetailsBlock;

    //Характеристики
    @FXML private TextField length;
    @FXML private TextField width;
    @FXML private TextField height;
    @FXML private ComboBox<String> unitSizes;
    @FXML private TextField weight;
    @FXML private ComboBox<String> unitWeight;

    //Дополнительная информация
    @FXML private TextField source;
    @FXML private ComboBox<String> condition;
    @FXML private TextArea conditionDetails;
    @FXML private TextField placeOfProduction;
    @FXML private TextField productionTime;
    @FXML private TextArea inscriptions;
    @FXML private TextArea publication;
    @FXML private TextArea usage;
    @FXML private TextArea museumValue;

    //Работа с фото
    @FXML private ImageView imageView;
    @FXML private Button choosePhotoBtn;
    @FXML private Button resetPhotoBtn;

    private File selectedPhoto; // Выбранный файл изображения

    //Кнопки
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;

    @FXML private Label title; // Заголовок (добавление/редактирование)

    //Выбор владельца
    @FXML private TextField ownerSearchField;
    @FXML private TableView<Owner> ownersTable;
    @FXML private TableColumn<Owner, String> fioOwnerColumn;
    @FXML private TableColumn<Owner, String> passportOwnerColumn;
    @FXML private TableColumn<Owner, String> phoneOwnerColumn;

    // Карточки состояния выбора владельца
    @FXML private VBox ownerNotSelected;
    @FXML private VBox ownerSelected;

    // Данные выбранного владельца
    @FXML private Label selectedOwnerFio;
    @FXML private Label selectedOwnerPassport;
    @FXML private Label selectedOwnerPhone;

    private Integer selectedOwnerId = null; // ID выбранного владельца

    public Exhibit getResultController() {
        return resultController;
    }

    private Exhibit resultController;


    Service service = new Service();
    private FilteredList<Owner> filteredOwners;

    public void initialize() throws SQLException {
        setupTableColumns();
        loadOwners();
        //Поиск владельца из загруженного списка
        ownerSearchField.textProperty().addListener((obs, oldVal, newVal) -> searchOwners(newVal));
        //Отображение выбранного владельца
        ownersTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Owner selected = ownersTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    selectOwner(selected);
                }
            }
        });
        //Загрузка списка значений ComboBox
        unitSizes.getItems().addAll(service.getValuesComboBox("units", "unit_name", "type = ?", "length"));
        unitSizes.setValue(unitSizes.getItems().getFirst());
        unitWeight.getItems().addAll(service.getValuesComboBox("units", "unit_name", "type = ?", "weight"));
        unitWeight.setValue(unitWeight.getItems().getFirst());
        updateWeightState();

        weight.textProperty().addListener((obs, oldVal, newVal) -> {
            updateWeightState();
        });

        condition.getItems().addAll(service.getValuesComboBox("exhibit_conditions", "condition_name", null));
        cancelBtn.setOnAction(actionEvent -> {
            Service.setExhibit(null);
            Stage stage = (Stage) cancelBtn.getScene().getWindow();
            stage.close();
        });

        hideLocationBlock(false);

        //Определение режима формы (редактирование/добавление)
        if (Service.getExhibit() != null) {
            loadExhibit();
            arrivalDate.setDisable(true);
            title.setText("Редактирование предмета");
        } else {
            arrivalDate.setValue(LocalDate.now());
        }

        choosePhotoBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(choosePhotoBtn.getScene().getWindow());

            if (file != null) {
                selectedPhoto = file;
                imageView.setImage(new Image(file.toURI().toString()));
            }
        });
        resetPhotoBtn.setOnAction(e -> {
            selectedPhoto = new File("src/main/resources/com/example/museumcatalog/images/picture.png");
            System.out.println(selectedPhoto);
            imageView.setImage(new Image(Objects.requireNonNull(
                    getClass().getResource("/com/example/" +
                            "museumcatalog/images/picture.png")).toExternalForm()));
        });
        saveBtn.setOnAction(actionEvent -> {
            if (!validateRequiredFields()) {
                service.openAlert(Alert.AlertType.WARNING,
                        "Не все обязательные поля заполнены. Поля, подсвеченные красным, необходимо заполнить.",
                        "Проверка обязательных полей");
                return;
            }

            Double lengthValue = validateDoubleField(length, "Длина");
            Double widthValue = validateDoubleField(width, "Ширина");
            Double heightValue = validateDoubleField(height, "Высота");

            if (lengthValue == null || widthValue == null || heightValue == null) {
                return;
            }

            Double weightValue = null;
            if (!weight.getText().isEmpty()) {
                weightValue = validateDoubleField(weight, "Вес");
            }

            Exhibit exhibit = Service.getExhibit();
            boolean isUpdate = exhibit != null;
            String oldPhotoName = isUpdate && exhibit.getPhoto() != null ? exhibit.getPhoto() : null;

            if (!isUpdate) {
                exhibit = new Exhibit();
            }

            exhibit.setName(name.getText());
            exhibit.setDescription(description.getText());
            exhibit.setMaterial(material.getText());
            exhibit.setColor(color.getText());
            exhibit.setDatingMaterial(datingMaterial.getText());
            exhibit.setTechnique(technique.getText());
            exhibit.setSource(source.getText());

            exhibit.setLocation(storageLocation.getText());
            exhibit.setConditionDetails(conditionDetails.getText());

            exhibit.setLength(lengthValue);
            exhibit.setWidth(widthValue);
            exhibit.setHeight(heightValue);
            exhibit.setWeight(weightValue);

            exhibit.setUnitSizes(unitSizes.getValue());
            exhibit.setUnitWeight(weightValue != null ? unitWeight.getValue() : null);

            exhibit.setCondition(condition.getValue());
            exhibit.setPlaceOfProduction(placeOfProduction.getText());
            exhibit.setProductionTime(productionTime.getText());
            exhibit.setInscriptions(inscriptions.getText());
            exhibit.setPublication(publication.getText());
            exhibit.setUsage(usage.getText());
            exhibit.setMuseumValue(museumValue.getText());

            exhibit.setArrivalDate(arrivalDate.getValue());
            exhibit.setOwnerId(selectedOwnerId);
            exhibit.setOwnerFio(selectedOwnerFio.getText());

            exhibit.setStatus(isUpdate ? Service.getExhibit().getStatus() : "В обработке");

            String photoName = oldPhotoName;

            if (selectedPhoto != null) {
                if (selectedPhoto.getPath().contains("picture.png")) {
                    photoName = "";
                } else {
                    String extension = selectedPhoto.getName().substring(selectedPhoto.getName().lastIndexOf("."));
                    photoName = UUID.randomUUID() + extension;
                }
            }
            exhibit.setPhoto(photoName);

            try {
                int result = ExhibitRepository.addOrEdit(exhibit);

                if (result > 0) {
                    try {
                        if (selectedPhoto != null && !selectedPhoto.getName().toLowerCase().contains("picture.png")) {
                            Path path = Path.of("images", photoName);
                            Files.copy(selectedPhoto.toPath(), path);
                        }

                        if (selectedPhoto != null && oldPhotoName != null && !oldPhotoName.isEmpty()
                                && !oldPhotoName.equals("picture.png") && !oldPhotoName.equals(photoName)) {
                            Files.deleteIfExists(Path.of("images", oldPhotoName));
                        }
                    } catch (IOException ioEx) {
                        service.openAlert(Alert.AlertType.WARNING,
                                "Файл не удалось сохранить",
                                "Предмет сохранён, но возникла ошибка при работе с изображением");
                        ioEx.printStackTrace();
                    }

                    if (!isUpdate) {
                        exhibit.setId(result);
                        ExhibitRepository.getExhibits().add(exhibit);
                    }

                    service.openAlert(Alert.AlertType.INFORMATION,
                            isUpdate ? "Предмет изменен!" : "Предмет добавлен!",
                            "Успешно!");

                    selectedPhoto = null;
                    this.resultController = exhibit;
                } else {
                    service.openAlert(Alert.AlertType.INFORMATION,
                            "Ошибка при сохранении предмета",
                            "Неуспешно!");
                }

            } catch (SQLException e) {
                service.openAlert(Alert.AlertType.ERROR,
                        "Ошибка базы данных: " + e.getMessage(), "Ошибка!");
                e.printStackTrace();
            }
        });
    }

    private void updateKPAndLocationVisibility(Exhibit ex) {
        boolean hasKP = ex.getNumberKP() != null && !ex.getNumberKP().isBlank();
        hideLocationBlock(hasKP);
    }

    private void hideLocationBlock(boolean flag) {
        storageLocationBlock.setVisible(flag);
        storageLocationBlock.setManaged(flag);
    }

    private void updateWeightState() {
        boolean hasWeight = weight.getText() != null && !weight.getText().trim().isEmpty();
        unitWeight.setDisable(!hasWeight);
    }

    //Вывод списка владельцев
    private void loadOwners() throws SQLException {
        filteredOwners = new FilteredList<>(
                OwnerRepository.getOwners(),
                p -> true
        );
        if (filteredOwners.isEmpty()) {
            OwnerRepository.loadAll();
        }
        ownersTable.setItems(filteredOwners);
    }
    private void setupTableColumns() {
        //Связываем колонки с полями модели
        fioOwnerColumn.setCellValueFactory(cell ->
                Bindings.createStringBinding(
                        () -> cell.getValue().getFullFio(),
                        cell.getValue().lastNameProperty(),
                        cell.getValue().firstNameProperty(),
                        cell.getValue().middleNameProperty()
                )
        );
        passportOwnerColumn.setCellValueFactory(cell ->
                Bindings.createStringBinding(
                        () -> cell.getValue().getPassport(),
                        cell.getValue().passportSeriesProperty(),
                        cell.getValue().passportNumberProperty()
                )
        );
        phoneOwnerColumn.setCellValueFactory(cell -> cell.getValue().phoneProperty());
    }
    private void loadExhibit() {
        Exhibit ex = Service.getExhibit();
        updateKPAndLocationVisibility(ex);
        if (ex.getOwnerId() != null) {
            for (Owner owner : filteredOwners) {
                int ownerId = owner.getId();
                if (ex.getOwnerId().equals(ownerId)) {
                    selectOwner(owner);
                    break;
                }
            }
        }
        if (ex.getPhoto() != null && !ex.getPhoto().isEmpty() && Files.exists(Path.of("images", ex.getPhoto()))) {
            imageView.setImage(new Image(new File("images/" + ex.getPhoto()).toURI().toString()));
        }
        name.setText(ex.getName());
        description.setText(ex.getDescription());
        material.setText(ex.getMaterial());
        color.setText(ex.getColor());
        technique.setText(ex.getTechnique());
        source.setText(ex.getSource());
        datingMaterial.setText(ex.getDatingMaterial());
        placeOfProduction.setText(ex.getPlaceOfProduction());
        productionTime.setText(ex.getProductionTime());
        inscriptions.setText(ex.getInscriptions());
        publication.setText(ex.getPublication());
        usage.setText(ex.getUsage());
        museumValue.setText(ex.getMuseumValue());
        length.setText(String.valueOf(ex.getLength()));
        width.setText(String.valueOf(ex.getWidth()));
        height.setText(String.valueOf(ex.getHeight()));
        weight.setText(ex.getWeight() != null ? String.valueOf(ex.getWeight()) : "");
        condition.setValue(ex.getCondition());
        unitSizes.setValue(ex.getUnitSizes());
        unitWeight.setValue(ex.getUnitWeight());
        arrivalDate.setValue(ex.getArrivalDate());
        storageLocation.setText(ex.getLocation());
        conditionDetails.setText(ex.getConditionDetails());
    }
    //Проверяем каждое обязательное поле на заполнение
    private boolean validateRequiredFields() {
        Map<Control, String> fields = new HashMap<>();
        fields.put(name, name.getText());
        fields.put(description, description.getText());
        fields.put(condition, condition.getValue());
        fields.put(source, source.getText());
        fields.put(material, material.getText());
        fields.put(color, color.getText());
        fields.put(technique, technique.getText());
        fields.put(length, length.getText());
        fields.put(width, width.getText());
        fields.put(height, height.getText());
        fields.put(unitSizes, unitSizes.getValue());

        boolean allFilled = true;
        for (Control control : fields.keySet()) {
            String value = fields.get(control);
            if (value == null || value.isBlank()) {
                service.markFieldAsError(control);
                allFilled = false;
            }
        }
        return allFilled;
    }
    // Валидирует числовое поле: проверяет, что введено число и оно не отрицательное
    // В случае ошибки показывает alert и возвращает null
    private Double validateDoubleField(TextField field, String fieldName) {
        try {
            double value = Double.parseDouble(field.getText());
            if (value < 0) {
                service.openAlert(Alert.AlertType.ERROR,
                        "Поле '" + fieldName + "' не может быть отрицательным",
                        "Ошибка!");
                return null;
            }
            return value;
        } catch (NumberFormatException e) {
            service.openAlert(Alert.AlertType.ERROR,
                    "Поле '" + fieldName + "' должно быть числом",
                    "Ошибка!");
            return null;
        }
    }
    // Фильтрация владельцев по поисковому запросу
    private void searchOwners(String queryText) {
        String search = queryText.toLowerCase().trim();
        filteredOwners.setPredicate(owner -> {
            String fio = owner.getFullFio().toLowerCase();
            String passport = owner.getPassport().toLowerCase();

            return fio.contains(search) || passport.contains(search);
        });
    }
    // Выбор владельца
    private void selectOwner(Owner owner) {
        selectedOwnerId = owner.getId();

        ownerNotSelected.setVisible(false);
        ownerNotSelected.setManaged(false);
        ownerSelected.setVisible(true);

        selectedOwnerFio.setText(owner.getFullFio());
        selectedOwnerPassport.setText("Паспорт: " + owner.getPassport());
        selectedOwnerPhone.setText("Телефон: " + (owner.getPhone() != null && !owner.getPhone().isEmpty() ? owner.getPhone() : "—"));
    }
}
