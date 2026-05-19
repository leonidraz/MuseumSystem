package com.example.museumcatalog.Models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Fund {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty fundName = new SimpleStringProperty();
    private final StringProperty fundDescription = new SimpleStringProperty();

    private final StringProperty kpPrefix = new SimpleStringProperty();

    public Fund() {}

    public Fund(int id, String fundName, String fundDescription, String kpPrefix) {
        setId(id);
        setFundName(fundName);
        setFundDescription(fundDescription);
        setKpPrefix(kpPrefix);
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

    public String getFundName() {
        return fundName.get();
    }

    public void setFundName(String fundName) {
        this.fundName.set(fundName);
    }

    public StringProperty fundNameProperty() {
        return fundName;
    }

    public String getFundDescription() {
        return fundDescription.get();
    }

    public void setFundDescription(String fundDescription) {
        this.fundDescription.set(fundDescription);
    }

    public StringProperty fundDescriptionProperty() {
        return fundDescription;
    }

    public String getKpPrefix() {
        return kpPrefix.get();
    }

    public void setKpPrefix(String kpPrefix) { this.kpPrefix.set(kpPrefix); }

    public StringProperty kpPrefixProperty() {
        return kpPrefix;
    }
}
