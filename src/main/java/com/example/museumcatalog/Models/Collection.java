package com.example.museumcatalog.Models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Collection {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty collectionName = new SimpleStringProperty();
    private final StringProperty fundName = new SimpleStringProperty();
    private final StringProperty collectionDescription = new SimpleStringProperty();

    public Collection() {}

    public Collection(int id, String collectionName, String fundName, String collectionDescription) {
        setId(id);
        setCollectionName(collectionName);
        setFundName(fundName);
        setCollectionDescription(collectionDescription);
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

    public String getCollectionName() {
        return collectionName.get();
    }

    public void setCollectionName(String collectionName) {
        this.collectionName.set(collectionName);
    }

    public StringProperty collectionNameProperty() {
        return collectionName;
    }

    public String getFundName() {
        return fundName.get();
    }

    public void setFundName(String fundName) {
        this.fundName.set(fundName);
    }

    public StringProperty fundNameProperty() {
        return fundName;
    }

    public String getCollectionDescription() {
        return collectionDescription.get();
    }

    public void setCollectionDescription(String collectionDescription) {
        this.collectionDescription.set(collectionDescription);
    }

    public StringProperty collectionDescriptionProperty() {
        return collectionDescription;
    }
}
