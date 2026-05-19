package com.example.museumcatalog.Models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class EmployeePosition {

    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty positionName = new SimpleStringProperty();

    public EmployeePosition() {
    }

    public EmployeePosition(int id, String positionName) {
        this.id.set(id);
        this.positionName.set(positionName);
    }

    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public String getPositionName() {
        return positionName.get();
    }

    public void setPositionName(String positionName) {
        this.positionName.set(positionName);
    }

    public StringProperty positionNameProperty() {
        return positionName;
    }
}