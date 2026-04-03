package com.example.museumcatalog.Controllers;

import com.example.museumcatalog.DBHandler;
import com.example.museumcatalog.Models.Exhibit;
import com.example.museumcatalog.Service;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class ExhibitsListFormController {

    private static final Map<String, String> STATUS_COLORS = Map.of(
            "На временном хранении", "#2196F3",    // Синий
            "На рассмотрении ЭФЗК", "#FF9800",    // Оранжевый
            "Принят ЭФЗК", "#4CAF50",             // Зеленый
            "Возвращен владельцу", "#9C27B0",     // Фиолетовый
            "В фонде", "#00BCD4",                 // Бирюзовый
            "На выставке", "#FF5722",             // Оранжево-красный
            "Выдан организации", "#795548",       // Коричневый
            "Архивирован", "#607D8B",             // Серо-синий
            "В обработке", "#607D8B"              // Серо-синий
    );

    @FXML
    public VBox centerVBox;

    @FXML
    private Circle statusCircle;

    @FXML
    public BorderPane rootPane;

    @FXML
    public DatePicker dateFrom;

    @FXML
    public DatePicker dateTo;

    @FXML
    private Label arrivalDate;

    @FXML
    private Label collection;

    @FXML
    private Label color;

    @FXML
    private Label condition;

    @FXML
    private Label dating;

    @FXML
    private Label description;

    @FXML
    private Label dimensions;

    @FXML
    private Label fund;

    @FXML
    private Label inscriptions;

    @FXML
    private Label KPPNumber;

    @FXML
    private Label material;

    @FXML
    private Label museumValue;

    @FXML
    private Label name;

    @FXML
    private Label owner;

    @FXML
    private Label productionPlace;

    @FXML
    private Label productionTime;

    @FXML
    private Label publications;

    @FXML
    private Label source;

    @FXML
    private Label status;

    @FXML
    private Label technique;

    @FXML
    private Label totalCountLabel;

    @FXML
    private Label usage;

    @FXML
    private Label weight;

    @FXML
    private TextField search;

    @FXML
    private Button closeDetailsCard;

    @FXML
    private VBox detailsCard;

    @FXML
    private VBox contentDetailsCard;

    @FXML
    private ComboBox<String> filterCollection;

    @FXML
    private ComboBox<String> filterFund;

    @FXML
    private ComboBox<String> filterStatus;

    @FXML
    private Button addBtn;

    @FXML
    private Button editBtn;

    @FXML
    private Button deleteBtn;

    @FXML
    private Button archiveBtn;

    @FXML
    private ImageView imageView;

    @FXML
    private TableView<Exhibit> exhibitsTable;
    @FXML
    private TableColumn<Exhibit, String> nameColumn;
    @FXML
    private TableColumn<Exhibit, String> descColumn;
    @FXML
    private TableColumn<Exhibit, Double> lengthColumn;
    @FXML
    private TableColumn<Exhibit, Double> widthColumn;
    @FXML
    private TableColumn<Exhibit, Double> heightColumn;
    @FXML
    public TableColumn<Exhibit, String> unitSizesColumn;
    @FXML
    public TableColumn<Exhibit, String> unitWeightColumn;
    @FXML
    private TableColumn<Exhibit, Double> weightColumn;
    @FXML
    private TableColumn<Exhibit, String> colorColumn;
    @FXML
    private TableColumn<Exhibit, String> materialColumn;
    @FXML
    private TableColumn<Exhibit, String> datingMaterialColumn;
    @FXML
    private TableColumn<Exhibit, String> techniqueColumn;
    @FXML
    private TableColumn<Exhibit, String> conditionColumn;
    @FXML
    private TableColumn<Exhibit, String> sourceColumn;
    @FXML
    private TableColumn<Exhibit, String> arrivalDateColumn;
    @FXML
    private TableColumn<Exhibit, String> inscriptionsColumn;
    @FXML
    private TableColumn<Exhibit, String> placeOfProductionColumn;
    @FXML
    private TableColumn<Exhibit, String> productionTimeColumn;
    @FXML
    private TableColumn<Exhibit, String> publicationColumn;
    @FXML
    private TableColumn<Exhibit, String> usageColumn;
    @FXML
    private TableColumn<Exhibit, String> museumValueColumn;
    @FXML
    private TableColumn<Exhibit, String> statusColumn;
    @FXML
    private TableColumn<Exhibit, String> fundColumn;
    @FXML
    private TableColumn<Exhibit, String> collectionColumn;
    @FXML
    private TableColumn<Exhibit, String> numberKPColumn;
    @FXML
    private Button updateTableBtn;

    ObservableList<Exhibit> exhibitsList = FXCollections.observableArrayList();

    Service service = new Service();

    public void initialize() throws SQLException {
        //Скрытие карточки с деталями экспоната при открытии формы
        archiveBtn.setText("Архивировать");
        detailsCard.setPrefWidth(10);
        detailsCard.setStyle("-fx-background-color: #7CB4CF;");
        contentDetailsCard.setVisible(false);

        //Загрузка списка значений ComboBox для фильтров
        filterStatus.getItems().add("Все статусы");
        filterStatus.getItems().addAll(service.getValuesComboBox("exhibit_statuses", "status_name", null));
        filterFund.getItems().add("Все записи");
        filterFund.getItems().add("Все фонды");
        filterFund.getItems().add("Без фонда");
        filterFund.getItems().addAll(service.getValuesComboBox("funds", "fund_name", null));
        filterFund.setValue("Все записи");

        //Скрытие коллекций при значении "Все фонды"
        updateCollectionsComboBox(filterFund.getValue());

        //Отслеживание изменений значений фильтра со статусами
        filterStatus.valueProperty().addListener((obs, oldValue, newValue) -> {
            try {
                loadExhibits();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        //Отслеживание изменений значений фильтра с фондами
        filterFund.valueProperty().addListener((obs, oldValue, newValue) -> {
            updateCollectionsComboBox(newValue);
            try {
                loadExhibits();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        //Отслеживание изменений значений фильтра с коллекциями
        filterCollection.valueProperty().addListener((obs, oldValue, newValue) -> {
            try {
                loadExhibits();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        dateFrom.valueProperty().addListener((obs, oldValue, newValue) -> {
            try {
                loadExhibits();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        dateTo.valueProperty().addListener((obs, oldValue, newValue) -> {
            try {
                loadExhibits();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        search.textProperty().addListener((obs, oldValue, newValue) -> {
            try {
                loadExhibits();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        // --- Связываем колонки с полями модели ---
        nameColumn.setCellValueFactory(cell -> cell.getValue().nameProperty());
        descColumn.setCellValueFactory(cell -> cell.getValue().descriptionProperty());
        lengthColumn.setCellValueFactory(cell -> cell.getValue().lengthProperty().asObject());
        widthColumn.setCellValueFactory(cell -> cell.getValue().widthProperty().asObject());
        heightColumn.setCellValueFactory(cell -> cell.getValue().heightProperty().asObject());
        unitSizesColumn.setCellValueFactory(cell -> cell.getValue().unitSizesProperty());
        weightColumn.setCellValueFactory(cell -> cell.getValue().weightProperty().asObject());
        unitWeightColumn.setCellValueFactory(cell -> cell.getValue().unitWeightProperty());
        colorColumn.setCellValueFactory(cell -> cell.getValue().colorProperty());
        materialColumn.setCellValueFactory(cell -> cell.getValue().materialProperty());
        datingMaterialColumn.setCellValueFactory(cell -> cell.getValue().datingMaterialProperty());
        techniqueColumn.setCellValueFactory(cell -> cell.getValue().techniqueProperty());
        conditionColumn.setCellValueFactory(cell -> cell.getValue().conditionProperty());
        sourceColumn.setCellValueFactory(cell -> cell.getValue().sourceProperty());
        arrivalDateColumn.setCellValueFactory(cell -> cell.getValue().arrivalDateProperty());
        inscriptionsColumn.setCellValueFactory(cell -> cell.getValue().inscriptionsProperty());
        placeOfProductionColumn.setCellValueFactory(cell -> cell.getValue().placeOfProductionProperty());
        productionTimeColumn.setCellValueFactory(cell -> cell.getValue().productionTimeProperty());
        publicationColumn.setCellValueFactory(cell -> cell.getValue().publicationProperty());
        usageColumn.setCellValueFactory(cell -> cell.getValue().usageProperty());
        museumValueColumn.setCellValueFactory(cell -> cell.getValue().museumValueProperty());
        statusColumn.setCellValueFactory(cell -> cell.getValue().statusProperty());
        fundColumn.setCellValueFactory(cell -> cell.getValue().fundProperty());
        collectionColumn.setCellValueFactory(cell -> cell.getValue().collectionProperty());
        numberKPColumn.setCellValueFactory(cell -> cell.getValue().numberKPProperty());

        // Подключаем слушатель на выбор строки
        exhibitsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                loadExhibitInfo(newSelection);
                openDetailsCard();
            }
        });

        closeDetailsCard.setOnAction(actionEvent -> {
            contentDetailsCard.setVisible(false);

            Timeline closeAnim = new Timeline(
                    new KeyFrame(Duration.millis(300),
                            new KeyValue(detailsCard.prefWidthProperty(), 10)
                    )
            );
            closeAnim.setOnFinished(e -> detailsCard.setStyle("-fx-background-color: #7CB4CF;"));

            closeAnim.play();
        });


        detailsCard.setOnMouseClicked(actionEvent -> {
            openDetailsCard();
        });

        addBtn.setOnAction(actionEvent -> {
            try {
                Service.openModal("ExhibitRegistrationForm", "Добавление экспоната",
                        (Stage) addBtn.getScene().getWindow());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        editBtn.setOnAction(actionEvent -> {
            Exhibit selected = exhibitsTable.getSelectionModel().getSelectedItem();

            if (selected == null) {
                service.openAlert(Alert.AlertType.WARNING,
                        "Выберите экспонат для редактирования",
                        "Предупреждение");
                return;
            }

            try {
                Service.setExhibit(selected); // ✅ передаём объект
                Service.openModal("ExhibitRegistrationForm", "Редактирование экспоната",
                        (Stage) addBtn.getScene().getWindow());
                exhibitsTable.refresh();
                loadExhibitInfo(selected);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        deleteBtn.setOnAction(actionEvent -> {
            Exhibit selected = exhibitsTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                service.openAlert(Alert.AlertType.WARNING, "Выберите экспонат для удаления", "Предупреждение");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Подтверждение удаления");
            confirm.setHeaderText("Вы уверены, что хотите удалить экспонат?");
            confirm.setContentText("Экспонат \"" + selected.getName() + "\" будет безвозвратно удален из базы данных и файловой системы.");

            if (confirm.showAndWait().get() == ButtonType.OK) {
                try {
                    // Удаление фото из файловой системы
                    if (selected.getPhoto() != null && !selected.getPhoto().isEmpty()) {
                        Files.deleteIfExists(Path.of("images", selected.getPhoto()));
                    }

                    // Удаление из БД
                    String query = "DELETE FROM exhibits WHERE id = ?";
                    DBHandler.executeUpdate(query, selected.getId());

                    service.openAlert(Alert.AlertType.INFORMATION, "Экспонат успешно удален", "Успешно");
                    loadExhibits();
                    totalCountLabel.setText("Всего: " + exhibitsTable.getItems().size());

                    // Сброс карточки деталей
                    name.setText("Экспонат не выбран");
                    status.setText("—");
                    KPPNumber.setText("—");
                    detailsCard.setPrefWidth(10);
                    detailsCard.setStyle("-fx-background-color: #7CB4CF;");
                    contentDetailsCard.setVisible(false);

                } catch (Exception e) {
                    e.printStackTrace();
                    service.openAlert(Alert.AlertType.ERROR, "Ошибка удаления экспоната: " + e.getMessage(), "Ошибка");
                }
            }
        });

        archiveBtn.setOnAction(actionEvent -> {
            Exhibit selected = exhibitsTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                service.openAlert(Alert.AlertType.WARNING, "Выберите экспонат", "Предупреждение");
                return;
            }

            String currentStatus = selected.getStatus();

            if ("Архивирован".equals(currentStatus)) {
                // Разархивация - восстанавливаем предыдущий статус
                try {
                    String query = "UPDATE exhibits SET " +
                            "status_id = previous_status_id, " +
                            "previous_status_id = NULL " +
                            "WHERE id = ?";
                    DBHandler.executeUpdate(query, selected.getId());
                    String queryStatus = "SELECT es.status_name FROM exhibits e " +
                            "LEFT JOIN exhibit_statuses es ON e.status_id = es.id " +
                            "WHERE e.id = ?";
                    ResultSet rs = DBHandler.executeQuery(queryStatus, selected.getId());
                    if (rs.next()) {
                        selected.setStatus(rs.getString("status_name"));
                    }
                    service.openAlert(Alert.AlertType.INFORMATION, "Экспонат восстановлен из архива", "Успешно");

                } catch (SQLException e) {
                    e.printStackTrace();
                    service.openAlert(Alert.AlertType.ERROR, "Ошибка восстановления: " + e.getMessage(), "Ошибка");
                }
            } else {
                // Архивация - сохраняем текущий статус как предыдущий
                try {
                    String query = "UPDATE exhibits SET " +
                            "previous_status_id = status_id, " +
                            "status_id = (SELECT id FROM exhibit_statuses WHERE status_name = 'Архивирован') " +
                            "WHERE id = ?";
                    DBHandler.executeUpdate(query, selected.getId());
                    selected.setStatus("Архивирован");
                    service.openAlert(Alert.AlertType.INFORMATION, "Экспонат помещен в архив", "Успешно");

                } catch (SQLException e) {
                    e.printStackTrace();
                    service.openAlert(Alert.AlertType.ERROR, "Ошибка архивации: " + e.getMessage(), "Ошибка");
                }
            }
            loadExhibitInfo(selected);
        });

        loadExhibits();
        totalCountLabel.setText("Всего: " + exhibitsTable.getItems().size());
    }

    private void loadExhibitInfo(Exhibit newSelection) {
        if (newSelection.photoProperty().get() != null && !newSelection.photoProperty().get().isEmpty()) {
            imageView.setImage(new Image(new File("images/" + newSelection.photoProperty().get()).toURI().toString()));
        } else {
            imageView.setImage(new Image(Objects.requireNonNull(
                    getClass().getResource("/com/example/" +
                            "museumcatalog/images/picture.png")).toExternalForm()));
        }

        if ("Архивирован".equals(newSelection.getStatus())) {
            archiveBtn.setText("Разархивировать");
        } else {
            archiveBtn.setText("Архивировать");
        }

        name.setText(nonNullOrDash(newSelection.nameProperty().get()));
        status.setText(nonNullOrDash(newSelection.statusProperty().get()));
        String statusColor = STATUS_COLORS.getOrDefault(status.getText(), "#607D8B");
        status.setStyle("-fx-text-fill: " + statusColor + ";");
        statusCircle.setFill(Paint.valueOf(statusColor));
        KPPNumber.setText(nonNullOrDash(newSelection.numberKPProperty().get()));
        arrivalDate.setText(nonNullOrDash(newSelection.arrivalDateProperty().get()));
        description.setText(nonNullOrDash(newSelection.descriptionProperty().get()));
        fund.setText(nonNullOrDash(newSelection.fundProperty().get()));
        collection.setText(nonNullOrDash(newSelection.collectionProperty().get()));

        dimensions.setText(nonNullOrDash(String.valueOf(newSelection.lengthProperty().get())) + " x " +
                nonNullOrDash(String.valueOf(newSelection.widthProperty().get())) + " x " +
                nonNullOrDash(String.valueOf(newSelection.heightProperty().get()) + " " + newSelection.unitSizesProperty().get()));
        weight.setText(nonNullOrDash(String.valueOf(newSelection.weightProperty().get()) + " " + newSelection.unitWeightProperty().get()));
        color.setText(nonNullOrDash(newSelection.colorProperty().get()));
        material.setText(nonNullOrDash(newSelection.materialProperty().get()));
        dating.setText(nonNullOrDash(newSelection.datingMaterialProperty().get()));
        technique.setText(nonNullOrDash(newSelection.techniqueProperty().get()));

        source.setText(nonNullOrDash(newSelection.sourceProperty().get()));
        condition.setText(nonNullOrDash(newSelection.conditionProperty().get()));
        productionPlace.setText(nonNullOrDash(newSelection.placeOfProductionProperty().get()));
        productionTime.setText(nonNullOrDash(newSelection.productionTimeProperty().get()));
        publications.setText(nonNullOrDash(newSelection.publicationProperty().get()));
        usage.setText(nonNullOrDash(newSelection.usageProperty().get()));
        museumValue.setText(nonNullOrDash(newSelection.museumValueProperty().get()));
        inscriptions.setText(nonNullOrDash(newSelection.inscriptionsProperty().get()));
    }

    private void openDetailsCard() {

        contentDetailsCard.setVisible(true);

        Timeline openAnim = new Timeline(
                new KeyFrame(Duration.millis(300),
                        new KeyValue(detailsCard.prefWidthProperty(), 443)
                )
        );

        openAnim.play();
        detailsCard.setStyle("-fx-background-color: #EBEFF0;");
    }

    private String nonNullOrDash(String value) {
        if (value == null || value.isEmpty()) {
            return "—";
        }
        return value;
    }

    private void updateCollectionsComboBox(String selectedFund) {
        filterCollection.getItems().clear();

        if (selectedFund.equals("Все фонды") || selectedFund.equals("Все записи") || selectedFund.equals("Без фонда")) {
            filterCollection.getItems().clear();
            filterCollection.getItems().add("Сначала выберите фонд");
            filterCollection.setValue("Сначала выберите фонд");
            filterCollection.setDisable(true);
        } else {
            try {
                filterCollection.setDisable(false);
                filterCollection.getItems().clear();
                filterCollection.getItems().add("Все коллекции");
                filterCollection.getItems().addAll(service.getValuesComboBox(
                        "collections c join funds f on c.fund_id = f.id",
                        "collection_name",
                        "f.fund_name = ?",
                        selectedFund
                ));
                filterCollection.setValue("Все коллекции");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadExhibits() throws SQLException {
        exhibitsList.clear();
        exhibitsTable.getItems().clear();

        StringBuilder query = new StringBuilder(
                "SELECT e.id, e.photo, e.\"name\", e.description, e.length, e.width, e.height, u_sizes.unit_name as unit_sizes, e.weight, u_weight.unit_name  as unit_weight,\n" +
                        "e.color, e.material, e.dating_material, e.technique, ec.condition_name, e.\"source\",\n" +
                        "e.arrival_date, e.inscriptions, e.place_of_production, e.production_time, e.\"publication\",\n" +
                        "e.\"usage\", e.museum_value, es.status_name, f.fund_name, c.collection_name, e.number_kp\n" +
                        "FROM exhibits e\n" +
                        "LEFT JOIN exhibit_statuses es ON e.status_id = es.id\n" +
                        "LEFT JOIN funds f ON e.fund_id = f.id\n" +
                        "LEFT JOIN collections c ON e.collection_id = c.id\n" +
                        "LEFT JOIN units u_sizes ON e.unit_sizes_id = u_sizes.id\n" +
                        "LEFT JOIN units u_weight ON e.unit_weight_id = u_weight.id\n" +
                        "LEFT JOIN exhibit_conditions ec ON e.condition_id = ec.id\n"
        );

        ArrayList<Object> params = new ArrayList<>();
        boolean hasWhere = false;

        // Поиск
        if (search != null && !search.getText().isEmpty()) {
            query.append("WHERE (e.\"name\" ILIKE ? OR e.number_kp ILIKE ?) ");
            String searchText = "%" + search.getText() + "%";
            params.add(searchText);
            params.add(searchText);
            hasWhere = true;
        }

        // Статус
        if (filterStatus != null && filterStatus.getValue() != null && !filterStatus.getValue().equals("Все статусы")) {
            query.append(hasWhere ? " AND " : " WHERE ");
            query.append("es.status_name = ? ");
            params.add(filterStatus.getValue());
            hasWhere = true;
        }

        // Фонд
        if (filterFund != null && filterFund.getValue() != null) {
            String fundValue = filterFund.getValue();
            if ("Все фонды".equals(fundValue)) {
                query.append(hasWhere ? " AND " : " WHERE ");
                query.append("e.fund_id IS NOT NULL ");
                hasWhere = true;
            } else if ("Без фонда".equals(fundValue)) {
                query.append(hasWhere ? " AND " : " WHERE ");
                query.append("e.fund_id IS NULL ");
                hasWhere = true;
            } else if (!"Все записи".equals(fundValue)) {
                query.append(hasWhere ? " AND " : " WHERE ");
                query.append("f.fund_name = ? ");
                params.add(fundValue);
                hasWhere = true;
            }
        }

        // Коллекция
        if (filterCollection != null && filterCollection.getValue() != null &&
                !filterCollection.getValue().equals("Все коллекции") &&
                !filterCollection.getValue().equals("Сначала выберите фонд")) {
            query.append(hasWhere ? " AND " : " WHERE ");
            query.append("c.collection_name = ? ");
            params.add(filterCollection.getValue());
            hasWhere = true;
        }

        // Даты
        if (dateFrom != null && dateFrom.getValue() != null) {
            query.append(hasWhere ? " AND " : " WHERE ");
            query.append("e.arrival_date >= ? ");
            params.add(dateFrom.getValue());
            hasWhere = true;
        }

        if (dateTo != null && dateTo.getValue() != null) {
            query.append(hasWhere ? " AND " : " WHERE ");
            query.append("e.arrival_date <= ? ");
            params.add(dateTo.getValue());
            hasWhere = true;
        }

        // Выполнение запроса с параметрами
        ResultSet rs = DBHandler.executeQuery(query.toString(), params.toArray());

        while (rs.next()) {
            Exhibit exhibit = new Exhibit(
                    rs.getInt("id"),
                    rs.getString("photo"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDouble("length"),
                    rs.getDouble("width"),
                    rs.getDouble("height"),
                    rs.getString("unit_sizes"),
                    rs.getDouble("weight"),
                    rs.getString("unit_weight"),
                    rs.getString("color"),
                    rs.getString("material"),
                    rs.getString("dating_material"),
                    rs.getString("technique"),
                    rs.getString("condition_name"),
                    rs.getString("source"),
                    rs.getString("arrival_date"),
                    rs.getString("inscriptions"),
                    rs.getString("place_of_production"),
                    rs.getString("production_time"),
                    rs.getString("publication"),
                    rs.getString("usage"),
                    rs.getString("museum_value"),
                    rs.getString("status_name"),
                    rs.getString("fund_name"),
                    rs.getString("collection_name"),
                    rs.getString("number_kp")
            );
            exhibitsList.add(exhibit);
        }

        exhibitsTable.setItems(exhibitsList);
    }

}
