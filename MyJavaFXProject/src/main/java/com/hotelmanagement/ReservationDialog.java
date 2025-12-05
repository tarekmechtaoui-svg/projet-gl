package com.hotelmanagement;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ReservationDialog {
    private Stage stage;
    private boolean confirmed = false;
    private ComboBox<String> customerComboBox;
    private ComboBox<Integer> roomComboBox;
    private DatePicker checkInPicker;
    private DatePicker checkOutPicker;
    private Integer reservationId;

    public ReservationDialog(Stage owner, Integer existingReservationId) {
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);

        this.reservationId = existingReservationId;

        if (existingReservationId != null) {
            stage.setTitle("Edit Reservation");
        } else {
            stage.setTitle("Add Reservation");
        }

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(15);

        Label customerLabel = new Label("Customer:");
        customerComboBox = new ComboBox<>();
        loadCustomers();
        customerComboBox.setPrefWidth(200);

        Label roomLabel = new Label("Room:");
        roomComboBox = new ComboBox<>();
        loadAvailableRooms(existingReservationId);
        roomComboBox.setPrefWidth(200);

        Label checkInLabel = new Label("Check-In Date:");
        checkInPicker = new DatePicker();
        checkInPicker.setPrefWidth(200);

        Label checkOutLabel = new Label("Check-Out Date:");
        checkOutPicker = new DatePicker();
        checkOutPicker.setPrefWidth(200);

        Button saveButton = new Button("Save");
        saveButton.setStyle(
                "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");
        saveButton.setOnAction(e -> {
            if (validateInput()) {
                confirmed = true;
                stage.close();
            }
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-padding: 8 20;");
        cancelButton.setOnAction(e -> stage.close());

        grid.add(customerLabel, 0, 0);
        grid.add(customerComboBox, 1, 0);
        grid.add(roomLabel, 0, 1);
        grid.add(roomComboBox, 1, 1);
        grid.add(checkInLabel, 0, 2);
        grid.add(checkInPicker, 1, 2);
        grid.add(checkOutLabel, 0, 3);
        grid.add(checkOutPicker, 1, 3);
        grid.add(saveButton, 0, 4);
        grid.add(cancelButton, 1, 4);

        if (existingReservationId != null) {
            loadReservationData(existingReservationId);
        }

        Scene scene = new Scene(grid, 400, 300);
        stage.setScene(scene);
    }

    private void loadCustomers() {
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT id, name FROM customers ORDER BY name";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                customerComboBox.getItems().add(rs.getInt("id") + " - " + rs.getString("name"));
            }
        } catch (Exception e) {
            showError("Error loading customers: " + e.getMessage());
        }
    }

    // In ReservationDialog.java, update loadAvailableRooms method
    private void loadAvailableRooms(Integer hotelId) {
        try (Connection conn = Database.getConnection()) {
            String sql;
            PreparedStatement pstmt;

            if (hotelId != null) {
                // Load rooms for specific hotel
                sql = "SELECT number, type FROM rooms WHERE available = true AND hotel_id = ? ORDER BY number";
                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, hotelId);
            } else {
                // Load all available rooms
                sql = "SELECT number, type FROM rooms WHERE available = true ORDER BY number";
                pstmt = conn.prepareStatement(sql);
            }

            ResultSet rs = pstmt.executeQuery();

            roomComboBox.getItems().clear();
            while (rs.next()) {
                roomComboBox.getItems().add(rs.getInt("number"));
            }
        } catch (Exception e) {
            showError("Error loading rooms: " + e.getMessage());
        }
    }

    private void loadReservationData(int reservationId) {
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT r.id, r.customer_id, c.name, r.room_number, r.check_in, r.check_out " +
                    "FROM reservations r " +
                    "JOIN customers c ON r.customer_id = c.id " +
                    "WHERE r.id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, reservationId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int customerId = rs.getInt("customer_id");
                String customerName = rs.getString("name");
                customerComboBox.setValue(customerId + " - " + customerName);
                customerComboBox.setDisable(true);

                roomComboBox.getItems().clear();
                int currentRoom = rs.getInt("room_number");
                roomComboBox.getItems().add(currentRoom);
                loadAvailableRooms(currentRoom);
                roomComboBox.setValue(currentRoom);

                checkInPicker.setValue(rs.getDate("check_in").toLocalDate());
                checkOutPicker.setValue(rs.getDate("check_out").toLocalDate());
            }
        } catch (Exception e) {
            showError("Error loading reservation data: " + e.getMessage());
        }
    }

    private boolean validateInput() {
        if (customerComboBox.getValue() == null || customerComboBox.getValue().trim().isEmpty()) {
            showError("Customer is required!");
            return false;
        }

        if (roomComboBox.getValue() == null) {
            showError("Room is required!");
            return false;
        }

        if (checkInPicker.getValue() == null) {
            showError("Check-In date is required!");
            return false;
        }

        if (checkOutPicker.getValue() == null) {
            showError("Check-Out date is required!");
            return false;
        }

        if (checkOutPicker.getValue().isBefore(checkInPicker.getValue()) ||
                checkOutPicker.getValue().isEqual(checkInPicker.getValue())) {
            showError("Check-Out date must be after Check-In date!");
            return false;
        }

        return true;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void showAndWait() {
        stage.showAndWait();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public int getCustomerId() {
        String value = customerComboBox.getValue();
        return Integer.parseInt(value.split(" - ")[0]);
    }

    public int getRoomNumber() {
        return roomComboBox.getValue();
    }

    public String getCheckIn() {
        return checkInPicker.getValue().toString();
    }

    public String getCheckOut() {
        return checkOutPicker.getValue().toString();
    }
}
