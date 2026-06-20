package com.example.museumcatalog.Models;

import javafx.beans.property.*;
import java.time.LocalDate;

public class Owner {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty lastName = new SimpleStringProperty();
    private final StringProperty firstName = new SimpleStringProperty();
    private final StringProperty middleName = new SimpleStringProperty();
    private final StringProperty passportSeries = new SimpleStringProperty();
    private final StringProperty passportNumber = new SimpleStringProperty();
    private final StringProperty issuedBy = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> dateOfIssue = new SimpleObjectProperty<>();
    private final StringProperty address = new SimpleStringProperty();
    private final StringProperty phone = new SimpleStringProperty();
    private final StringProperty notice = new SimpleStringProperty();
    private final BooleanProperty selected = new SimpleBooleanProperty(false);

    public Owner() {
    }

    public Owner(int id, String lastName, String firstName, String middleName,
                 String address, String passportSeries, String passportNumber,
                 String issuedBy, LocalDate dateOfIssue, String phone,
                 String notice) {
        setId(id);
        setLastName(lastName);
        setFirstName(firstName);
        setMiddleName(middleName);
        setAddress(address);
        setPassportSeries(passportSeries);
        setPassportNumber(passportNumber);
        setIssuedBy(issuedBy);
        setDateOfIssue(dateOfIssue);
        setPhone(phone);
        setNotice(notice);
    }

    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    public String getLastName() { return lastName.get(); }
    public void setLastName(String value) { lastName.set(value); }
    public StringProperty lastNameProperty() { return lastName; }

    public String getFirstName() { return firstName.get(); }
    public void setFirstName(String value) { firstName.set(value); }
    public StringProperty firstNameProperty() { return firstName; }

    public String getMiddleName() { return middleName.get(); }
    public void setMiddleName(String value) { middleName.set(value); }
    public StringProperty middleNameProperty() { return middleName; }

    public String getPassportSeries() { return passportSeries.get(); }
    public void setPassportSeries(String value) { passportSeries.set(value); }
    public StringProperty passportSeriesProperty() { return passportSeries; }

    public String getPassportNumber() { return passportNumber.get(); }
    public void setPassportNumber(String value) { passportNumber.set(value); }
    public StringProperty passportNumberProperty() { return passportNumber; }

    public String getIssuedBy() { return issuedBy.get(); }
    public void setIssuedBy(String value) { issuedBy.set(value); }
    public StringProperty issuedByProperty() { return issuedBy; }

    public LocalDate getDateOfIssue() { return dateOfIssue.get(); }
    public void setDateOfIssue(LocalDate value) { dateOfIssue.set(value); }
    public ObjectProperty<LocalDate> dateOfIssueProperty() { return dateOfIssue; }

    public String getAddress() { return address.get(); }
    public void setAddress(String value) { address.set(value); }
    public StringProperty addressProperty() { return address; }

    public String getPhone() { return phone.get(); }
    public void setPhone(String value) { phone.set(value); }
    public StringProperty phoneProperty() { return phone; }

    public String getNotice() { return notice.get(); }
    public void setNotice(String value) { notice.set(value); }
    public StringProperty noticeProperty() { return notice; }

    public boolean isSelected() { return selected.get(); }
    public void setSelected(boolean value) { selected.set(value); }
    public BooleanProperty selectedProperty() { return selected; }

    public String getFullFio() {
        String l = lastName.get(), f = firstName.get(), m = middleName.get();
        return ((l != null ? l : "") + " " + (f != null ? f : "") + " " + (m != null ? m : ""))
                .trim().replaceAll("\\s+", " ");
    }

    public String getShortFio() {
        String l = lastName.get(), f = firstName.get(), m = middleName.get();
        StringBuilder res = new StringBuilder(l != null ? l : "");
        if (f != null && !f.isEmpty()) res.append(" ").append(f.charAt(0)).append(".");
        if (m != null && !m.isEmpty()) res.append(" ").append(m.charAt(0)).append(".");
        return res.toString();
    }

    public String getPassport() {
        String s = passportSeries.get();
        String n = passportNumber.get();
        return (s != null ? s : "") + " " + (n != null ? n : "");
    }
}