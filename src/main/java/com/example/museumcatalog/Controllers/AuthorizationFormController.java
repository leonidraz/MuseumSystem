package com.example.museumcatalog.Controllers;

import com.example.museumcatalog.DBHandler;
import com.example.museumcatalog.Models.User;
import com.example.museumcatalog.Service;
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

            try {
                DBHandler.setConnection();
                user = checkUser(login, password);
            } catch (SQLException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            if (user != null) {
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
                service.openAlert(Alert.AlertType.WARNING, "Неверный логин или пароль!", "Предупреждение!");
            }
        });
    }

    private User checkUser(String login, String password) throws SQLException {
        String query = String.format("select r.role_name , e.last_name, e.first_name, e.middle_name, us.status_name\n" +
                "FROM users u\n" +
                "inner join roles r on u.role_id = r.id\n" +
                "inner join employees e on u.employee_id = e.id\n" +
                "inner join user_statuses us on u.status_id  = us.id\n" +
                "where u.login = '%s' and \"password\" = crypt('%s', \"password\")", login, password);

        ResultSet rs = DBHandler.executeQuery(query);
        if (rs.next()) {
            String role = rs.getString("role_name");
            String lastName = rs.getString("last_name");
            String firstName = rs.getString("first_name");
            String middleName = rs.getString("middle_name");
            String status = rs.getString("status_name");

            return new User(role, lastName, firstName, middleName, status);
        }
        return null;
    }
}