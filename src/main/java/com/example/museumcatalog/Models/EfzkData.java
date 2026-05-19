package com.example.museumcatalog.Models;

import java.time.LocalDate;

public class EfzkData {
    LocalDate startDate;
    LocalDate endDate;
    String fundName;
    String collectionName;

    public EfzkData(LocalDate startDate, LocalDate endDate, String fundName, String collectionName) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.fundName = fundName;
        this.collectionName = collectionName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getFundName() {
        return fundName;
    }

    public void setFundName(String fundName) {
        this.fundName = fundName;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }
}
