package com.example.museumcatalog.Models;

import java.time.LocalDateTime;

public class ReportRow {
    private String kp;
    private String name;
    private String description;
    private LocalDateTime date;
    private String document;
    private String stage;

    private String owner;

    public String getKp() { return kp; }
    public void setKp(String kp) { this.kp = kp; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public String getDocument() { return document; }
    public void setDocument(String document) { this.document = document; }

    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }

    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }
}