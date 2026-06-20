package com.example.museumcatalog.Models;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDateTime;

public class Document {
    private final ObjectProperty<Integer> id = new SimpleObjectProperty<>();
    private final StringProperty docNumber = new SimpleStringProperty();
    private final StringProperty docType = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> docDate = new SimpleObjectProperty<>();
    private final StringProperty owner = new SimpleStringProperty();
    private final ObjectProperty<Integer> ownerId = new SimpleObjectProperty<>();
    private final ObjectProperty<Integer> exhibitsCount = new SimpleObjectProperty<>();
    private final StringProperty docStatus = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> conductedDate = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> changeDate = new SimpleObjectProperty<>();
    private final StringProperty createdBy = new SimpleStringProperty();
    private final StringProperty updatedBy = new SimpleStringProperty();
    private final StringProperty base = new SimpleStringProperty();

    public Document() {}

    public Document(Integer id, String docNumber, String docType, LocalDateTime docDate, String owner, Integer ownerId, Integer exhibitsCount, String docStatus, LocalDateTime changeDate, LocalDateTime conductedDate, String createdBy, String updateBy, String base) {
        this.id.set(id);
        this.docNumber.set(docNumber);
        this.docType.set(docType);
        this.docDate.set(docDate);
        this.owner.set(owner);
        this.ownerId.set(ownerId);
        this.exhibitsCount.set(exhibitsCount);
        this.docStatus.set(docStatus);
        this.conductedDate.set(conductedDate);
        this.changeDate.set(changeDate);
        this.createdBy.set(createdBy);
        this.updatedBy.set(updateBy);
        this.base.set(base);
    }

    public Integer getId() {
        return id.get();
    }

    public void setId(Integer id) {
        this.id.set(id);
    }

    public ObjectProperty<Integer> idProperty() {
        return id;
    }

    public String getDocNumber() {
        return docNumber.get();
    }

    public void setDocNumber(String docNumber) {
        this.docNumber.set(docNumber);
    }

    public StringProperty docNumberProperty() {
        return docNumber;
    }

    public String getDocType() {
        return docType.get();
    }

    public void setDocType(String docType) {
        this.docType.set(docType);
    }

    public StringProperty docTypeProperty() {
        return docType;
    }

    public LocalDateTime getDocDate() {
        return docDate.get();
    }

    public void setDocDate(LocalDateTime docDate) {
        this.docDate.set(docDate);
    }

    public ObjectProperty<LocalDateTime> docDateProperty() {
        return docDate;
    }

    public String getOwner() {
        return owner.get();
    }

    public void setOwner(String owner) {
        this.owner.set(owner);
    }

    public StringProperty ownerProperty() {
        return owner;
    }

    public Integer getOwnerId() {
        return ownerId.get();
    }

    public void setOwnerId(Integer ownerId) {
        this.ownerId.set(ownerId);
    }

    public ObjectProperty<Integer> ownerIdProperty() {
        return ownerId;
    }

    public Integer getExhibitsCount() {
        return exhibitsCount.get();
    }

    public void setExhibitsCount(Integer exhibitsCount) {
        this.exhibitsCount.set(exhibitsCount);
    }

    public ObjectProperty<Integer> exhibitsCountProperty() {
        return exhibitsCount;
    }

    public String getDocStatus() {
        return docStatus.get();
    }

    public void setDocStatus(String docStatus) {
        this.docStatus.set(docStatus);
    }

    public StringProperty docStatusProperty() {
        return docStatus;
    }

    public LocalDateTime getConductedDate() {
        return conductedDate.get();
    }

    public void setConductedDate(LocalDateTime conductedDate) {
        this.conductedDate.set(conductedDate);
    }

    public LocalDateTime getChangeDate() {
        return changeDate.get();
    }

    public void setChangeDate(LocalDateTime changeDate) {
        this.changeDate.set(changeDate);
    }

    public ObjectProperty<LocalDateTime> changeDateProperty() {
        return changeDate;
    }

    public ObjectProperty<LocalDateTime> conductedDateProperty() {
        return conductedDate;
    }

    public String getCreatedBy() {
        return createdBy.get();
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy.set(createdBy);
    }

    public StringProperty createdByProperty() {
        return createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy.get();
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy.set(updatedBy);
    }

    public StringProperty updatedByProperty() {
        return updatedBy;
    }

    public String getBase() {
        return base.get();
    }

    public void setBase(String base) {
        this.base.set(base);
    }

    public StringProperty baseProperty() {
        return base;
    }
}