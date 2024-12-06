package com.dorca.ui;

import com.dorca.ui.logins.DoctorLoginForm;
import com.dorca.beans.DoctorBean;

import javax.swing.*;
import java.sql.*;

public class DoctorRegistrationForm extends javax.swing.JFrame {

    // Declare custom components
    private JTextField fnameField, lnameField, contactField, emailField;
    private JComboBox<String> specialtyComboBox;
    private JPasswordField passwordField, confirmPasswordField; // Password field
    private JButton loginButton;
    private JButton submitButton;

    // Database connection details
    private final String url = "jdbc:sqlserver://localhost:1433;databaseName=HospitalDB;encrypt=true;trustServerCertificate=true;";
    private final String username = "hunter";
    private final String password = "hunter42";

    /**
     * Constructor
     */
    public DoctorRegistrationForm() {
        initComponents(); // NetBeans auto-generated method
        customInit(); // Add custom components and logic
    }

    private void customInit() {
        // Set additional frame properties
        setTitle("Doctor Registration");
        setLayout(new java.awt.GridLayout(9, 2)); // Adjusted layout for 9 rows (with confirm password)

        // Add components
        add(new JLabel("First Name:"));
        fnameField = new JTextField();
        add(fnameField);

        add(new JLabel("Last Name:"));
        lnameField = new JTextField();
        add(lnameField);

        add(new JLabel("Specialty:"));
        specialtyComboBox = new JComboBox<>(new String[]{
            "General Medicine", "Pediatrics", "Physician",
            "Dermatologist", "Surgery", "Orthopedics",
            "Cardiology", "Neurologist"
        });
        add(specialtyComboBox);

        add(new JLabel("Contact:"));
        contactField = new JTextField();
        add(contactField);

        add(new JLabel("Email:"));
        emailField = new JTextField();
        add(emailField);

        add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        add(passwordField);

        add(new JLabel("Confirm Password:"));
        confirmPasswordField = new JPasswordField();
        add(confirmPasswordField);

        // Add Submit Button
        submitButton = new JButton("Register");
        add(submitButton);

        submitButton.addActionListener(e -> {
            // Validate the form
            if (fnameField.getText().trim().isEmpty() || lnameField.getText().trim().isEmpty() || contactField.getText().trim().isEmpty()
                    || emailField.getText().trim().isEmpty() || passwordField.getPassword().length == 0 || confirmPasswordField.getPassword().length == 0) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return; // Stop further processing if fields are empty
            }

            // Check if the passwords match
            if (!String.valueOf(passwordField.getPassword()).equals(String.valueOf(confirmPasswordField.getPassword()))) {
                JOptionPane.showMessageDialog(this, "Passwords do not match.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return; // Stop further processing if passwords do not match
            }

            // Create a DoctorBean object and populate it with user input
            DoctorBean doctor = new DoctorBean();
            doctor.setFirstName(fnameField.getText());
            doctor.setLastName(lnameField.getText());
            doctor.setSpecialty((String) specialtyComboBox.getSelectedItem());
            doctor.setContact(contactField.getText());
            doctor.setEmail(emailField.getText());
            doctor.setStatus("Unbooked");  // Set status to "Unbooked" programmatically
            doctor.setPassword(hashPassword(new String(passwordField.getPassword()))); // Hash the password before setting it

            // Insert doctor data into the database
            insertDoctorData(doctor);

            // Show confirmation dialog
            JOptionPane.showMessageDialog(this, "Doctor Registered Successfully!");

            // Clear the form after submission
            clearForm();
        });

        // Add Login Button
        loginButton = new JButton("Login");
        loginButton.addActionListener(e -> {
            // Navigate back to the login form
            new DoctorLoginForm().setVisible(true);
            this.dispose(); // Close the registration form
        });
        add(loginButton);

        pack(); // Adjust window size to fit content
        setLocationRelativeTo(null); // Center the window on screen
        setVisible(true); // Make the frame visible
    }

    /**
     * Method to insert doctor data into the database
     */
    private void insertDoctorData(DoctorBean doctor) {
        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            String sql = "INSERT INTO Doctors (fname, lname, specialty, contact, email, password, status) VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                // Set parameters
                stmt.setString(1, doctor.getFirstName());
                stmt.setString(2, doctor.getLastName());
                stmt.setString(3, doctor.getSpecialty());
                stmt.setString(4, doctor.getContact());
                stmt.setString(5, doctor.getEmail());
                stmt.setString(6, doctor.getPassword()); // Insert the hashed password
                stmt.setString(7, doctor.getStatus()); // Insert "Unbooked" status

                // Execute the insert statement
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }
    }

    /**
     * Hashes the password using SHA-256
     */
    private String hashPassword(String password) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashedBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString(); // Return the hashed password
        } catch (java.security.NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null; // Return null if hashing fails
        }
    }

    /**
     * Clears the form fields after submission
     */
    private void clearForm() {
        fnameField.setText("");
        lnameField.setText("");
        specialtyComboBox.setSelectedIndex(0);
        contactField.setText("");
        emailField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
// Clear the password field
        // statusComboBox.setSelectedIndex(0);  // No need to reset status as it's hidden
    }

    /**
     * NetBeans auto-generated method for GUI setup WARNING: Do NOT modify this
     * method manually!
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 400, Short.MAX_VALUE));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 300, Short.MAX_VALUE));

        pack();
    }

    /**
     * Main method to run the application
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new DoctorRegistrationForm().setVisible(true));
    }
}
