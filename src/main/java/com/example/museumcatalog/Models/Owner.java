package com.example.museumcatalog.Models;

import javafx.beans.property.*;

import java.time.LocalDate;

public class Owner {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty lastName = new SimpleStringProperty();
    private final StringProperty firstName = new SimpleStringProperty();
    private final StringProperty middleName = new SimpleStringProperty();
    private final StringProperty address = new SimpleStringProperty();
    private final StringProperty passportSeries = new SimpleStringProperty();
    private final StringProperty passportNumber = new SimpleStringProperty();
    private final StringProperty issuedBy = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> dateOfIssue = new SimpleObjectProperty<>();
    private final StringProperty phone = new SimpleStringProperty();
    private final StringProperty notice = new SimpleStringProperty();
    private final BooleanProperty isActive = new SimpleBooleanProperty(true);

    // Конструктор по умолчанию
    public Owner() {
    }

    // Полный конструктор
    public Owner(int id, String lastName, String firstName, String middleName,
                 String address, String passportSeries, String passportNumber,
                 String issuedBy, LocalDate dateOfIssue, String phone,
                 String notice, boolean isActive) {
        this.id.set(id);
        this.lastName.set(lastName);
        this.firstName.set(firstName);
        this.middleName.set(middleName);
        this.address.set(address);
        this.passportSeries.set(passportSeries);
        this.passportNumber.set(passportNumber);
        this.issuedBy.set(issuedBy);
        this.dateOfIssue.set(dateOfIssue);
        this.phone.set(phone);
        this.notice.set(notice);
        this.isActive.set(isActive);
    }

    // Геттеры для значений
    public int getId() {
        return id.get();
    }

    public String getLastName() {
        return lastName.get();
    }

    public String getFirstName() {
        return firstName.get();
    }

    public String getMiddleName() {
        return middleName.get();
    }

    public String getAddress() {
        return address.get();
    }

    public String getPassportSeries() {
        return passportSeries.get();
    }

    public String getPassportNumber() {
        return passportNumber.get();
    }

    public String getIssuedBy() {
        return issuedBy.get();
    }

    public LocalDate getDateOfIssue() {
        return dateOfIssue.get();
    }

    public String getPhone() {
        return phone.get();
    }

    public String getNotice() {
        return notice.get();
    }

    public boolean isActive() {
        return isActive.get();
    }

    // Сеттеры для значений
    public void setId(int id) {
        this.id.set(id);
    }

    public void setLastName(String lastName) {
        this.lastName.set(lastName);
    }

    public void setFirstName(String firstName) {
        this.firstName.set(firstName);
    }

    public void setMiddleName(String middleName) {
        this.middleName.set(middleName);
    }

    public void setAddress(String address) {
        this.address.set(address);
    }

    public void setPassportSeries(String passportSeries) {
        this.passportSeries.set(passportSeries);
    }

    public void setPassportNumber(String passportNumber) {
        this.passportNumber.set(passportNumber);
    }

    public void setIssuedBy(String issuedBy) {
        this.issuedBy.set(issuedBy);
    }

    public void setDateOfIssue(LocalDate dateOfIssue) {
        this.dateOfIssue.set(dateOfIssue);
    }

    public void setPhone(String phone) {
        this.phone.set(phone);
    }

    public void setNotice(String notice) {
        this.notice.set(notice);
    }

    public void setActive(boolean active) {
        isActive.set(active);
    }

    // Property-геттеры (обязательны для привязки в TableView)
    public IntegerProperty idProperty() {
        return id;
    }

    public StringProperty lastNameProperty() {
        return lastName;
    }

    public StringProperty firstNameProperty() {
        return firstName;
    }

    public StringProperty middleNameProperty() {
        return middleName;
    }

    public StringProperty addressProperty() {
        return address;
    }

    public StringProperty passportSeriesProperty() {
        return passportSeries;
    }

    public StringProperty passportNumberProperty() {
        return passportNumber;
    }

    public StringProperty issuedByProperty() {
        return issuedBy;
    }

    public ObjectProperty<LocalDate> dateOfIssueProperty() {
        return dateOfIssue;
    }

    public StringProperty phoneProperty() {
        return phone;
    }

    public StringProperty noticeProperty() {
        return notice;
    }

    public BooleanProperty activeProperty() {
        return isActive;
    }

    public String getFullFio() {
        StringBuilder fio = new StringBuilder(lastName.get());
        if (firstName.get() != null && !firstName.get().isEmpty()) {
            fio.append(" ").append(firstName.get());
        }
        if (middleName.get() != null && !middleName.get().isEmpty()) {
            fio.append(" ").append(middleName.get());
        }
        return fio.toString();
    }

    public String getShortFio() {
        StringBuilder fio = new StringBuilder(lastName.get());
        if (firstName.get() != null && !firstName.get().isEmpty()) {
            fio.append(" ").append(firstName.get().charAt(0)).append(".");
        }
        if (middleName.get() != null && !middleName.get().isEmpty()) {
            fio.append(middleName.get().charAt(0)).append(".");
        }
        return fio.toString();
    }

    // Форматированный паспорт для отображения
    public String getPassport() {
        return passportSeries.get() + " " + passportNumber.get();
    }

    @Override
    public String toString() {
        return getShortFio() + " (" + getPassport() + ")";
    }
}