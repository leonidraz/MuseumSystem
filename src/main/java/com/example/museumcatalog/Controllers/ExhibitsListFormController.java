package com.example.museumcatalog.Controllers;

import com.example.museumcatalog.*;
import com.example.museumcatalog.Models.Exhibit;
import com.example.museumcatalog.Models.Owner;
import com.example.museumcatalog.Storages.ExhibitRepository;
import com.example.museumcatalog.Storages.OwnerRepository;
import com.example.museumcatalog.Storages.ReportRepository;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ExhibitsListFormController {
    //Центральная таблица exhibitsTable
    @FXML
    private TableView<Exhibit> exhibitsTable;

    //Колонки таблицы exhibitsTable
    @FXML private TableColumn<Exhibit, String> nameColumn;
    @FXML private TableColumn<Exhibit, String> descColumn;
    @FXML private TableColumn<Exhibit, String> ownerShortName;
    @FXML private TableColumn<Exhibit, Double> lengthColumn;
    @FXML private TableColumn<Exhibit, Double> widthColumn;
    @FXML private TableColumn<Exhibit, Double> heightColumn;
    @FXML private TableColumn<Exhibit, String> unitSizesColumn;
    @FXML private TableColumn<Exhibit, Double> weightColumn;
    @FXML private TableColumn<Exhibit, String> unitWeightColumn;
    @FXML private TableColumn<Exhibit, String> colorColumn;
    @FXML private TableColumn<Exhibit, String> materialColumn;
    @FXML private TableColumn<Exhibit, String> datingMaterialColumn;
    @FXML private TableColumn<Exhibit, String> techniqueColumn;
    @FXML private TableColumn<Exhibit, String> conditionColumn;
    @FXML private TableColumn<Exhibit, String> sourceColumn;
    @FXML private TableColumn<Exhibit, LocalDate> arrivalDateColumn;
    @FXML private TableColumn<Exhibit, String> inscriptionsColumn;
    @FXML private TableColumn<Exhibit, String> placeOfProductionColumn;
    @FXML private TableColumn<Exhibit, String> productionTimeColumn;
    @FXML private TableColumn<Exhibit, String> publicationColumn;
    @FXML private TableColumn<Exhibit, String> usageColumn;
    @FXML private TableColumn<Exhibit, String> museumValueColumn;
    @FXML private TableColumn<Exhibit, String> statusColumn;
    @FXML private TableColumn<Exhibit, String> fundColumn;
    @FXML private TableColumn<Exhibit, String> collectionColumn;
    @FXML private TableColumn<Exhibit, String> numberKPColumn;

    //Кнопки
    @FXML private Button refreshBtn;
    @FXML private Button addBtn;
    @FXML private Button exportBtn;
    @FXML private Button historyBtn;
    @FXML private Button editBtn;
    @FXML private Button deleteBtn;
    @FXML private Button archiveBtn;
    @FXML private Button closeDetailsCard;
    @FXML private Button returnToFundBtn;

    //Элементы детальной карточки предмета
    @FXML private ImageView imageView;
    @FXML private Label name;
    @FXML private Circle statusCircle;
    @FXML private Label status;
    @FXML private Label KPPNumber;
        //Вкладка "Основное"
    @FXML private Label arrivalDate;
    @FXML private Label description;
    @FXML private Label fund;
    @FXML private Label collection;
    @FXML private Label owner;
        //Вкладка "Характеристики"
    @FXML private Label dimensions;
    @FXML private Label weight;
    @FXML private Label color;
    @FXML private Label material;
    @FXML private Label datingMaterial;
    @FXML private Label technique;
        //Вкладка "Дополнительно"
    @FXML private Label source;
    @FXML private Label condition;
    @FXML private Label inscriptions;
    @FXML private Label productionPlace;
    @FXML private Label productionTime;
    @FXML private Label publications;
    @FXML private Label usage;
    @FXML private Label museumValue;

    //Элементы поиска/фильтрации
    @FXML private ComboBox<String> filterStatus;
    @FXML private ComboBox<String> filterFund;
    @FXML private ComboBox<String> filterCollection;
    @FXML private DatePicker dateFrom;
    @FXML private DatePicker dateTo;
    @FXML private TextField ownerSearch;
    @FXML private TextField search;

    //Другие элементы
    @FXML private VBox centerVBox;
    @FXML private BorderPane rootPane;
    @FXML private Label totalCountLabel;
    @FXML private VBox detailsCard;
    @FXML private VBox contentDetailsCard;

    private static final Map<String, String> STATUS_COLORS = Map.of(
            "На временном хранении", "#2196F3",
            "На рассмотрении ЭФЗК", "#FF9800",
            "Принят ЭФЗК", "#4CAF50",
            "Возвращен владельцу", "#9C27B0",
            "В фонде", "#00BCD4",
            "На выставке", "#FF5722",
            "Выдан организации", "#795548",
            "Архивирован", "#607D8B",
            "В обработке", "#607D8B"
    );

    public static final String STATUS_ON_EXHIBITION = "На выставке";
    public static final String STATUS_ISSUED = "Выдан";
    public static final String STATUS_IN_FUND = "В фонде";
    Service service = new Service();
    private FilteredList<Exhibit> filteredExhibits;

    public void initialize() throws SQLException {
        setupTableColumns();
        loadExhibits();
        setupListeners();
        setupButtonHandlers();
        resetDetailsCard();

        if (Service.getExhibit() != null) {
            exhibitsTable.getItems().stream()
                    .filter(e -> e.getId() == Service.getExhibit().getId())
                    .findFirst()
                    .ifPresent(e -> {
                        exhibitsTable.getSelectionModel().select(e);
                        exhibitsTable.scrollTo(e);
                        loadExhibitInfo(e);
                        animateDetailsCard(true);
                    });
        }

        //Загрузка списка значений ComboBox для фильтров
        filterStatus.getItems().add("Все статусы");
        filterStatus.getItems().addAll(service.getValuesComboBox("exhibit_statuses", "status_name", null));
        filterStatus.setValue("Все статусы");
        filterFund.getItems().addAll("Все записи", "Все фонды", "Без фонда");
        filterFund.getItems().addAll(service.getValuesComboBox("funds", "fund_name", null));
        filterFund.setValue("Все записи");

        //Скрытие коллекций при значении "Все записи"
        updateCollectionsComboBox(filterFund.getValue());

        detailsCard.setOnMouseClicked(actionEvent -> {
            animateDetailsCard(true);
        });

        //Cлушатель на выбор строки
        exhibitsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                loadExhibitInfo(newSelection);
                animateDetailsCard(true);
            }
        });
    }

    private void setupTableColumns() {
        //Связываем колонки с полями модели
        exhibitsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        nameColumn.setCellValueFactory(cell -> cell.getValue().nameProperty());
        descColumn.setCellValueFactory(cell -> cell.getValue().descriptionProperty());
        lengthColumn.setCellValueFactory(cell -> cell.getValue().lengthProperty().asObject());
        widthColumn.setCellValueFactory(cell -> cell.getValue().widthProperty().asObject());
        heightColumn.setCellValueFactory(cell -> cell.getValue().heightProperty().asObject());
        unitSizesColumn.setCellValueFactory(cell -> cell.getValue().unitSizesProperty());
        weightColumn.setCellValueFactory(cell -> cell.getValue().weightProperty());
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
        ownerShortName.setCellValueFactory(cell -> cell.getValue().ownerFioProperty());
    }

    private void loadExhibits() throws SQLException {
        ExhibitRepository.loadAll();
        filteredExhibits = new FilteredList<>(
                ExhibitRepository.getExhibits(),
                p -> true
        );

        SortedList<Exhibit> sortedList = new SortedList<>(filteredExhibits);

        sortedList.comparatorProperty().bind(exhibitsTable.comparatorProperty());
        exhibitsTable.setItems(sortedList);

        totalCountLabel.setText("Всего: " + filteredExhibits.size());
    }

    private void setupListeners() {
        search.textProperty().addListener((obs, o, n) -> applyFilters());
        ownerSearch.textProperty().addListener((obs, o, n) -> applyFilters());

        filterStatus.valueProperty().addListener((obs, o, n) -> applyFilters());
        filterFund.valueProperty().addListener((obs, o, n) -> {
            updateCollectionsComboBox(n);
            applyFilters();
        });
        filterCollection.valueProperty().addListener((obs, o, n) -> applyFilters());
        dateFrom.valueProperty().addListener((obs, o, n) -> applyFilters());
        dateTo.valueProperty().addListener((obs, o, n) -> applyFilters());
    }

    private void applyFilters() {
        filteredExhibits.setPredicate(exhibit -> {
            //Поиск по наименованию и номеру КП
            String text = search.getText();
            if (text != null && !text.isEmpty()) {
                String t = text.toLowerCase();

                String name = exhibit.getName() == null ? "" : exhibit.getName().toLowerCase();
                String kp = exhibit.getNumberKP() == null ? "" : exhibit.getNumberKP().toLowerCase();

                if (!name.contains(t) && !kp.contains(t)) {
                    return false;
                }
            }
            // Фильтрация по ФИО или паспортным данным владельца
            String ownerText = ownerSearch.getText();
            if (ownerText != null && !ownerText.isEmpty()) {

                String t = ownerText.toLowerCase().replaceAll("\\s+", "");

                Owner owner = OwnerRepository.getOwners().stream()
                        .filter(o -> o.getId() == exhibit.getOwnerId())
                        .findFirst()
                        .orElse(null);

                if (owner == null) return false;

                String fio = owner.getFullFio() == null ? "" : owner.getFullFio().toLowerCase().replaceAll("\\s+", "");

                String passport = owner.getPassportNumber() == null ? "" : owner.getPassportNumber().toLowerCase().replaceAll("\\s+", "");

                String series = owner.getPassportSeries() == null ? "" : owner.getPassportSeries().toLowerCase().replaceAll("\\s+", "");

                String fullPassport = series + passport;

                if (!fio.contains(t) && !passport.contains(t) && !series.contains(t) && !fullPassport.contains(t)) {
                    return false;
                }
            }
            //Фильтрация по статусу
            if (filterStatus.getValue() != null && !filterStatus.getValue().equals("Все статусы")) {
                if (!filterStatus.getValue().equals(exhibit.getStatus())) {
                    return false;
                }
            }
            //Фильтрация по фонду
            String fund = filterFund.getValue();
            if (fund != null) {
                if (fund.equals("Все записи")) {

                }
                else if (fund.equals("Все фонды")) {
                    if (exhibit.getFund() == null) {
                        return false;
                    }
                }
                else if (fund.equals("Без фонда")) {
                    if (exhibit.getFund() != null) {
                        return false;
                    }
                }
                else {
                    if (exhibit.getFund() == null || !fund.equals(exhibit.getFund())) {
                        return false;
                    }
                }
            }
            //Фильтрация по коллекции
            if (filterCollection.getValue() != null && !filterCollection.getValue().equals("Все коллекции") && !filterCollection.getValue().equals("Сначала выберите фонд")) {
                if (!filterCollection.getValue().equals(exhibit.getCollection())) {
                    return false;
                }
            }
            //Фильтрация по периоду поступления
            if (dateFrom.getValue() != null && exhibit.getArrivalDate() != null) {
                if (exhibit.getArrivalDate().compareTo(dateFrom.getValue()) < 0) {
                    return false;
                }
            }
            if (dateTo.getValue() != null && exhibit.getArrivalDate() != null) {
                if (exhibit.getArrivalDate().compareTo(dateTo.getValue()) > 0) {
                    return false;
                }
            }
            return true;
        });
        totalCountLabel.setText("Всего: " + filteredExhibits.size());
    }

    private void setupButtonHandlers() {
        refreshBtn.setOnAction(actionEvent -> {
            search.clear();
            ownerSearch.clear();

            filterStatus.setValue("Все статусы");
            filterFund.setValue("Все записи");
            dateFrom.setValue(null);
            dateTo.setValue(null);

            exhibitsTable.getSortOrder().clear();

            applyFilters();
            animateDetailsCard(false);
        });

        closeDetailsCard.setOnAction(actionEvent -> {
            animateDetailsCard(false);
        });

        addBtn.setOnAction(actionEvent -> {
            Service.setExhibit(null);
            try {
                ExhibitRegistrationForm controller = Service.openModal("ExhibitRegistrationForm", "Добавление предмета",
                        (Stage) addBtn.getScene().getWindow());
                if (controller.getResultController() != null) {
                    Exhibit ex = controller.getResultController();
                    exhibitsTable.getSelectionModel().select(ex);
                    loadExhibitInfo(ex);
                    animateDetailsCard(true);
                    totalCountLabel.setText("Всего: " + filteredExhibits.size());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        exportBtn.setOnAction(e -> {

            try {
                Exhibit selected = exhibitsTable.getSelectionModel().getSelectedItem();
                if (selected == null) {service.openAlert(Alert.AlertType.WARNING, "Не выбран предмет для экспорта", "Предупреждение!");
                    return;
                }
                if (selected.getNumberKP() == null || selected.getNumberKP().isBlank()) {
                    service.openAlert(Alert.AlertType.WARNING, "Нельзя экспортировать карточку: предмет не является МП", "Предупреждение!");
                    return;
                }

                FileChooser chooser = new FileChooser();
                chooser.setTitle("Сохранить карточку МП");
                chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Word Document", "*.docx"));
                chooser.setInitialFileName(selected.getName() + ".docx");

                File file = chooser.showSaveDialog(exhibitsTable.getScene().getWindow());

                if (file == null) {
                    service.openAlert(Alert.AlertType.INFORMATION, "Экспорт отменён пользователем", "Уведомление!");
                    return;
                }

                ExhibitCardExport.export(selected, file);
                service.openAlert(Alert.AlertType.INFORMATION, "Карточка МП успешно сохранена", "Уведомление!");

            } catch (Exception ex) {
                ex.printStackTrace();
                service.openAlert(Alert.AlertType.ERROR, "Ошибка при экспорте карточки МП: " + ex.getMessage(), "Ошибка!");
            }
        });

        historyBtn.setOnAction(e -> {
            List<Exhibit> sel = exhibitsTable.getSelectionModel().getSelectedItems();

            if (sel == null || sel.isEmpty()) {
                service.openAlert(Alert.AlertType.WARNING, "Выберите один или несколько предметов", "Предупреждение!");return;
            }

            FileChooser fc = new FileChooser();
            fc.setTitle("Сохранить отчет");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel файл (*.xlsx)", "*.xlsx"));
            fc.setInitialFileName("Отчет о движении_" +
                    DateTimeUtil.formatDateOnly(java.time.LocalDateTime.now()) + ".xlsx");

            File file = fc.showSaveDialog((Stage) historyBtn.getScene().getWindow());
            if (file == null) return;

            try {
                List<Integer> ids = sel.stream().map(Exhibit::getId).toList();
                var data = ReportRepository.getMovementReport(ids);

                ReportExcelService.export(data, file.toPath(), ReportExcelService.ReportType.MOVEMENT);
                service.openAlert(Alert.AlertType.INFORMATION, "Отчет успешно сохранён", "Уведомление!");
            } catch (Exception ex) {
                ex.printStackTrace();
                service.openAlert(Alert.AlertType.ERROR, "Ошибка при экспорте: " + ex.getMessage(), "Ошибка!");
            }
        });

        editBtn.setOnAction(actionEvent -> {
            Exhibit selected = exhibitsTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                service.openAlert(Alert.AlertType.WARNING, "Выберите предмет для редактирования", "Предупреждение");
                return;
            }
            try {
                Service.setExhibit(selected); //передаю объект при редактировании
                Service.openModal("ExhibitRegistrationForm", "Редактирование предмета",
                        (Stage) addBtn.getScene().getWindow());
                loadExhibitInfo(selected);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        deleteBtn.setOnAction(actionEvent -> {
            Exhibit exhibit = exhibitsTable.getSelectionModel().getSelectedItem();
            if (exhibit == null) {
                service.openAlert(Alert.AlertType.WARNING,
                        "Выберите предмет для удаления",
                        "Предупреждение");
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Подтверждение удаления");
            confirm.setHeaderText("Вы уверены, что хотите удалить предмет?");
            confirm.setContentText("Предмет \"" + exhibit.getName() + "\" будет безвозвратно удален из базы данных и файловой системы.");

            if (confirm.showAndWait().get() != ButtonType.OK) {
                return;
            }
            try {
                int result = ExhibitRepository.delete(exhibit.getId());
                if (result > 0) {
                    if (exhibit.getPhoto() != null && !exhibit.getPhoto().isEmpty()) {
                        Files.deleteIfExists(Path.of("images", exhibit.getPhoto()));
                    }
                    ExhibitRepository.getExhibits().remove(exhibit);
                    totalCountLabel.setText("Всего: " + filteredExhibits.size());

                    animateDetailsCard(false);

                    service.openAlert(Alert.AlertType.INFORMATION, "Предмет успешно удален!", "Успешно!");

                } else if (result == -2) {
                    service.openAlert(Alert.AlertType.WARNING, "Невозможно удалить предмет: он связан с одним или несколькими документами.\n" + "Сначала удалите связанные документы.", "Удаление запрещено");
                }
            } catch (Exception e) {
                e.printStackTrace();
                service.openAlert(Alert.AlertType.ERROR, "Ошибка удаления предмета: " + e.getMessage(), "Ошибка");
            }
        });

        archiveBtn.setOnAction(actionEvent -> {
            Exhibit selected = exhibitsTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                service.openAlert(Alert.AlertType.WARNING, "Выберите предмет", "Предупреждение");
                return;
            }
            try {
                if ("Архивирован".equals(selected.getStatus())) {
                    String newStatus = ExhibitRepository.unarchiveExhibit(selected.getId());
                    selected.setStatus(newStatus);
                    service.openAlert(Alert.AlertType.INFORMATION, "Предмет восстановлен из архива", "Успешно");
                    applyFilters();

                } else {
                    ExhibitRepository.archiveExhibit(selected.getId());
                    selected.setStatus("Архивирован");
                    service.openAlert(Alert.AlertType.INFORMATION, "Предмет помещен в архив", "Успешно");
                    applyFilters();
                }

                loadExhibitInfo(selected);

            } catch (SQLException e) {
                service.openAlert(Alert.AlertType.ERROR, "Ошибка: " + e.getMessage(), "Ошибка");
                e.printStackTrace();
            }
        });

        returnToFundBtn.setOnAction(e -> {
            Exhibit selected = exhibitsTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                service.openAlert(Alert.AlertType.WARNING, "Выберите предмет", "Предупреждение");
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Подтверждение возврата");
            confirm.setHeaderText("Вы уверены, что хотите возвратить предмет?");
            confirm.setContentText("Предмет \"" + selected.getName() + "\" поменяет свой статус на 'В фонде'.");

            if (confirm.showAndWait().get() != ButtonType.OK) {
                return;
            }
            try {
                ExhibitRepository.returnToFund(selected.getId());
                selected.setStatus(STATUS_IN_FUND);
                loadExhibitInfo(selected);
                applyFilters();
                service.openAlert(Alert.AlertType.INFORMATION, "Предмет возвращён в фонд", "Успешно");
            } catch (SQLException ex) {
                ex.printStackTrace();
                service.openAlert(Alert.AlertType.ERROR, "Ошибка возврата: " + ex.getMessage(), "Ошибка");
            }
        });
    }

    // Сброс карточки деталей предмета
    private void resetDetailsCard() {
        detailsCard.setPrefWidth(10);
        contentDetailsCard.setVisible(false);
        detailsCard.getStyleClass().add("panel-details-closed");
    }

    private void animateDetailsCard(boolean isOpenDetailsCard) {
        double targetWidth = isOpenDetailsCard ? 443 : 10;
        contentDetailsCard.setVisible(isOpenDetailsCard);

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(300),
                new KeyValue(detailsCard.prefWidthProperty(), targetWidth)));

        timeline.play();

        detailsCard.getStyleClass().removeAll("panel-details-open", "panel-details-closed");

        timeline.setOnFinished(e -> {
            detailsCard.getStyleClass().add(isOpenDetailsCard ? "panel-details-open" : "panel-details-closed");
        });
    }

    private void loadExhibitInfo(Exhibit newSelection) {
        imageView.setImage(new Image(Objects.requireNonNull(getClass().getResource("/com/example/museumcatalog/images/picture.png")).toExternalForm()));
        if (newSelection.photoProperty().get() != null && !newSelection.photoProperty().get().isEmpty() && Files.exists(Path.of("images", newSelection.photoProperty().get()))) {
            imageView.setImage(new Image(new File("images/" + newSelection.photoProperty().get()).toURI().toString()));
        }

        archiveBtn.setText("Архивирован".equals(newSelection.getStatus()) ? "Разархивировать" : "Архивировать");
        updateReturnToFundButtonVisibility(newSelection);

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
        owner.setText(nonNullOrDash(newSelection.ownerFioProperty().get()));

        dimensions.setText(nonNullOrDash(newSelection.lengthProperty().get()) + " x " +
                nonNullOrDash(newSelection.widthProperty().get()) + " x " +
                nonNullOrDash(newSelection.heightProperty().get()) + " " + nonNullOrDash(newSelection.unitSizesProperty().get()));
        Double w = newSelection.weightProperty().get();
        String unit = newSelection.getUnitWeight();

        weight.setText(nonNullOrDash(w) + (w == null ? "" : " " + unit));
        color.setText(nonNullOrDash(newSelection.colorProperty().get()));
        material.setText(nonNullOrDash(newSelection.materialProperty().get()));
        datingMaterial.setText(nonNullOrDash(newSelection.datingMaterialProperty().get()));
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

    private void updateReturnToFundButtonVisibility(Exhibit exhibit) {
        String status = exhibit.getStatus();
        boolean canReturn = STATUS_ON_EXHIBITION.equals(status) || STATUS_ISSUED.equals(status);
        returnToFundBtn.setVisible(canReturn);
        returnToFundBtn.setManaged(canReturn);
    }

    private String nonNullOrDash(String value) {
        return (value == null || value.isEmpty()) ? "—" : value;
    }
    private String nonNullOrDash(Double value) {
        return (value == null || value == 0.0) ? "—" : value.toString();
    }

    private String nonNullOrDash(LocalDate value) {
        return (value == null) ? "—" : value.toString();
    }

    private void updateCollectionsComboBox(String selectedFund) {
        filterCollection.getItems().clear();
        if (selectedFund.equals("Все фонды") || selectedFund.equals("Все записи") || selectedFund.equals("Без фонда")) {
            filterCollection.setDisable(true);
            filterCollection.getItems().add("Сначала выберите фонд");
            filterCollection.setValue(filterCollection.getItems().getFirst());
        } else {
            try {
                filterCollection.setDisable(false);
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
}
