package com.dorca.ui.logins;

import com.dorca.ui.PatientRegistrationForm;

import javax.swing.*;
import java.sql.*;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class LoginForm extends javax.swing.JFrame {

    // Declare UI components
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;

    // Database connection details
    private final String url = "jdbc:sqlserver://localhost:1433;databaseName=HospitalDB;encrypt=true;trustServerCertificate=true;";
    private final String dbUsername = "hunter";
    private final String dbPassword = "hunter42";

    public LoginForm() {
        customInit();
    }

    private void customInit() {
        setTitle("Patient Login");
        setLayout(new java.awt.GridLayout(5, 1)); // Grid layout for form fields

        add(new JLabel("Email:"));
        emailField = new JTextField();
        add(emailField);

        add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        add(passwordField);

        loginButton = new JButton("Login");
        add(loginButton);

        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(e -> openRegistrationForm());
        add(registerButton);

// Exit button setup
        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> {
            int confirmExit = JOptionPane.showConfirmDialog(this, "Are you sure you want to exit?", "Exit", JOptionPane.YES_NO_OPTION);
            if (confirmExit == JOptionPane.YES_OPTION) {
                System.exit(0); // Exit the application
            }
        });
        add(exitButton);

        // Action listener for the login button
        loginButton.addActionListener(e -> {
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());

            // Call validateLogin() to check credentials
            if (validateLogin(email, password)) {
                JOptionPane.showMessageDialog(this, "Login Successful!");
                emailField.setText(""); // Clear email field
                passwordField.setText(""); // Clear password field
                showPatientDashboard();
                fetchPatientId(email);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid email or password. Please try again.");
            }
        });

        pack();
        setLocationRelativeTo(null); // Center the form on the screen
        setVisible(true);
    }

    // Method to validate the login credentials
    private boolean validateLogin(String email, String password) {
        try (Connection conn = DriverManager.getConnection(url, dbUsername, dbPassword)) {
            String sql = "SELECT * FROM Patients WHERE email = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, email); // Set the input email in the query
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String storedHashedPassword = rs.getString("password"); // Get the stored password
                    // Compare the entered password with the stored hashed password
                    if (storedHashedPassword.equals(hashPassword(password))) {
                        return true; // Login successful
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }
        return false; // Invalid login
    }

    // Method to hash the entered password
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashedBytes) {
                hexString.append(String.format("%02x", b)); // Convert bytes to hex format
            }
            return hexString.toString(); // Return the hashed password
        } catch (Exception e) {
            e.printStackTrace();
            return null; // Return null if hashing fails
        }
    }

    // Method to show the dashboard with the two buttons after login success
    private void showPatientDashboard() {
        JFrame dashboardFrame = new JFrame("Patient Dashboard");
        dashboardFrame.setLayout(new java.awt.GridLayout(6, 1)); // Adjust grid layout for additional buttons

        JButton bookAppointmentButton = new JButton("Book Appointment");
        bookAppointmentButton.addActionListener(e -> bookAppointment());
        dashboardFrame.add(bookAppointmentButton);

        JButton checkAppointmentsButton = new JButton("Check Appointments");
        checkAppointmentsButton.addActionListener(e -> showAppointments());

        JButton showCompletedAppointmentsButton = new JButton("View Completed Appointments");
        showCompletedAppointmentsButton.addActionListener(e -> showAppointmentsByStatus("Completed"));

        JButton showCancelledAppointmentsButton = new JButton("View Cancelled Appointments");
        showCancelledAppointmentsButton.addActionListener(e -> showAppointmentsByStatus("Cancelled"));

        JButton cancelAppointmentButton = new JButton("Cancel Appointment");
        cancelAppointmentButton.addActionListener(e -> cancelAppointment());

        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> {
            dashboardFrame.dispose();
            dispose();
        });

        dashboardFrame.add(checkAppointmentsButton);
        dashboardFrame.add(showCompletedAppointmentsButton);
        dashboardFrame.add(showCancelledAppointmentsButton);
        dashboardFrame.add(cancelAppointmentButton);
        dashboardFrame.add(exitButton);

        dashboardFrame.setSize(400, 300); // Adjust size for additional buttons
        dashboardFrame.setLocationRelativeTo(null);
        dashboardFrame.setVisible(true);
    }

    private boolean isDoctorAvailable(int doctorId, String date, String time) {
        try (Connection conn = DriverManager.getConnection(url, dbUsername, dbPassword)) {
            String sql = "SELECT * FROM Appointments WHERE doctor_id = ? AND appointment_date = ? AND appointment_time = ? AND status = 'Scheduled'";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, doctorId);
                stmt.setString(2, date);
                stmt.setString(3, time);

                ResultSet rs = stmt.executeQuery();
                // If a record exists, the doctor is not available
                return !rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }
        return false; // Assume doctor is unavailable if an error occurs
    }

    private void updateDoctorStatus(int doctorId) {
        try (Connection conn = DriverManager.getConnection(url, dbUsername, dbPassword)) {
            String sql = "UPDATE Doctors SET status = 'Busy' WHERE doctor_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, doctorId);
                int rowsUpdated = stmt.executeUpdate();
                if (rowsUpdated > 0) {
                    System.out.println("Doctor status updated to 'Busy'.");
                } else {
                    System.out.println("No doctor found with the given ID.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }
    }

    private void bookAppointment() {
        if (patientId == 0) {
            JOptionPane.showMessageDialog(this, "Patient ID not available. Please log in first.");
            return;
        }

        // Frame for booking form
        JFrame bookingFrame = new JFrame("Book Appointment");
        bookingFrame.setLayout(new java.awt.GridLayout(5, 2));

        // Doctor selection
        bookingFrame.add(new JLabel("Select Doctor:"));
        JComboBox<String> doctorComboBox = new JComboBox<>();
        List<Integer> doctorIds = new ArrayList<>(); // To store doctor IDs
        try (Connection conn = DriverManager.getConnection(url, dbUsername, dbPassword)) {
            String sql = "SELECT doctor_id, fname FROM Doctors";
            try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    doctorComboBox.addItem(rs.getString("fname"));
                    doctorIds.add(rs.getInt("doctor_id")); // Store doctor_id
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
            return;
        }
        bookingFrame.add(doctorComboBox);

        // Appointment date selection
        bookingFrame.add(new JLabel("Select Appointment Date:"));
        JComboBox<String> dateComboBox = new JComboBox<>();
        java.time.LocalDate startDate = java.time.LocalDate.of(2025, 1, 1);
        for (int i = 0; i < 15; i++) {
            dateComboBox.addItem(startDate.plusDays(i).toString());
        }
        bookingFrame.add(dateComboBox);

        // Appointment time selection
        bookingFrame.add(new JLabel("Select Appointment Time:"));
        JComboBox<String> timeComboBox = new JComboBox<>();
        for (int hour = 8; hour <= 15; hour++) {
            timeComboBox.addItem(String.format("%02d:00", hour));
            timeComboBox.addItem(String.format("%02d:30", hour));
        }
        bookingFrame.add(timeComboBox);

        // Book button
        JButton bookButton = new JButton("Book");
        bookButton.addActionListener(e -> {
            String selectedDoctor = (String) doctorComboBox.getSelectedItem();
            int selectedDoctorId = doctorIds.get(doctorComboBox.getSelectedIndex());
            String appointmentDate = (String) dateComboBox.getSelectedItem();
            String appointmentTime = (String) timeComboBox.getSelectedItem();

            if (appointmentDate == null || appointmentTime == null) {
                JOptionPane.showMessageDialog(bookingFrame, "Please select both date and time.");
                return;
            }

            if (isDoctorAvailable(selectedDoctorId, appointmentDate, appointmentTime)) {
                try (Connection conn = DriverManager.getConnection(url, dbUsername, dbPassword)) {
                    String sql = "INSERT INTO Appointments (patient_id, patient_first_name, doctor_id, doctor_first_name, appointment_date, appointment_time, status) VALUES (?, ?, ?, ?, ?, ?, 'Scheduled')";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setInt(1, patientId);
                        stmt.setString(2, fetchPatientName());
                        stmt.setInt(3, selectedDoctorId);
                        stmt.setString(4, selectedDoctor);
                        stmt.setString(5, appointmentDate);
                        stmt.setString(6, appointmentTime);

                        int rowsInserted = stmt.executeUpdate();
                        if (rowsInserted > 0) {
                            updateDoctorStatus(selectedDoctorId);
                            JOptionPane.showMessageDialog(bookingFrame, "Appointment scheduled successfully!");
                            bookingFrame.dispose();
                        } else {
                            JOptionPane.showMessageDialog(bookingFrame, "Failed to book the appointment.");
                        }
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(bookingFrame, "Database Error: " + ex.getMessage());
                }
            } else {
                JOptionPane.showMessageDialog(bookingFrame, "Selected doctor is not available at the chosen time. Please select a different slot.");
            }
        });
        bookingFrame.add(bookButton);

        // Cancel button
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> bookingFrame.dispose());
        bookingFrame.add(cancelButton);

        bookingFrame.pack();
        bookingFrame.setLocationRelativeTo(null);
        bookingFrame.setVisible(true);
    }

    private void showAppointmentsByStatus(String status) {
        if (patientId == 0) {
            JOptionPane.showMessageDialog(this, "Patient ID not available. Please log in first.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(url, dbUsername, dbPassword)) {
            String sql = "SELECT * FROM Appointments WHERE patient_id = ? AND status = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, patientId);
                stmt.setString(2, status);
                ResultSet rs = stmt.executeQuery();

                List<String> appointments = new ArrayList<>();
                while (rs.next()) {
                    String appointmentDetails = "Appointment ID: " + rs.getInt("appointment_id")
                            + ", Your Name: " + rs.getString("patient_first_name")
                            + ", Doctor: " + rs.getString("doctor_first_name")
                            + ", Date: " + rs.getTimestamp("appointment_date")
                            + ", Status: " + rs.getString("status");
                    appointments.add(appointmentDetails);
                }

                if (appointments.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No " + status.toLowerCase() + " appointments found.");
                } else {
                    JOptionPane.showMessageDialog(this, String.join("\n", appointments));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }
    }

    private void cancelAppointment() {
        if (patientId == 0) {
            JOptionPane.showMessageDialog(this, "Patient ID not available. Please log in first.");
            return;
        }

        String appointmentIdInput = JOptionPane.showInputDialog(this, "Enter the Appointment ID to cancel:");
        if (appointmentIdInput == null || appointmentIdInput.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Appointment ID is required.");
            return;
        }

        try {
            int appointmentId = Integer.parseInt(appointmentIdInput);

            try (Connection conn = DriverManager.getConnection(url, dbUsername, dbPassword)) {
                // Update the status of the appointment to "Cancelled"
                String sql = "UPDATE Appointments SET status = ? WHERE appointment_id = ? AND patient_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, "Cancelled");
                    stmt.setInt(2, appointmentId);
                    stmt.setInt(3, patientId);

                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(this, "Appointment cancelled successfully.");
                    } else {
                        JOptionPane.showMessageDialog(this, "No such appointment found or you are not authorized to cancel it.");
                    }
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid Appointment ID. Please enter a valid number.");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }
    }

    private void showCancelledAppointments() {
        if (patientId == 0) {
            JOptionPane.showMessageDialog(this, "Patient ID not available. Please log in first.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(url, dbUsername, dbPassword)) {
            String sql = "SELECT * FROM Appointments WHERE patient_id = ? AND status = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, patientId);
                stmt.setString(2, "Cancelled"); // Filter by status 'Cancelled'
                ResultSet rs = stmt.executeQuery();

                List<String> cancelledAppointments = new ArrayList<>();
                while (rs.next()) {
                    String appointmentDetails = "Appointment ID: " + rs.getInt("appointment_id")
                            + ", Patient Name: " + rs.getString("patient_first_name")
                            + ", Doctor: " + rs.getString("doctor_first_name")
                            + ", Date: " + rs.getTimestamp("appointment_date")
                            + ", Status: " + rs.getString("status");
                    cancelledAppointments.add(appointmentDetails);
                }

                if (cancelledAppointments.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No cancelled appointments found.");
                } else {
                    JOptionPane.showMessageDialog(this, String.join("\n", cancelledAppointments));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }
    }

    private void openRegistrationForm() {
        new PatientRegistrationForm(); // Open the registration form
        this.dispose(); // Close the login form
    }

    private String fetchPatientName() {
        try (Connection conn = DriverManager.getConnection(url, dbUsername, dbPassword)) {
            String sql = "SELECT firstName FROM Patients WHERE idNo = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, patientId); // Assuming patientId is fetched earlier
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getString("firstName");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }
        return "Unknown";
    }

    // Method to show appointments for the logged-in patient
    private int patientId; // Class-level variable to store the patient ID

    private void fetchPatientId(String email) {
        // Fetch the idNo from the Patients table using the email
        String idNo = null;

        try (Connection conn = DriverManager.getConnection(url, dbUsername, dbPassword)) {
            String sql = "SELECT idNo FROM Patients WHERE email = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, email);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    idNo = rs.getString("idNo");
                } else {
                    JOptionPane.showMessageDialog(this, "No patient found with the provided email.");
                    return;
                }
            }

            // Now, use idNo to find the corresponding patient_id in the Appointments table
            String sql2 = "SELECT patient_id FROM Appointments WHERE patient_id = ?";
            try (PreparedStatement stmt2 = conn.prepareStatement(sql2)) {
                stmt2.setString(1, idNo); // Assuming patient_id in Appointments is stored as a string
                ResultSet rs2 = stmt2.executeQuery();

                if (rs2.next()) {
                    patientId = rs2.getInt("patient_id");
                } else {
                    JOptionPane.showMessageDialog(this, "No appointments found for this patient.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }
    }

    private void showAppointments() {
        if (patientId == 0) {
            JOptionPane.showMessageDialog(this, "Patient ID not available. Please log in first.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(url, dbUsername, dbPassword)) {
            // SQL query to fetch appointments for a specific patient and status
            String sql = "SELECT * FROM Appointments WHERE patient_id = ? AND status = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                // Set the parameters for patient_id and status
                stmt.setInt(1, patientId); // Assuming patientId is provided in the context
                stmt.setString(2, "Scheduled"); // Focus only on "Scheduled" appointments

                ResultSet rs = stmt.executeQuery();

                // Collect the scheduled appointments
                List<String> appointments = new ArrayList<>();
                while (rs.next()) {
                    String appointmentDetails = "Appointment ID: " + rs.getInt("appointment_id")
                            + ", Your Name: " + rs.getString("patient_first_name")
                            + ", Doctor: " + rs.getString("doctor_first_name")
                            + ", Date: " + rs.getTimestamp("appointment_date")
                            + ", Status: " + rs.getString("status");
                    appointments.add(appointmentDetails);
                }

                // Display results to the user
                if (appointments.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No scheduled appointments found.");
                } else {
                    JOptionPane.showMessageDialog(this, String.join("\n", appointments));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }

    }

    // Main method to run the application
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new LoginForm().setVisible(true));
    }
}
