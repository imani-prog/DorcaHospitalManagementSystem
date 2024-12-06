package com.dorca.ui;

import com.dorca.ui.logins.LoginForm;
import com.dorca.beans.PatientBean;
import com.dorca.ui.logins.LoginForm;
import javax.swing.*;
import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public class PatientRegistrationForm extends javax.swing.JFrame {

    // Declare custom components
    private JTextField fnameField, lnameField, addressField, contactField, idField, emailField;
    private JPasswordField passwordField, confirmPasswordField; // Password field
    private JSpinner dobSpinner; // Spinner for DOB
    private JButton submitButton;
    private JButton loginButton; // Login button

    // Database connection details
    private final String url = "jdbc:sqlserver://localhost:1433;databaseName=HospitalDB;encrypt=true;trustServerCertificate=true;";
    private final String username = "hunter";
    private final String password = "hunter42";

    /**
     * Constructor
     */
    public PatientRegistrationForm() {
        initComponents(); // NetBeans auto-generated method
        customInit();     // Add custom components and logic
    }

    /**
     * Custom initialization method for manual UI setup
     */
    private void customInit() {
        // Set additional frame properties
        setTitle("Patient Registration");
        setLayout(new java.awt.GridLayout(11, 2)); // Increased rows for the password field

        // Add components
        add(new JLabel("First Name:"));
        fnameField = new JTextField();
        add(fnameField);

        add(new JLabel("Last Name:"));
        lnameField = new JTextField();
        add(lnameField);

        add(new JLabel("Address:"));
        addressField = new JTextField();
        add(addressField);

        add(new JLabel("ID No:"));
        idField = new JTextField();
        add(idField);

        add(new JLabel("Contact:"));
        contactField = new JTextField();
        add(contactField);

        add(new JLabel("Email:"));
        emailField = new JTextField();  // Added email field for better record keeping
        add(emailField);

        // Add password field
        add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        add(passwordField);

        // Add confirm password field
        add(new JLabel("Confirm Password:"));
        confirmPasswordField = new JPasswordField();
        add(confirmPasswordField);

        // Add Date of Birth field
        add(new JLabel("Date of Birth:"));
        dobSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dobSpinner, "yyyy-MM-dd");
        dobSpinner.setEditor(dateEditor);
        add(dobSpinner);

        submitButton = new JButton("Register");
        add(submitButton);

        // Add Login Button to go back to login page
        loginButton = new JButton("Back to Login");
        loginButton.addActionListener(e -> openLoginForm());
        add(loginButton);

        // Action listener for submit button
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

            // Handle form submission if validation passes
            handleSubmit();
        });

        pack(); // Adjust window size to fit content
        setLocationRelativeTo(null); // Center the window on screen
        setVisible(true); // Make the frame visible
    }

    /**
     * Method to handle patient registration
     */
    private void handleSubmit() {
        // Create a PatientBean object and populate it with user input
        PatientBean patient = new PatientBean();
        patient.setFirstName(fnameField.getText());
        patient.setLastName(lnameField.getText());
        patient.setAddress(addressField.getText());
        patient.setContact(contactField.getText());
        patient.setIdNo(idField.getText());
        patient.setEmail(emailField.getText());

        // Hash the password and set it in the PatientBean
        String hashedPassword = hashPassword(new String(passwordField.getPassword()));
        patient.setPassword(hashedPassword);

        // Get the selected DOB and set it in the PatientBean
        patient.setDob((Date) dobSpinner.getValue());

        // Insert patient data into the database
        insertPatientData(patient);

        // Show confirmation dialog with options
        int confirmLogin = JOptionPane.showOptionDialog(
            this,
            "Patient Registered Successfully! Would you like to proceed to login?",
            "Registration Successful",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            null, // No custom icon
            new Object[] {"Proceed to Login", "Stay on Registration"}, // Options
            "Proceed to Login" // Default option
        );

        // If user selects "Proceed to Login", open the login form
        if (confirmLogin == JOptionPane.YES_OPTION) {
            openLoginForm();
        } else {
            // Optionally clear the form and let the user stay on registration page
            clearForm();
        }
    }

    /**
     * Method to hash the password using SHA-256
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashedBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();  // Return the hashed password in hexadecimal format
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;  // Return null if hashing fails
        }
    }

    /**
     * Method to insert patient data into the database
     */
    private void insertPatientData(PatientBean patient) {
        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            String sql = "INSERT INTO Patients (idNo, firstName, lastName, address, contact, email, dob, password) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                // Set parameters
                stmt.setString(1, patient.getIdNo());  // Set idNo as primary key
                stmt.setString(2, patient.getFirstName());  // First name
                stmt.setString(3, patient.getLastName());   // Last name
                stmt.setString(4, patient.getAddress());    // Address
                stmt.setString(5, patient.getContact());    // Contact
                stmt.setString(6, patient.getEmail());      // Email
                stmt.setDate(7, new java.sql.Date(patient.getDob().getTime()));  // Convert Date to SQL Date
                stmt.setString(8, patient.getPassword());  // Set the hashed password

                // Execute the insert statement
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }
    }

    /**
     * Method to clear the form after submission
     */
    private void clearForm() {
        fnameField.setText("");  // Clear first name field
        lnameField.setText("");  // Clear last name field
        addressField.setText("");  // Clear address field
        contactField.setText("");  // Clear contact field
        idField.setText("");  // Clear idNo field
        emailField.setText("");  
        passwordField.setText("");  
        confirmPasswordField.setText("");  // Clear confirm password field
        dobSpinner.setValue(new Date()); 
    }

    /**
     * Method to open the login form
     */
    private void openLoginForm() {
        new LoginForm(); // Open the login form
        this.dispose();  // Close the registration form
    }

    /**
     * NetBeans auto-generated method for GUI setup
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }

    // Main method to run the application
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            new PatientRegistrationForm().setVisible(true);
        });
    }
}
