package com.dorca.beans;

import java.util.Date;

public class PatientBean {

    private String idNo;  // idNo as primary key (String)
    private String firstName;
    private String lastName;
    private String address;
    private String contact;
    private String email;
    private Date dob;  // Store DOB as a Date object
    private String password;  // New password field

    // Getters and setters for idNo (as primary key)
    public String getIdNo() {
        return idNo;
    }

    public void setIdNo(String idNo) {
        this.idNo = idNo;
    }

    // Getters and setters for other fields
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getDob() {
        return dob;
    }

    // Setter for DOB (Date type)
    public void setDob(Date dob) {
        this.dob = dob;
    }

    // Getter and setter for password
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Constructor to initialize the PatientBean object
    public PatientBean(String idNo, String firstName, String lastName, String address, String contact, String email, Date dob, String password) {
        this.idNo = idNo;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.contact = contact;
        this.email = email;
        this.dob = dob;
        this.password = password;  // Initialize password
    }

    // Default constructor
    public PatientBean() {
    }

    // Override toString method for display in JComboBox or other components
    @Override
    public String toString() {
        return firstName + " " + lastName; // Display the full name of the patient
    }
}
