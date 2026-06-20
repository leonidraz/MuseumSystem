package com.example.museumcatalog.Controllers;

import com.example.museumcatalog.Models.Exhibit;
import com.example.museumcatalog.Service;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class ExhibitCardController {

    @FXML private VBox rootCard;
    @FXML private ImageView imageView;
    @FXML private Label nameLabel;
    @FXML private Label dateLabel;
    @FXML private Label statusLabel;

    private Exhibit exhibit;

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

    public void initialize() {
        rootCard.setOnMouseClicked(mouseEvent -> {
            Service.setExhibit(exhibit);
            try {
                Service.getBaseFormController().switchContent("ExhibitsListForm");
                Service.getBaseFormController().setActive(Service.getBaseFormController().exhibitsLabel);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    public void setData(Exhibit exhibit) {
        this.exhibit = exhibit;
        nameLabel.setText(exhibit.getName());
        dateLabel.setText("Дата поступления: " + exhibit.getArrivalDate());
        statusLabel.setText("Статус: " + exhibit.getStatus());
        statusLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + getStatusColor(exhibit.getStatus()));
        loadImage();
    }

    private void loadImage() {
        if (exhibit.getPhoto() != null && !exhibit.getPhoto().isEmpty() && Files.exists(Path.of("images", exhibit.getPhoto()))) {
            imageView.setImage(new Image(new File("images/" + exhibit.getPhoto()).toURI().toString()));
        }
    }

    private String getStatusColor(String status) {
        return STATUS_COLORS.getOrDefault(status, "#607D8B");
    }
}