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

public class RoomDialog {
    private Stage stage;
    private boolean confirmed = false;
    private TextField numberField;
    private ComboBox<String> typeComboBox;
    private ComboBox<String> hotelComboBox; // NEW: Hotel selection
    private CheckBox availableCheckBox;
    private Integer roomId;
    private Integer existingHotelId; // Store hotel ID for editing

    public RoomDialog(Stage owner, Integer existingRoomNumber) {
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);

        this.roomId = existingRoomNumber;

        if (existingRoomNumber != null) {
            stage.setTitle("Edit Room");
        } else {
            stage.setTitle("Add Room");
        }

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(15);

        Label numberLabel = new Label("Room Number:");
        numberField = new TextField();
        numberField.setPrefWidth(200);

        Label typeLabel = new Label("Room Type:");
        typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll("Single", "Double", "Suite", "Deluxe");
        typeComboBox.setValue("Single");
        typeComboBox.setPrefWidth(200);

        // NEW: Hotel selection combo box
        Label hotelLabel = new Label("Hotel:");
        hotelComboBox = new ComboBox<>();
        loadHotels(); // Load hotels from database
        hotelComboBox.setPrefWidth(200);

        Label availableLabel = new Label("Available:");
        availableCheckBox = new CheckBox();
        availableCheckBox.setSelected(true);

        Button saveButton = new Button("Save");
        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");
        saveButton.setOnAction(e -> {
            if (validateInput()) {
                confirmed = true;
                stage.close();
            }
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-padding: 8 20;");
        cancelButton.setOnAction(e -> stage.close());

        grid.add(numberLabel, 0, 0);
        grid.add(numberField, 1, 0);
        grid.add(typeLabel, 0, 1);
        grid.add(typeComboBox, 1, 1);
        grid.add(hotelLabel, 0, 2); // NEW: Hotel row
        grid.add(hotelComboBox, 1, 2); // NEW: Hotel row
        grid.add(availableLabel, 0, 3);
        grid.add(availableCheckBox, 1, 3);
        grid.add(saveButton, 0, 4);
        grid.add(cancelButton, 1, 4);

        if (existingRoomNumber != null) {
            loadRoomData(existingRoomNumber);
            numberField.setDisable(true);
        }

        Scene scene = new Scene(grid, 350, 300); // Increased height for new field
        stage.setScene(scene);
    }

    // NEW: Load hotels from database
    private void loadHotels() {
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT id, name FROM hotels ORDER BY name";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                hotelComboBox.getItems().add(rs.getInt("id") + " - " + rs.getString("name"));
            }
        } catch (Exception e) {
            showError("Error loading hotels: " + e.getMessage());
        }
    }

    private void loadRoomData(int roomNumber) {
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT r.*, h.name as hotel_name FROM rooms r " +
                        "LEFT JOIN hotels h ON r.hotel_id = h.id " +
                        "WHERE r.number = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, roomNumber);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                numberField.setText(String.valueOf(rs.getInt("number")));
                typeComboBox.setValue(rs.getString("type"));
                availableCheckBox.setSelected(rs.getBoolean("available"));
                
                // Set hotel if exists
                if (rs.getInt("hotel_id") > 0) {
                    hotelComboBox.setValue(rs.getInt("hotel_id") + " - " + rs.getString("hotel_name"));
                    existingHotelId = rs.getInt("hotel_id");
                }
            }
        } catch (Exception e) {
            showError("Error loading room data: " + e.getMessage());
        }
    }

    private boolean validateInput() {
        if (numberField.getText().trim().isEmpty()) {
            showError("Room number is required!");
            return false;
        }

        try {
            Integer.parseInt(numberField.getText().trim());
        } catch (NumberFormatException e) {
            showError("Room number must be a valid number!");
            return false;
        }

        if (typeComboBox.getValue() == null || typeComboBox.getValue().trim().isEmpty()) {
            showError("Room type is required!");
            return false;
        }

        // NEW: Validate hotel selection
        if (hotelComboBox.getValue() == null || hotelComboBox.getValue().trim().isEmpty()) {
            showError("Hotel selection is required!");
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

    public int getRoomNumber() {
        return Integer.parseInt(numberField.getText().trim());
    }

    public String getRoomType() {
        return typeComboBox.getValue();
    }

    // NEW: Get selected hotel ID
    public int getHotelId() {
        String value = hotelComboBox.getValue();
        return Integer.parseInt(value.split(" - ")[0]);
    }

    public boolean isAvailable() {
        return availableCheckBox.isSelected();
    }
}