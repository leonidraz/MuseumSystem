package com.example.museumcatalog.Controllers;

import com.example.museumcatalog.DBHandler;
import com.example.museumcatalog.Models.User;
import com.example.museumcatalog.Service;
import com.example.museumcatalog.Storages.AuthLogRepository;
import com.example.museumcatalog.Storages.UserRepository;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthorizationFormController {

    @FXML
    private Button enterBtn;

    @FXML
    private TextField loginTF;

    @FXML
    private PasswordField passwordTF;
    Service service = new Service();

    public void initialize() {
        enterBtn.setOnAction(actionEvent -> {
            String login = loginTF.getText();
            String password = passwordTF.getText();

            User user = null;
            boolean success = false;

            try {
                DBHandler.setConnection();
                user = UserRepository.checkUser(login, password);
            } catch (SQLException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            if (user != null) {
                success = true;

                try {
                    AuthLogRepository.addLog(user, login, success, "LOGIN");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                Service.setCurrentUser(user);
                if (Service.getCurrentUser().getStatus().equals("активен")) {
                    try {
                        service.switchScene("BaseForm", "Главная");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    service.openAlert(Alert.AlertType.WARNING, "Данный пользователь больше неактивен!", "Предупреждение!");
                }
            } else {
                if (login.isEmpty() || password.isEmpty()) {
                    service.openAlert(Alert.AlertType.WARNING, "Заполните, пожалуйста, все поля!", "Предупреждение!");
                    return;
                }
                try {
                    AuthLogRepository.addLog(null, login, success, "LOGIN");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                service.openAlert(Alert.AlertType.WARNING, "Неверный логин или пароль!", "Предупреждение!");
            }
        });
    }
}