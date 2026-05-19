package com.example.museumcatalog.Models;

import javafx.beans.property.*;

public class Employee {

    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty lastName = new SimpleStringProperty();
    private final StringProperty firstName = new SimpleStringProperty();
    private final StringProperty middleName = new SimpleStringProperty();
    private final StringProperty position = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final StringProperty phone = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();

    private final BooleanProperty selected = new SimpleBooleanProperty(false);

    public Employee() {}

    public Employee(int id, String lastName, String firstName, String middleName,
                    String position, String email, String phone, String status) {
        this.id.set(id);
        this.lastName.set(lastName);
        this.firstName.set(firstName);
        this.middleName.set(middleName);
        this.position.set(position);
        this.email.set(email);
        this.phone.set(phone);
        this.status.set(status);
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

    public String getPosition() { return position.get(); }
    public void setPosition(String value) { position.set(value); }
    public StringProperty positionProperty() { return position; }

    public String getEmail() { return email.get(); }
    public void setEmail(String value) { email.set(value); }
    public StringProperty emailProperty() { return email; }

    public String getPhone() { return phone.get(); }
    public void setPhone(String value) { phone.set(value); }
    public StringProperty phoneProperty() { return phone; }

    public String getStatus() { return status.get(); }
    public void setStatus(String value) { status.set(value); }
    public StringProperty statusProperty() { return status; }

    // Для отображения полного имени
    public String getFullFio() {
        String l = lastName.get(), f = firstName.get(), m = middleName.get();
        return ((l != null ? l : "") + " " + (f != null ? f : "") + " " + (m != null ? m : ""))
                .trim().replaceAll("\\s+", " ");
    }
}
