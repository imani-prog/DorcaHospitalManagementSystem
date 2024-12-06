package com.dorca.ui;

import com.dorca.beans.AppointmentBean;
import com.dorca.beans.PatientBean;
import com.dorca.beans.DoctorBean;
import javax.swing.*;
import java.sql.*;

public class AppointmentRegistrationForm extends javax.swing.JFrame {

    // Declare custom components
    private JComboBox<DoctorBean> doctorComboBox;
    private JComboBox<PatientBean> patientComboBox;
    private JComboBox<String> dateComboBox, timeComboBox, statusComboBox;
    private JButton refreshButton;
    private JButton submitButton;

    // Database connection details
    private final String url = "jdbc:sqlserver://localhost:1433;databaseName=HospitalDB;encrypt=true;trustServerCertificate=true;";
    private final String username = "hunter";
    private final String password = "hunter42";

    /**
     * Constructor
     */
    public AppointmentRegistrationForm() {
        initComponents(); // NetBeans auto-generated method
        customInit(); // Add custom components and logic
    }

    /**
     * Custom initialization method for manual UI setup
     */
    private void customInit() {
        // Set additional frame properties
        setTitle("Appointment Registration");
        setLayout(new java.awt.GridLayout(6, 2)); // GridLayout: 6 rows, 2 columns

        // Add components
        add(new JLabel("Patient:"));
        patientComboBox = new JComboBox<>();
        loadPatients(); // Load patients from the database
        add(patientComboBox);

        add(new JLabel("Doctor:"));
        doctorComboBox = new JComboBox<>();
        loadDoctors(); // Load doctors from the database
        add(doctorComboBox);

        add(new JLabel("Appointment Date:"));
        dateComboBox = new JComboBox<>(new String[]{"2025-12-01",
            "2024-12-02",
            "2024-12-03",
            "2024-12-04",
            "2024-12-05",
            "2024-12-06",
            "2024-12-07"});
        add(dateComboBox);

        add(new JLabel("Appointment Time:"));
        timeComboBox = new JComboBox<>(new String[]{"08:00",
            "08:30",
            "09:00",
            "09:30",
            "10:00",
            "10:30",
            "11:00",
            "11;30",
            "12:00",
            "12:30",
            "01:00",
            "01:30",
            "02:00",
            "02:30",
            "03:00",
            "03:30",
            "04:00"

        }); // Example times, replace with dynamic data
        add(timeComboBox);
        statusComboBox = new JComboBox<>(new String[]{"Scheduled", "Completed", "Cancelled"}); // Available statuses
        add(statusComboBox);
        statusComboBox.setVisible(false);

        submitButton = new JButton("Submit");
        add(submitButton);

        refreshButton = new JButton("Refresh Slots");
        add(refreshButton);

// Add action listener for the refresh button
        refreshButton.addActionListener(e -> {
            // Get the selected doctor
            DoctorBean selectedDoctor = (DoctorBean) doctorComboBox.getSelectedItem();

            if (selectedDoctor != null) {
                // Reload the available time slots for the selected doctor
                reloadAvailableTimeSlots(selectedDoctor.getDoctorId());
            }
        });

        // Add action listener for submit button
        submitButton.addActionListener(e -> {
            // Get the selected items from the combo boxes
            PatientBean selectedPatient = (PatientBean) patientComboBox.getSelectedItem();
            DoctorBean selectedDoctor = (DoctorBean) doctorComboBox.getSelectedItem();
            String selectedDate = (String) dateComboBox.getSelectedItem();
            String selectedTime = (String) timeComboBox.getSelectedItem();

            // Validate input fields
            if (selectedPatient == null || selectedDoctor == null || selectedDate == null || selectedTime == null) {
                JOptionPane.showMessageDialog(this, "All fields must be filled in. Please select valid options.");
                return; // Do not proceed with submission if any field is null or empty
            }

            // Create an AppointmentBean object
            AppointmentBean appointment = new AppointmentBean();
            appointment.setPatientId(selectedPatient.getIdNo());
            appointment.setDoctorId(selectedDoctor.getDoctorId());
            appointment.setAppointmentDate(selectedDate);
            appointment.setAppointmentTime(selectedTime);
            appointment.setStatus("Scheduled"); // Default status

            // Check if the doctor is available
            if (isDoctorAvailable(appointment.getDoctorId(), appointment.getAppointmentDate(), appointment.getAppointmentTime())) {
                // Insert appointment data if available
                insertAppointmentData(appointment);

                // Update doctor's status to 'Booked'
                updateDoctorStatus(appointment.getDoctorId());

       
                

                // Show success message
                JOptionPane.showMessageDialog(this, "Appointment Scheduled Successfully!");

                // Clear the form
                clearForm();
            } else {
                // Show error message if doctor is not available
                JOptionPane.showMessageDialog(this, "Selected doctor is not available at the chosen time. Please select a different slot.");
            }
        });

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void updateDoctorStatus(int doctorId) {
        String updateQuery = "UPDATE Doctors SET doctor_availability = 'Unavailable' WHERE doctor_id = ?";
        try (Connection conn = DriverManager.getConnection(url, username, password); PreparedStatement stmt = conn.prepareStatement(updateQuery)) {

            stmt.setInt(1, doctorId);
            stmt.executeUpdate(); // Execute the update query

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating doctor's availability: " + e.getMessage());
        }
    }

    private boolean isDoctorAvailable(int doctorId, String date, String time) {
        // Check if the doctor is available in the Doctors table
        String availabilityQuery = "SELECT doctor_availability FROM Doctors WHERE doctor_id = ?";
        try (Connection conn = DriverManager.getConnection(url, username, password); PreparedStatement stmt = conn.prepareStatement(availabilityQuery)) {

            stmt.setInt(1, doctorId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String availability = rs.getString("doctor_availability");
                    if ("Unavailable".equals(availability)) {
                        return false; // Doctor is marked as unavailable in the Doctors table
                    }
                } else {
                    // Handle case where doctor doesn't exist (if necessary)
                    return false;
                }
            }

            // Check if the doctor is already booked for the requested time and date
            String appointmentQuery = "SELECT COUNT(*) FROM Appointments WHERE doctor_id = ? AND appointment_date = ? AND appointment_time = ?";
            try (PreparedStatement stmt2 = conn.prepareStatement(appointmentQuery)) {
                stmt2.setInt(1, doctorId);
                stmt2.setString(2, date);
                stmt2.setString(3, time);

                try (ResultSet rs2 = stmt2.executeQuery()) {
                    if (rs2.next() && rs2.getInt(1) > 0) {
                        return false; // Doctor is already booked at this time
                    }
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error checking doctor availability: " + e.getMessage());
        }
        return true; // Doctor is available
    }

    /**
     * Method to insert appointment data into the database
     */
    private void insertAppointmentData(AppointmentBean appointment) {
        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            String sql = "INSERT INTO Appointments (patient_id, doctor_id, appointment_date, appointment_time, status, patient_first_name, doctor_first_name) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, appointment.getPatientId()); // Patient ID
                stmt.setInt(2, appointment.getDoctorId()); // Doctor ID
                stmt.setString(3, appointment.getAppointmentDate()); // Appointment date
                stmt.setString(4, appointment.getAppointmentTime()); // Appointment time
                stmt.setString(5, appointment.getStatus()); // Status
                stmt.setString(6, ((PatientBean) patientComboBox.getSelectedItem()).getFirstName()); // Patient first name
                stmt.setString(7, ((DoctorBean) doctorComboBox.getSelectedItem()).getFirstName()); // Doctor first name

                stmt.executeUpdate();

            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }
    }

    /**
     * Load patients into the combo box
     */
    private void loadPatients() {
        String query = "SELECT idNo, firstName, lastName FROM Patients"; // Adjust column names here
        try (Connection conn = DriverManager.getConnection(url, username, password); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                PatientBean patient = new PatientBean();
                patient.setIdNo(rs.getString("idNo")); // Assuming 'idNo' is the primary key
                patient.setFirstName(rs.getString("firstName")); // Use correct column names
                patient.setLastName(rs.getString("lastName"));

                // Add the PatientBean object to the combo box
                patientComboBox.addItem(patient); // Add the PatientBean object directly
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    /**
     * Load doctors into the combo box
     */
    private void loadDoctors() {
        // Modify query to filter only available doctors
        String query = "SELECT doctor_id, fname, lname, doctor_availability FROM Doctors WHERE doctor_availability = 'Available'";
        try (Connection conn = DriverManager.getConnection(url, username, password); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {

            // Clear the combo box before adding items
            doctorComboBox.removeAllItems();

            while (rs.next()) {
                DoctorBean doctor = new DoctorBean();
                doctor.setDoctorId(rs.getInt("doctor_id"));
                doctor.setFirstName(rs.getString("fname"));
                doctor.setLastName(rs.getString("lname"));
                doctor.setDoctorAvailability(rs.getString("doctor_availability")); // Set availability

                // Add the DoctorBean object to the combo box
                doctorComboBox.addItem(doctor);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

   private void reloadAvailableTimeSlots(int doctorId) {
    // Clear the existing time slots
    timeComboBox.removeAllItems();

    // Define all possible time slots
    String[] allTimeSlots = {
        "08:00", "08:30", "09:00", "09:30", "10:00", "10:30", "11:00", "11:30", "12:00",
        "12:30", "01:00", "01:30", "02:00", "02:30", "03:00", "03:30", "04:00"
    };

    // Add all time slots to a list to easily check availability
    java.util.List<String> availableTimeSlots = new java.util.ArrayList<>(java.util.Arrays.asList(allTimeSlots));

    // Query the database to find already booked time slots for the selected doctor
    String query = "SELECT appointment_time FROM Appointments WHERE doctor_id = ? AND appointment_date = ?";
    String selectedDate = (String) dateComboBox.getSelectedItem(); // Get selected date

    try (Connection conn = DriverManager.getConnection(url, username, password); PreparedStatement stmt = conn.prepareStatement(query)) {

        stmt.setInt(1, doctorId);
        stmt.setString(2, selectedDate);

        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String bookedTimeSlot = rs.getString("appointment_time");
                availableTimeSlots.remove(bookedTimeSlot); // Remove booked slots from the available list
            }
        }

    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error refreshing available time slots: " + e.getMessage());
    }

    // Add the remaining available time slots to the time combo box
    for (String timeSlot : availableTimeSlots) {
        timeComboBox.addItem(timeSlot);
    }

    // If no slots are available, inform the user
    if (availableTimeSlots.isEmpty()) {
        JOptionPane.showMessageDialog(this, "No available time slots for the selected doctor on this date.");
    }
}


    private void clearForm() {
        // Reset the patient combo box to null (or you can choose to reset it to the
        // first item if preferred)
        patientComboBox.setSelectedItem(null); // Clears the selection

        // Reset the doctor combo box to null
        doctorComboBox.setSelectedItem(null);

        // Reset the date combo box to null
        dateComboBox.setSelectedItem(null);

        // Reset the time combo box to null
        timeComboBox.setSelectedItem(null);

        // Reset the status combo box to null or default value
        statusComboBox.setSelectedItem("Scheduled"); // Or use null to leave it unselected
    }

    /**
     * NetBeans auto-generated method for GUI setup WARNING: Do NOT modify this
     * method manually!
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
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
    // </editor-fold>

    /**
     * Main method to run the application
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            new AppointmentRegistrationForm().setVisible(true);
        });
    }
}
