package com.dorca.ui.logins;

import com.dorca.ui.DoctorRegistrationForm;
import javax.swing.*;
import java.sql.*;
import java.security.MessageDigest;

public class DoctorLoginForm extends javax.swing.JFrame {

    // Declare UI components
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;

    // Database connection details
    private final String url = "jdbc:sqlserver://localhost:1433;databaseName=HospitalDB;encrypt=true;trustServerCertificate=true;";
    private final String dbUsername = "hunter";
    private final String dbPassword = "hunter42";

    public DoctorLoginForm() {
        customInit();
    }

    private void customInit() {
        setTitle("Doctor Login");
        setLayout(new java.awt.GridLayout(4, 1)); // Grid layout for form fields

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

        // Action listener for the login button
        loginButton.addActionListener(e -> {
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());

            // Call validateLogin() to check credentials
            if (validateDoctorLogin(email, password)) {
                JOptionPane.showMessageDialog(this, "Login Successful!");
                emailField.setText(""); // Clear email field
                passwordField.setText(""); // Clear password field
                showDoctorDashboard();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid email or password. Please try again.");
            }
        });

        pack();
        setLocationRelativeTo(null); // Center the form on the screen
        setVisible(true);
    }

    // Method to validate the login credentials for a doctor
    private boolean validateDoctorLogin(String email, String password) {
        try (Connection conn = DriverManager.getConnection(url, dbUsername, dbPassword)) {
            String sql = "SELECT * FROM Doctors WHERE email = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, email); // Set the input email in the query
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String storedHashedPassword = rs.getString("password"); // Get the stored password
                    doctorId = rs.getInt("doctor_id"); // Fetch doctor's ID
                    doctorName = rs.getString("fname"); // Fetch doctor's name

                    // Compare the entered password with the stored hashed password
                    return storedHashedPassword.equals(hashPassword(password));
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

    // Method to show the dashboard with options for the doctor
    // Method to show the dashboard with options for the doctor
    private void showDoctorDashboard() {
        JFrame dashboardFrame = new JFrame("Doctor Dashboard");
        dashboardFrame.setLayout(new java.awt.GridLayout(4, 2)); // Increase grid layout rows for the new buttons

        JButton viewAppointmentsButton = new JButton("View Scheduled Appointments");
        viewAppointmentsButton.addActionListener(e -> viewScheduledAppointments());
        dashboardFrame.add(viewAppointmentsButton);

        JButton markCompletedButton = new JButton("Mark Appointment as Completed");
        markCompletedButton.addActionListener(e -> markAppointmentCompleted());
        dashboardFrame.add(markCompletedButton);

        JButton showCompletedButton = new JButton("Show Completed Appointments");
        showCompletedButton.addActionListener(e -> showCompletedAppointments());
        dashboardFrame.add(showCompletedButton);

        JButton cancelAppointmentButton = new JButton("Cancel Appointment");
        cancelAppointmentButton.addActionListener(e -> cancelAppointment());
        dashboardFrame.add(cancelAppointmentButton);

        JButton showCancelledButton = new JButton("Show Cancelled Appointments");
        showCancelledButton.addActionListener(e -> showCancelledAppointments());
        dashboardFrame.add(showCancelledButton);

        JButton updateTimeSlotButton = new JButton("Update Time Slot Availability");
        updateTimeSlotButton.addActionListener(e -> openUpdateTimeSlotForm());
        dashboardFrame.add(updateTimeSlotButton);

        JButton addAvailabilityButton = new JButton("Add Availability");
        addAvailabilityButton.addActionListener(e -> addAvailability());
        dashboardFrame.add(addAvailabilityButton);

        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> {
            dashboardFrame.dispose();
            dispose();
        });
        dashboardFrame.add(exitButton);

        dashboardFrame.setSize(400, 350);
        dashboardFrame.setLocationRelativeTo(null);
        dashboardFrame.setVisible(true);
    }

    // Method to show completed appointments
    private void showCompletedAppointments() {
        try (Connection conn = DriverManager.getConnection(url, dbUsername, dbPassword)) {
            String sql = "SELECT * FROM Appointments WHERE doctor_id = ? AND status = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, doctorId);
                stmt.setString(2, "Completed");

                ResultSet rs = stmt.executeQuery();
                StringBuilder appointments = new StringBuilder("Completed Appointments:\n");
                while (rs.next()) {
                    appointments.append("Appointment ID: ").append(rs.getInt("appointment_id"))
                            .append(", Patient: ").append(rs.getString("patient_first_name"))
                            .append(", Date: ").append(rs.getString("appointment_date"))
                            .append(", Time: ").append(rs.getString("appointment_time"))
                            .append("\n");
                }

                JOptionPane.showMessageDialog(this, appointments.toString());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }
    }

// Method to show cancelled appointments
    private void showCancelledAppointments() {
        try (Connection conn = DriverManager.getConnection(url, dbUsername, dbPassword)) {
            String sql = "SELECT * FROM Appointments WHERE doctor_id = ? AND status = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, doctorId);
                stmt.setString(2, "Cancelled");

                ResultSet rs = stmt.executeQuery();
                StringBuilder appointments = new StringBuilder("Cancelled Appointments:\n");
                while (rs.next()) {
                    appointments.append("Appointment ID: ").append(rs.getInt("appointment_id"))
                            .append(", Patient: ").append(rs.getString("patient_first_name"))
                            .append(", Date: ").append(rs.getString("appointment_date"))
                            .append(", Time: ").append(rs.getString("appointment_time"))
                            .append("\n");
                }

                JOptionPane.showMessageDialog(this, appointments.toString());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }
    }

// Method to open the form for updating time slot availability
    // Method to open the form for updating time slot availability
    private void openUpdateTimeSlotForm() {
        String[] allTimeSlots = {
            "08:00", "08:30", "09:00", "09:30", "10:00", "10:30", "11:00", "11:30", "12:00",
            "12:30", "01:00", "01:30", "02:00", "02:30", "03:00", "03:30", "04:00"
        };

        // Display the doctor's first name in the dialog
        String selectedTimeSlot = (String) JOptionPane.showInputDialog(
                this,
                "Dr. " + doctorName + ", select the time slot to update:", // Personalize with doctor’s name
                "Update Time Slot Availability",
                JOptionPane.QUESTION_MESSAGE,
                null,
                allTimeSlots,
                allTimeSlots[0] // Default selection
        );

        if (selectedTimeSlot != null) {
            String availability = JOptionPane.showInputDialog(this,
                    "Enter availability for " + selectedTimeSlot + " (Available/Unavailable):");

            if (availability != null && (availability.equalsIgnoreCase("Available") || availability.equalsIgnoreCase("Unavailable"))) {
                updateDoctorTimeSlotAvailability(doctorId, selectedTimeSlot, availability.equalsIgnoreCase("Available"));
            } else {
                JOptionPane.showMessageDialog(this, "Invalid availability status. Please enter 'Available' or 'Unavailable'.");
            }
        }
    }

// Method to update the availability of a specific time slot for the doctor
    // Method to update the availability of a specific time slot for the doctor
    // Method to update the availability of a specific time slot for the doctor
 private void updateDoctorTimeSlotAvailability(int doctorId, String timeSlot, boolean isAvailable) {
    // Check if the time slot record exists for the doctor
    String checkQuery = "SELECT availability FROM DoctorTimeSlots WHERE doctor_id = ? AND time_slot = ?";
    try (Connection conn = DriverManager.getConnection(url, dbUsername, dbPassword); 
         PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {

        checkStmt.setInt(1, doctorId);
        checkStmt.setString(2, timeSlot);
        ResultSet rs = checkStmt.executeQuery();

        // Step 1: Fetch the doctor's first name
        String doctorFirstName = null;
        String fetchFirstNameQuery = "SELECT fname FROM Doctors WHERE doctor_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(fetchFirstNameQuery)) {
            stmt.setInt(1, doctorId); // Set doctorId in the query
            ResultSet rsFirstName = stmt.executeQuery();

            if (rsFirstName.next()) {
                doctorFirstName = rsFirstName.getString("fname"); // Retrieve the first name
            } else {
                JOptionPane.showMessageDialog(this, "Doctor not found with this ID.");
                return; // Exit if no result is found
            }
        }

        if (rs.next()) {
            String currentAvailability = rs.getString("availability");
            JOptionPane.showMessageDialog(this, "Current availability for " + timeSlot + ": " + currentAvailability);
            
            // Step 2: Update the availability and fname for existing time slot
            String updateQuery = "UPDATE DoctorTimeSlots SET availability = ?, fname = ? WHERE doctor_id = ? AND time_slot = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                stmt.setString(1, isAvailable ? "Available" : "Unavailable");
                stmt.setString(2, doctorFirstName); // Set the first name retrieved from Doctors table
                stmt.setInt(3, doctorId);
                stmt.setString(4, timeSlot);

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Time slot " + timeSlot + " updated to " + (isAvailable ? "Available" : "Unavailable") + " for " + doctorFirstName + ".");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update time slot.");
                }
            }
        } else {
            // Step 3: If no record found, insert the time slot with availability and fname
            String insertQuery = "INSERT INTO DoctorTimeSlots (doctor_id, time_slot, availability, fname) VALUES (?, ?, ?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                insertStmt.setInt(1, doctorId);
                insertStmt.setString(2, timeSlot);
                insertStmt.setString(3, isAvailable ? "Available" : "Unavailable");
                insertStmt.setString(4, doctorFirstName); // Insert the doctor's first name

                int rowsAffected = insertStmt.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Time slot " + timeSlot + " added as " + (isAvailable ? "Available" : "Unavailable") + ".");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add new time slot.");
                }
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
    }
}



    // Method to view scheduled appointments
    private void viewScheduledAppointments() {
        try (Connection conn = DriverManager.getConnection(url, dbUsername, dbPassword)) {
            String sql = "SELECT * FROM Appointments WHERE doctor_id = ? AND status = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, doctorId);
                stmt.setString(2, "Scheduled");

                ResultSet rs = stmt.executeQuery();
                StringBuilder appointments = new StringBuilder("Scheduled Appointments:\n");
                while (rs.next()) {
                    appointments.append("Appointment ID: ").append(rs.getInt("appointment_id"))
                            .append(", Patient: ").append(rs.getString("patient_first_name"))
                            .append(", Date: ").append(rs.getString("appointment_date"))
                            .append(", Time: ").append(rs.getString("appointment_time"))
                            .append("\n");
                }

                JOptionPane.showMessageDialog(this, appointments.toString());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }
    }

    // Method to mark an appointment as completed
    private void markAppointmentCompleted() {
        String appointmentIdInput = JOptionPane.showInputDialog(this, "Enter the Appointment ID to mark as completed:");
        if (appointmentIdInput == null || appointmentIdInput.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Appointment ID is required.");
            return;
        }

        try {
            int appointmentId = Integer.parseInt(appointmentIdInput);

            try (Connection conn = DriverManager.getConnection(url, dbUsername, dbPassword)) {
                String sql = "UPDATE Appointments SET status = ? WHERE appointment_id = ? AND doctor_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, "Completed");
                    stmt.setInt(2, appointmentId);
                    stmt.setInt(3, doctorId);

                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(this, "Appointment marked as completed.");
                    } else {
                        JOptionPane.showMessageDialog(this, "No such appointment found or you are not authorized to update it.");
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

    // Method to cancel an appointment
    private void cancelAppointment() {
        String appointmentIdInput = JOptionPane.showInputDialog(this, "Enter the Appointment ID to cancel:");
        if (appointmentIdInput == null || appointmentIdInput.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Appointment ID is required.");
            return;
        }

        try {
            int appointmentId = Integer.parseInt(appointmentIdInput);

            try (Connection conn = DriverManager.getConnection(url, dbUsername, dbPassword)) {
                String sql = "UPDATE Appointments SET status = ? WHERE appointment_id = ? AND doctor_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, "Cancelled");
                    stmt.setInt(2, appointmentId);
                    stmt.setInt(3, doctorId);

                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(this, "Appointment cancelled.");
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

    // Method to add availability
    private void addAvailability() {
        // Implementation for adding doctor availability
        String availability = JOptionPane.showInputDialog(this, "Enter your availability (Available/Unavailable):");
        if (availability != null && !availability.trim().isEmpty()) {
            try (Connection conn = DriverManager.getConnection(url, dbUsername, dbPassword)) {
                String sql = "UPDATE Doctors SET doctor_availability = ? WHERE doctor_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, availability);
                    stmt.setInt(2, doctorId);

                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(this, "Availability updated to " + availability + ".");
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to update availability.");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
            }
        }
    }

    private void openRegistrationForm() {
        new DoctorRegistrationForm().setVisible(true); // Navigate to Registration Form
        this.dispose(); // Close the login form
    }

    // Main method to run the application
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new DoctorLoginForm().setVisible(true));
    }

    // Class-level variables to store doctor's details
    private int doctorId; // Store the logged-in doctor's ID
    private String doctorName; // Store the logged-in doctor's name
}
