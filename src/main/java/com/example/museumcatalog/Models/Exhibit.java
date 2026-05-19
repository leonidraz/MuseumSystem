package com.example.museumcatalog.Models;

import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;

import java.time.LocalDate;

public class Exhibit {

    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty photo = new SimpleStringProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final DoubleProperty length = new SimpleDoubleProperty();
    private final DoubleProperty width = new SimpleDoubleProperty();
    private final DoubleProperty height = new SimpleDoubleProperty();
    private final ObjectProperty<Double> weight = new SimpleObjectProperty<>();
    private final StringProperty unitSizes = new SimpleStringProperty();
    private final StringProperty unitWeight = new SimpleStringProperty();
    private final StringProperty color = new SimpleStringProperty();
    private final StringProperty material = new SimpleStringProperty();
    private final StringProperty datingMaterial = new SimpleStringProperty();
    private final StringProperty technique = new SimpleStringProperty();
    private final StringProperty condition = new SimpleStringProperty();
    private final StringProperty source = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> arrivalDate = new SimpleObjectProperty<>();
    private final StringProperty inscriptions = new SimpleStringProperty();
    private final StringProperty placeOfProduction = new SimpleStringProperty();
    private final StringProperty productionTime = new SimpleStringProperty();
    private final StringProperty publication = new SimpleStringProperty();
    private final StringProperty usage = new SimpleStringProperty();
    private final StringProperty museumValue = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();
    private final StringProperty fund = new SimpleStringProperty();
    private final StringProperty collection = new SimpleStringProperty();
    private final StringProperty numberKP = new SimpleStringProperty();
    private final BooleanProperty selected = new SimpleBooleanProperty(false);
    private final StringProperty ownerFio = new SimpleStringProperty();
    private final ObjectProperty<Integer> ownerId = new SimpleObjectProperty<>();
    private final StringProperty location = new SimpleStringProperty();
    private final StringProperty conditionDetails = new SimpleStringProperty();

    private BooleanProperty selectable = new SimpleBooleanProperty(true);

    public Exhibit() {

    }

    public Exhibit(int id, String photo, String name, String description,
                   double length, double width, double height, String unitSizes,
                   Double weight, String unitWeight, String color, String material,
                   String datingMaterial, String technique, String condition, String source,
                   LocalDate arrivalDate, String inscriptions, String placeOfProduction,
                   String productionTime, String publication, String usage,
                   String museumValue, String status, String fund,
                   String collection, String numberKP, String ownerFio, int ownerId, String location,
                   String conditionDetails) {

        setId(id);
        setPhoto(photo);
        setName(name);
        setDescription(description);
        setLength(length);
        setWidth(width);
        setHeight(height);
        setUnitSizes(unitSizes);
        setWeight(weight);
        setUnitWeight(unitWeight);
        setColor(color);
        setMaterial(material);
        setDatingMaterial(datingMaterial);
        setTechnique(technique);
        setCondition(condition);
        setSource(source);
        setArrivalDate(arrivalDate);
        setInscriptions(inscriptions);
        setPlaceOfProduction(placeOfProduction);
        setProductionTime(productionTime);
        setPublication(publication);
        setUsage(usage);
        setMuseumValue(museumValue);
        setStatus(status);
        setFund(fund);
        setCollection(collection);
        setNumberKP(numberKP);
        setOwnerFio(ownerFio);
        setOwnerId(ownerId);
        setLocation(location);
        setConditionDetails(conditionDetails);
    }


    public Integer getOwnerId() { return ownerId.get(); }
    public void setOwnerId(Integer value) { ownerId.set(value); }
    public ObjectProperty<Integer> ownerIdProperty() { return ownerId; }

    public Integer getIdValue() {
        return id.get() == 0 ? null : id.get();
    }

    public String getOwnerFio() { return ownerFio.get(); }
    public void setOwnerFio(String value) { ownerFio.set(value); }
    public StringProperty ownerFioProperty() { return ownerFio; }

    public BooleanProperty selectableProperty() {
        return selectable;
    }

    public boolean isSelectable() {
        return selectable.get();
    }

    public void setSelectable(boolean value) {
        selectable.set(value);
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    public boolean isSelected() {
        return selected.get();
    }

    public void setSelected(boolean value) {
        selected.set(value);
    }

    // --- ID ---
    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    // --- Photo ---
    public String getPhoto() { return photo.get(); }
    public void setPhoto(String value) { photo.set(value); }
    public StringProperty photoProperty() { return photo; }

    // --- Name ---
    public String getName() { return name.get(); }
    public void setName(String value) { name.set(value); }
    public StringProperty nameProperty() { return name; }

    // --- Description ---
    public String getDescription() { return description.get(); }
    public void setDescription(String value) { description.set(value); }
    public StringProperty descriptionProperty() { return description; }

    // --- Dimensions ---
    public double getLength() { return length.get(); }
    public void setLength(double value) { length.set(value); }
    public DoubleProperty lengthProperty() { return length; }

    public double getWidth() { return width.get(); }
    public void setWidth(double value) { width.set(value); }
    public DoubleProperty widthProperty() { return width; }

    public double getHeight() { return height.get(); }
    public void setHeight(double value) { height.set(value); }
    public DoubleProperty heightProperty() { return height; }

    public String getUnitSizes() { return unitSizes.get(); }
    public void setUnitSizes(String value) { unitSizes.set(value); }
    public StringProperty unitSizesProperty() { return unitSizes; }

    // --- Weight ---
    public Double getWeight() {
        return weight.get();
    }
    public void setWeight(Double value) {
        weight.set(value);
    }
    public ObjectProperty<Double> weightProperty() {
        return weight;
    }

    public String getUnitWeight() { return unitWeight.get(); }
    public void setUnitWeight(String value) { unitWeight.set(value); }
    public StringProperty unitWeightProperty() { return unitWeight; }

    // --- Other fields ---
    public String getColor() { return color.get(); }
    public void setColor(String value) { color.set(value); }
    public StringProperty colorProperty() { return color; }

    public String getMaterial() { return material.get(); }
    public void setMaterial(String value) { material.set(value); }
    public StringProperty materialProperty() { return material; }

    public String getDatingMaterial() { return datingMaterial.get(); }
    public void setDatingMaterial(String value) { datingMaterial.set(value); }
    public StringProperty datingMaterialProperty() { return datingMaterial; }

    public String getTechnique() { return technique.get(); }
    public void setTechnique(String value) { technique.set(value); }
    public StringProperty techniqueProperty() { return technique; }

    public String getCondition() { return condition.get(); }
    public void setCondition(String value) { condition.set(value); }
    public StringProperty conditionProperty() { return condition; }

    public String getSource() { return source.get(); }
    public void setSource(String value) { source.set(value); }
    public StringProperty sourceProperty() { return source; }

    public LocalDate getArrivalDate() { return arrivalDate.get(); }
    public void setArrivalDate(LocalDate value) { arrivalDate.set(value); }
    public ObjectProperty<LocalDate> arrivalDateProperty() { return arrivalDate; }

    public String getInscriptions() { return inscriptions.get(); }
    public void setInscriptions(String value) { inscriptions.set(value); }
    public StringProperty inscriptionsProperty() { return inscriptions; }

    public String getPlaceOfProduction() { return placeOfProduction.get(); }
    public void setPlaceOfProduction(String value) { placeOfProduction.set(value); }
    public StringProperty placeOfProductionProperty() { return placeOfProduction; }

    public String getProductionTime() { return productionTime.get(); }
    public void setProductionTime(String value) { productionTime.set(value); }
    public StringProperty productionTimeProperty() { return productionTime; }

    public String getPublication() { return publication.get(); }
    public void setPublication(String value) { publication.set(value); }
    public StringProperty publicationProperty() { return publication; }

    public String getUsage() { return usage.get(); }
    public void setUsage(String value) { usage.set(value); }
    public StringProperty usageProperty() { return usage; }

    public String getMuseumValue() { return museumValue.get(); }
    public void setMuseumValue(String value) { museumValue.set(value); }
    public StringProperty museumValueProperty() { return museumValue; }

    public String getStatus() { return status.get(); }
    public void setStatus(String value) { status.set(value); }
    public StringProperty statusProperty() { return status; }

    public String getFund() { return fund.get(); }
    public void setFund(String value) { fund.set(value); }
    public StringProperty fundProperty() { return fund; }

    public String getCollection() { return collection.get(); }
    public void setCollection(String value) { collection.set(value); }
    public StringProperty collectionProperty() { return collection; }

    public String getNumberKP() { return numberKP.get(); }
    public void setNumberKP(String value) { numberKP.set(value); }
    public StringProperty numberKPProperty() { return numberKP; }

    public String getLocation() { return location.get(); }
    public void setLocation(String value) { location.set(value); }
    public StringProperty locationProperty() { return location; }

    public String getConditionDetails() { return conditionDetails.get(); }
    public void setConditionDetails(String value) { conditionDetails.set(value); }
    public StringProperty conditionDetailsProperty() { return conditionDetails; }
}