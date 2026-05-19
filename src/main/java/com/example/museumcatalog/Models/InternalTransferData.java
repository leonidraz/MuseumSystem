package com.example.museumcatalog.Models;

public class InternalTransferData {
    int fromEmployeeId;
    int toEmployeeId;
    String transferPurpose;

    public InternalTransferData(int fromEmployeeId, int toEmployeeId, String transferPurpose) {
        this.fromEmployeeId = fromEmployeeId;
        this.toEmployeeId = toEmployeeId;
        this.transferPurpose = transferPurpose;
    }

    public int getFromEmployeeId() {
        return fromEmployeeId;
    }

    public void setFromEmployeeId(int fromEmployeeId) {
        this.fromEmployeeId = fromEmployeeId;
    }

    public int getToEmployeeId() {
        return toEmployeeId;
    }

    public void setToEmployeeId(int toEmployeeId) {
        this.toEmployeeId = toEmployeeId;
    }

    public String getTransferPurpose() {
        return transferPurpose;
    }

    public void setTransferPurpose(String transferPurpose) {
        this.transferPurpose = transferPurpose;
    }
}
