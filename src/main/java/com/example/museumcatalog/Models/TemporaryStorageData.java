package com.example.museumcatalog.Models;

public class TemporaryStorageData {
    String receiverType;
    String receiverName;
    String receiverIdentifier;
    String receiverAddress;
    String admissionPurpose;

    public TemporaryStorageData(String receiverType, String receiverName, String receiverIdentifier, String receiverAddress, String admissionPurpose) {
        this.receiverType = receiverType;
        this.receiverName = receiverName;
        this.receiverIdentifier = receiverIdentifier;
        this.receiverAddress = receiverAddress;
        this.admissionPurpose = admissionPurpose;
    }

    public String getReceiverType() {
        return receiverType;
    }

    public void setReceiverType(String receiverType) {
        this.receiverType = receiverType;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverIdentifier() {
        return receiverIdentifier;
    }

    public void setReceiverIdentifier(String receiverIdentifier) {
        this.receiverIdentifier = receiverIdentifier;
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    public String getAdmissionPurpose() {
        return admissionPurpose;
    }

    public void setAdmissionPurpose(String admissionPurpose) {
        this.admissionPurpose = admissionPurpose;
    }
}
