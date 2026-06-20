package com.example.museumcatalog.Models;

import java.util.List;

public class FullDocumentData {

    private Document document;

    private Owner owner;

    private List<DocumentEmployeeRelation> employees;
    private List<Exhibit> exhibits;

    private EfzkData efzkData;
    private InternalTransferData internalTransferData;
    private TemporaryStorageData temporaryStorageData;

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public List<DocumentEmployeeRelation> getEmployees() {
        return employees;
    }

    public void setEmployees(List<DocumentEmployeeRelation> employees) {
        this.employees = employees;
    }

    public List<Exhibit> getExhibits() {
        return exhibits;
    }

    public void setExhibits(List<Exhibit> exhibits) {
        this.exhibits = exhibits;
    }

    public EfzkData getEfzkData() {
        return efzkData;
    }

    public void setEfzkData(EfzkData efzkData) {
        this.efzkData = efzkData;
    }

    public InternalTransferData getInternalTransferData() {
        return internalTransferData;
    }

    public void setInternalTransferData(InternalTransferData internalTransferData) {
        this.internalTransferData = internalTransferData;
    }

    public TemporaryStorageData getTemporaryStorageData() {
        return temporaryStorageData;
    }

    public void setTemporaryStorageData(TemporaryStorageData temporaryStorageData) {
        this.temporaryStorageData = temporaryStorageData;
    }
}