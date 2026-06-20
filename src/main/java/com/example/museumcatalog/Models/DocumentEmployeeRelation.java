package com.example.museumcatalog.Models;

import com.example.museumcatalog.DocumentEmployeeRole;

public class DocumentEmployeeRelation {

    private Employee employee;
    private DocumentEmployeeRole role;

    public DocumentEmployeeRelation(Employee employee, DocumentEmployeeRole role) {
        this.employee = employee;
        this.role = role;
    }

    public Employee getEmployee() {
        return employee;
    }

    public DocumentEmployeeRole getRole() {
        return role;
    }
}