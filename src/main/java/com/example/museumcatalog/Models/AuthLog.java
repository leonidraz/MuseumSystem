package com.example.museumcatalog.Models;

import javafx.beans.property.*;
import java.time.LocalDateTime;

public class AuthLog {

    private final IntegerProperty id = new SimpleIntegerProperty();
    private final ObjectProperty<Integer> userId = new SimpleObjectProperty<>();
    private final StringProperty login = new SimpleStringProperty();
    private final StringProperty userFio = new SimpleStringProperty();
    private final StringProperty eventType = new SimpleStringProperty();
    private final BooleanProperty success = new SimpleBooleanProperty();
    private final StringProperty ipAddress = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> dateTime = new SimpleObjectProperty<>();

    public AuthLog() {
    }

    public AuthLog(int id,
                   Integer userId,
                   String login,
                   String userFio,
                   String eventType,
                   boolean success,
                   String ipAddress,
                   LocalDateTime dateTime) {

        this.id.set(id);
        this.userId.set(userId);
        this.login.set(login);
        this.userFio.set(userFio);
        this.eventType.set(eventType);
        this.success.set(success);
        this.ipAddress.set(ipAddress);
        this.dateTime.set(dateTime);
    }

    public int getId() {
        return id.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public Integer getUserId() {
        return userId.get();
    }

    public ObjectProperty<Integer> userIdProperty() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId.set(userId);
    }

    public String getLogin() {
        return login.get();
    }

    public StringProperty loginProperty() {
        return login;
    }

    public void setLogin(String login) {
        this.login.set(login);
    }

    public String getUserFio() {
        return userFio.get();
    }

    public StringProperty userFioProperty() {
        return userFio;
    }

    public void setUserFio(String userFio) {
        this.userFio.set(userFio);
    }

    public String getEventType() {
        return eventType.get();
    }

    public StringProperty eventTypeProperty() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType.set(eventType);
    }

    public boolean isSuccess() {
        return success.get();
    }

    public BooleanProperty successProperty() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success.set(success);
    }

    public String getSuccessText() {
        return isSuccess() ? "успешно" : "неуспешно";
    }

    public String getIpAddress() {
        return ipAddress.get();
    }

    public StringProperty ipAddressProperty() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress.set(ipAddress);
    }

    public LocalDateTime getDateTime() {
        return dateTime.get();
    }

    public ObjectProperty<LocalDateTime> dateTimeProperty() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime.set(dateTime);
    }
}