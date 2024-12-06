package com.dorca.beans;

public class DoctorBean {

    private int doctorId;
    private String firstName;
    private String lastName;
    private String specialty;
    private String contact;
    private String email;
    private String status;  // status field
    private String password;  // Added password field
    private String doctorAvailability;  // Added field for doctor availability

    // Getters and setters
    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

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

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
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

    public String getStatus() {
        return status;  // Properly return status value
    }

    public void setStatus(String status) {
        this.status = status;  // Set status value
    }

    public String getPassword() {
        return password;  // Get password value
    }

    public void setPassword(String password) {
        this.password = password;  // Set password value
    }

    // Getter and Setter for doctor availability
    public String getDoctorAvailability() {
        return doctorAvailability;  // Return doctor availability status
    }

    public void setDoctorAvailability(String doctorAvailability) {
        this.doctorAvailability = doctorAvailability;  // Set doctor availability status
    }

    // Optionally, a method to hash the password
    public String hashPassword(String password) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashedBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();  // Return the hashed password
        } catch (java.security.NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;  // Return null if hashing fails
        }
    }

    // Override toString to return full name
    @Override
    public String toString() {
        return firstName + " " + lastName;  // Concatenate first and last names
    }
}
