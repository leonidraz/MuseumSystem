package com.example.museumcatalog.Models;

import javafx.beans.property.*;

public class User {

    private final IntegerProperty id = new SimpleIntegerProperty();

    private final StringProperty role = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();

    private final StringProperty employeeFio = new SimpleStringProperty();

    private final ObjectProperty<Integer> employeeId = new SimpleObjectProperty<>();

    private final StringProperty login = new SimpleStringProperty();
    private final StringProperty password = new SimpleStringProperty();

    public User() {}

    public User(int id, String role, String status,
                String employeeFio,
                Integer employeeId,
                String login, String password) {

        setId(id);
        setRole(role);
        setStatus(status);
        setEmployeeFio(employeeFio);
        setEmployeeId(employeeId);
        setLogin(login);
        setPassword(password);
    }

    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    public String getRole() { return role.get(); }
    public void setRole(String role) { this.role.set(role); }
    public StringProperty roleProperty() { return role; }

    public String getStatus() { return status.get(); }
    public void setStatus(String status) { this.status.set(status); }
    public StringProperty statusProperty() { return status; }

    public String getEmployeeFio() { return employeeFio.get(); }
    public void setEmployeeFio(String employeeFio) { this.employeeFio.set(employeeFio); }
    public StringProperty employeeFioProperty() { return employeeFio; }

    public Integer getEmployeeId() { return employeeId.get(); }
    public void setEmployeeId(Integer employeeId) { this.employeeId.set(employeeId); }
    public ObjectProperty<Integer> employeeIdProperty() { return employeeId; }

    public String getLogin() { return login.get(); }
    public void setLogin(String login) { this.login.set(login); }
    public StringProperty loginProperty() { return login; }

    public String getPassword() { return password.get(); }
    public void setPassword(String password) { this.password.set(password); }
    public StringProperty passwordProperty() { return password; }

    public String getFullFio() {
        return employeeFio.get();
    }

    public String getShortFio() {
        String fio = employeeFio.get();
        if (fio == null || fio.isBlank()) {
            return "— — —";
        }
        String[] parts = fio.trim().split("\\s+");

        if (parts.length == 0) {
            return "";
        }
        StringBuilder res = new StringBuilder(parts[0]);
        if (parts.length > 1 && !parts[1].isEmpty()) {
            res.append(" ").append(parts[1].charAt(0)).append(".");
        }
        if (parts.length > 2 && !parts[2].isEmpty()) {
            res.append(" ").append(parts[2].charAt(0)).append(".");
        }
        return res.toString();
    }
}