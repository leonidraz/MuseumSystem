package com.example.museumcatalog.Controllers;

import com.example.museumcatalog.DBHandler;
import com.example.museumcatalog.Models.Exhibit;
import com.example.museumcatalog.Models.Owner;
import com.example.museumcatalog.Service;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class ExhibitRegistrationForm {

    @FXML
    private DatePicker arrivalDate;

    @FXML
    private Button cancelBtn;

    @FXML
    private Button choosePhotoBtn;

    @FXML
    private TextField color;

    @FXML
    private ComboBox<String> condition;

    @FXML
    private TextField datingMaterial;

    @FXML
    private TextArea description;

    @FXML
    private TextField height;

    @FXML
    private ImageView imageView;

    @FXML
    private TextArea inscriptions;

    @FXML
    private TextField length;

    @FXML
    private TextField material;

    @FXML
    private TextArea museumValue;

    @FXML
    private TextField name;

    @FXML
    private TextField placeOfProduction;

    @FXML
    private TextField productionTime;

    @FXML
    private TextArea publication;

    @FXML
    private Button resetPhotoBtn;

    @FXML
    private Button saveBtn;

    @FXML
    private Button saveWithDocumentBtn;

    @FXML
    private VBox selectedOwnerCard;

    @FXML
    private TextField source;

    @FXML
    private TextField technique;

    @FXML
    private ComboBox<String> unitSizes;

    @FXML
    private ComboBox<String> unitWeight;

    @FXML
    private TextArea usage;

    @FXML
    private TextField weight;

    @FXML
    private TextField width;

    @FXML
    private Label title;

    @FXML private TableView<Owner> ownersTable;
    @FXML private TableColumn<Owner, String> fioColumn;
    @FXML private TableColumn<Owner, String> passportColumn;
    @FXML private TableColumn<Owner, String> phoneColumn;
    @FXML private TextField ownerSearchField;
    @FXML private Button ownerSearchBtn;
    @FXML private VBox ownerNotSelected;
    @FXML private VBox ownerSelected;
    @FXML private Label selectedOwnerFio;
    @FXML private Label selectedOwnerPassport;
    @FXML private Label selectedOwnerPhone;
    @FXML private Label selectedOwnerEmail;
    @FXML private VBox cancelSelectionBtnContainer;
    @FXML private Button cancelSelectionBtn;

    private Integer selectedOwnerId = null;
    private ObservableList<Owner> ownersList = FXCollections.observableArrayList();

    private File selectedPhoto;

    Service service = new Service();

    public void initialize() throws SQLException {
        unitSizes.getItems().addAll(service.getValuesComboBox("units", "unit_name", "type = ?", "length"));
        unitSizes.setValue(unitSizes.getItems().getFirst());
        unitWeight.getItems().addAll(service.getValuesComboBox("units", "unit_name", "type = ?", "weight"));
        unitWeight.setValue(unitWeight.getItems().getFirst());
        condition.getItems().addAll(service.getValuesComboBox("exhibit_conditions", "condition_name", null));

        if (Service.getExhibit() != null) {
            loadExhibit();
            arrivalDate.setDisable(true);
            title.setText("Редактирование экспоната");
        } else {
            arrivalDate.setValue(LocalDate.now());
        }

        fioColumn.setCellValueFactory(cell -> {
            Owner owner = cell.getValue();
            return new SimpleStringProperty(owner.getShortFio());
        });
        passportColumn.setCellValueFactory(cell -> cell.getValue().passportSeriesProperty()
                .concat(" ").concat(cell.getValue().passportNumberProperty()));
        phoneColumn.setCellValueFactory(cell -> cell.getValue().phoneProperty());

        loadOwners();

        ownerSearchField.textProperty().addListener((obs, oldVal, newVal) -> filterOwners(newVal));

        ownersTable.setRowFactory(tv -> {
            TableRow<Owner> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    selectOwner(row.getItem());
                }
            });
            return row;
        });

        cancelBtn.setOnAction(actionEvent -> {
            Service.setExhibit(null);
            Stage stage = (Stage) cancelBtn.getScene().getWindow();
            stage.close();
        });

        choosePhotoBtn.setOnAction(e -> {

            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(choosePhotoBtn.getScene().getWindow());

            if (file != null) {
                selectedPhoto = file;
                imageView.setImage(new Image(file.toURI().toString()));
            }
        });

        resetPhotoBtn.setOnAction(e -> {
            selectedPhoto = null;

            imageView.setImage(new Image(Objects.requireNonNull(
                    getClass().getResource("/com/example/" +
                            "museumcatalog/images/picture.png")).toExternalForm()));
        });

        saveBtn.setOnAction(actionEvent -> {
            if (!validateRequiredFields()) {
                return;
            }

            Double lengthValue = validateDoubleField(length, "Длина");
            Double widthValue = validateDoubleField(width, "Ширина");
            Double heightValue = validateDoubleField(height, "Высота");
            Double weightValue = validateDoubleField(weight, "Вес");

            if (lengthValue == null || widthValue == null || heightValue == null || weightValue == null) {
                return;
            }

            String photoName = "";

            try {
                if (selectedPhoto != null) {
                    String extension = selectedPhoto.getName().substring(selectedPhoto.getName().lastIndexOf("."));
                    photoName = UUID.randomUUID() + extension;
                    Path path = Path.of("images", photoName);
                    Files.copy(selectedPhoto.toPath(), path);
                }
                if (Service.getExhibit()!= null) {
                    String oldPhoto = Service.getExhibit().photoProperty().get();
                    if (oldPhoto != null && !oldPhoto.isEmpty()) {
                        Files.deleteIfExists(Path.of("images", oldPhoto));
                    }
                    Service.getExhibit().setPhoto(photoName);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            String query;
            boolean isUpdate = Service.getExhibit() != null;

            if (!isUpdate) {
                // INSERT
                query = "INSERT INTO public.exhibits\n" +
                        "(photo,\n" +
                        "\"name\",\n" +
                        "description,\n" +
                        "length,\n" +
                        "width,\n" +
                        "height,\n" +
                        "weight,\n" +
                        "color,\n" +
                        "material,\n" +
                        "dating_material,\n" +
                        "technique,\n" +
                        "condition_id,\n" +
                        "\"source\",\n" +
                        "arrival_date,\n" +
                        "inscriptions,\n" +
                        "place_of_production,\n" +
                        "production_time,\n" +
                        "\"publication\",\n" +
                        "\"usage\",\n" +
                        "museum_value,\n" +
                        "status_id,\n" +
                        "fund_id,\n" +
                        "collection_id,\n" +
                        "number_kp,\n" +
                        "owner_id,\n" +
                        "unit_sizes_id,\n" +
                        "unit_weight_id\n" +
                        ")\n" +
                        "VALUES\n" +
                        "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,\n" +
                        "(SELECT id FROM exhibit_conditions WHERE condition_name = ?),\n" +
                        "?, ?, ?, ?, ?, ?, ?, ?,\n" +
                        "(SELECT id FROM exhibit_statuses WHERE status_name = ?),\n" +
                        "NULL, NULL, NULL, ?,\n" +
                        "(SELECT id FROM units WHERE unit_name = ?),\n" +
                        "(SELECT id FROM units WHERE unit_name = ?)\n" +
                        ")";
            } else {
                // UPDATE
                query = "UPDATE public.exhibits SET\n" +
                        "photo = ?, \n" +
                        "\"name\" = ?, \n" +
                        "description = ?, \n" +
                        "length = ?, \n" +
                        "width = ?, \n" +
                        "height = ?, \n" +
                        "weight = ?, \n" +
                        "color = ?, \n" +
                        "material = ?,\n" +
                        "dating_material = ?, \n" +
                        "technique = ?, \n" +
                        "condition_id = (SELECT id FROM exhibit_conditions WHERE condition_name = ?), \n" +
                        "\"source\" = ?, \n" +
                        "arrival_date = ?, \n" +
                        "inscriptions = ?, \n" +
                        "place_of_production = ?,\n" +
                        "production_time = ?, \n" +
                        "\"publication\" = ?, \n" +
                        "\"usage\" = ?, \n" +
                        "museum_value = ?,\n" +
                        "status_id = (SELECT id FROM exhibit_statuses WHERE status_name = ?), \n" +
                        "unit_sizes_id = (SELECT id FROM units WHERE unit_name = ?), \n" +
                        "unit_weight_id = (SELECT id FROM units WHERE unit_name = ?)\n" +
                        "owner_id = ?\n" +
                        "WHERE id = ?";
            }

            try {
                int count;

                if (!isUpdate) {
                    count = DBHandler.executeUpdate(query,
                            photoName,
                            name.getText(),
                            description.getText(),
                            lengthValue,
                            widthValue,
                            heightValue,
                            weightValue,
                            color.getText(),
                            material.getText(),
                            datingMaterial.getText(),
                            technique.getText(),
                            condition.getValue(),
                            source.getText(),
                            arrivalDate.getValue(),
                            inscriptions.getText(),
                            placeOfProduction.getText(),
                            productionTime.getText(),
                            publication.getText(),
                            usage.getText(),
                            museumValue.getText(),
                            "В обработке",
                            selectedOwnerId,
                            unitSizes.getValue(),
                            unitWeight.getValue()
                    );
                } else {
                    count = DBHandler.executeUpdate(query,
                            photoName,
                            name.getText(),
                            description.getText(),
                            lengthValue,
                            widthValue,
                            heightValue,
                            weightValue,
                            color.getText(),
                            material.getText(),
                            datingMaterial.getText(),
                            technique.getText(),
                            condition.getValue(),
                            source.getText(),
                            arrivalDate.getValue(),
                            inscriptions.getText(),
                            placeOfProduction.getText(),
                            productionTime.getText(),
                            publication.getText(),
                            usage.getText(),
                            museumValue.getText(),
                            Service.getExhibit().getStatus(), // сохраняем текущий статус при редактировании
                            unitSizes.getValue(),
                            unitWeight.getValue(),
                            selectedOwnerId,
                            Service.getExhibit().getId()
                    );
                }

                String action = isUpdate ? "изменен" : "добавлен";
                String message = count > 0
                        ? "Экспонат успешно " + action
                        : "Экспонат не был " + action;

                service.openAlert(Alert.AlertType.INFORMATION, message, "Уведомление!");


            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void loadExhibit() {
        var exhibit = Service.getExhibit();

        if (exhibit.getPhoto() != null && !exhibit.getPhoto().isEmpty()) {
            imageView.setImage(new Image(new File("images/" + exhibit.getPhoto()).toURI().toString()));
        }

        name.setText(exhibit.getName());
        description.setText(exhibit.getDescription());
        material.setText(exhibit.getMaterial());
        color.setText(exhibit.getColor());
        technique.setText(exhibit.getTechnique());
        source.setText(exhibit.getSource());
        datingMaterial.setText(exhibit.getDatingMaterial());
        placeOfProduction.setText(exhibit.getPlaceOfProduction());
        productionTime.setText(exhibit.getProductionTime());
        inscriptions.setText(exhibit.getInscriptions());
        publication.setText(exhibit.getPublication());
        usage.setText(exhibit.getUsage());
        museumValue.setText(exhibit.getMuseumValue());
        length.setText(String.valueOf(exhibit.getLength()));
        width.setText(String.valueOf(exhibit.getWidth()));
        height.setText(String.valueOf(exhibit.getHeight()));
        weight.setText(String.valueOf(exhibit.getWeight()));
        condition.setValue(exhibit.getCondition());
        unitSizes.setValue(exhibit.getUnitSizes());
        unitWeight.setValue(exhibit.getUnitWeight());
        arrivalDate.setValue(exhibit.getArrivalDate());
    }

    private boolean validateRequiredFields() {
        boolean allFilled = true;

        // Проверяем каждое обязательное поле
        if (name.getText() == null || name.getText().isBlank()) {
            markFieldAsError(name);
            allFilled = false;
        }
        if (description.getText() == null || description.getText().isBlank()) {
            markFieldAsError(description);
            allFilled = false;
        }
        if (condition.getValue() == null || condition.getValue().isBlank()) {
            markFieldAsError(condition);
            allFilled = false;
        }
        if (source.getText() == null || source.getText().isBlank()) {
            markFieldAsError(source);
            allFilled = false;
        }
        if (material.getText() == null || material.getText().isBlank()) {
            markFieldAsError(material);
            allFilled = false;
        }
        if (color.getText() == null || color.getText().isBlank()) {
            markFieldAsError(color);
            allFilled = false;
        }
        if (technique.getText() == null || technique.getText().isBlank()) {
            markFieldAsError(technique);
            allFilled = false;
        }
        if (length.getText() == null || length.getText().isBlank()) {
            markFieldAsError(length);
            allFilled = false;
        }
        if (width.getText() == null || width.getText().isBlank()) {
            markFieldAsError(width);
            allFilled = false;
        }
        if (height.getText() == null || height.getText().isBlank()) {
            markFieldAsError(height);
            allFilled = false;
        }
        if (unitSizes.getValue() == null || unitSizes.getValue().isBlank()) {
            markFieldAsError(unitSizes);
            allFilled = false;
        }
        if (unitWeight.getValue() == null || unitWeight.getValue().isBlank()) {
            markFieldAsError(unitWeight);
            allFilled = false;
        }

        if (!allFilled) {
            service.openAlert(Alert.AlertType.WARNING,
                    "Не все обязательные поля заполнены. Поля, подсвеченные красным, необходимо заполнить.",
                    "Проверка обязательных полей");
        }

        return allFilled;
    }

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

    private void markFieldAsError(Control field) {
        String currentStyle = field.getStyle();
        field.setStyle(currentStyle + "; -fx-border-color: red;");

        field.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                field.setStyle(currentStyle + "-fx-border-color: #CCCCCC;");
            }
        });
    }

    // Загрузка всех активных владельцев
    private void loadOwners() {
        try {
            String query = "SELECT id, last_name, first_name, middle_name, " +
                    "address, passport_series, passport_number, issued_by, " +
                    "date_of_issue, phone, notice\n" +
                    "FROM owners ORDER BY last_name, first_name\n";

            ResultSet rs = DBHandler.executeQuery(query);
            ownersList.clear();

            while (rs.next()) {
                Owner owner = new Owner();
                owner.setId(rs.getInt("id"));
                owner.setLastName(rs.getString("last_name"));
                owner.setFirstName(rs.getString("first_name"));
                owner.setMiddleName(rs.getString("middle_name"));
                owner.setAddress(rs.getString("address"));
                owner.setPassportSeries(rs.getString("passport_series"));
                owner.setPassportNumber(rs.getString("passport_number"));
                owner.setIssuedBy(rs.getString("issued_by"));
                owner.setDateOfIssue(rs.getDate("date_of_issue") != null ?
                        rs.getDate("date_of_issue").toLocalDate() : null);
                owner.setPhone(rs.getString("phone"));
                owner.setNotice(rs.getString("notice"));

                ownersList.add(owner);
            }

            ownersTable.setItems(ownersList);

        } catch (SQLException e) {
            e.printStackTrace();
            service.openAlert(Alert.AlertType.ERROR, "Ошибка загрузки владельцев: " + e.getMessage(), "Ошибка");
        }
    }

    // Фильтрация владельцев по поисковому запросу
    private void filterOwners(String queryText) {
        if (queryText == null || queryText.trim().isEmpty()) {
            ownersTable.setItems(ownersList);
            return;
        }

        String searchText = queryText.trim().toLowerCase();
        ObservableList<Owner> filtered = FXCollections.observableArrayList();

        for (Owner owner : ownersList) {
            String fio = (owner.getLastName() + " " + owner.getFirstName() + " " + owner.getMiddleName()).toLowerCase();
            String passport = (owner.getPassportSeries() + owner.getPassportNumber()).toLowerCase();

            if (fio.contains(searchText) || passport.contains(searchText.replace(" ", ""))) {
                filtered.add(owner);
            }
        }

        ownersTable.setItems(filtered);
    }

    // Выбор владельца
    private void selectOwner(Owner owner) {
        selectedOwnerId = owner.getId();

        ownerNotSelected.setVisible(false);
        ownerNotSelected.setManaged(false);
        ownerSelected.setVisible(true);

        selectedOwnerFio.setText(owner.getFullFio());
        selectedOwnerPassport.setText("Паспорт: " + owner.getPassport());
        selectedOwnerPhone.setText("Телефон: " + (owner.getPhone() != null && !owner.getPhone().isEmpty() ?
                owner.getPhone() : "—"));
    }
}
