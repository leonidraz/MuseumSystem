package com.example.museumcatalog;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.transform.Scale;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {
    private static Stage currentStage;

    private static boolean isAuth = true;

    public static void setIsAuth(boolean isAuth) {
        MainApplication.isAuth = isAuth;
    }


    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("fxml/" + "АuthorizationForm.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        applyScale(scene);
        stage.setTitle("Авторизация");
        stage.setScene(scene);
        stage.show();
        setCurrentStage(stage);
    }

    public static Stage getCurrentStage() {
        return currentStage;
    }

    public static void setCurrentStage(Stage currentStage) {
        MainApplication.currentStage = currentStage;
    }

    public static void main(String[] args) {
        launch();
    }

    public static void applyScale(Scene scene) {
        double baseWidth = 1920;
        double baseHeight = 1080;

        ChangeListener<Number> listener = (obs, oldVal, newVal) -> {
            updateScale(scene, baseWidth, baseHeight);
        };

        scene.widthProperty().addListener(listener);
        scene.heightProperty().addListener(listener);

        // первый запуск после отображения
        Platform.runLater(() -> updateScale(scene, baseWidth, baseHeight));
    }

    private static void updateScale(Scene scene, double baseWidth, double baseHeight) {

        double w = scene.getWidth();
        double h = scene.getHeight();

        if (w <= 0 || h <= 0) return;

        if (isAuth) {
            scene.getRoot().getTransforms().setAll();
            return;
        }

        double scale = Math.min(w / baseWidth, h / baseHeight) * 1.08;
        scene.getRoot().getTransforms().setAll(new Scale(scale, scale));
    }
}