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

public class HotelDialog {
    private Stage stage;
    private boolean confirmed = false;
    private TextField nameField;
    private TextArea descriptionArea;
    private TextArea addressArea;
    private ComboBox<Double> ratingComboBox;
    private Integer hotelId;

    public HotelDialog(Stage owner, Integer existingHotelId) {
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);

        this.hotelId = existingHotelId;

        if (existingHotelId != null) {
            stage.setTitle("Edit Hotel");
        } else {
            stage.setTitle("Add Hotel");
        }

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(15);

        Label nameLabel = new Label("Hotel Name:");
        nameField = new TextField();
        nameField.setPrefWidth(250);

        Label descriptionLabel = new Label("Description:");
        descriptionArea = new TextArea();
        descriptionArea.setPrefWidth(250);
        descriptionArea.setPrefRowCount(3);
        descriptionArea.setWrapText(true);

        Label addressLabel = new Label("Address:");
        addressArea = new TextArea();
        addressArea.setPrefWidth(250);
        addressArea.setPrefRowCount(3);
        addressArea.setWrapText(true);

        Label ratingLabel = new Label("Rating:");
        ratingComboBox = new ComboBox<>();
        ratingComboBox.getItems().addAll(1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0);
        ratingComboBox.setValue(3.0);
        ratingComboBox.setPrefWidth(100);

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

        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(descriptionLabel, 0, 1);
        grid.add(descriptionArea, 1, 1);
        grid.add(addressLabel, 0, 2);
        grid.add(addressArea, 1, 2);
        grid.add(ratingLabel, 0, 3);
        grid.add(ratingComboBox, 1, 3);
        grid.add(saveButton, 0, 4);
        grid.add(cancelButton, 1, 4);

        if (existingHotelId != null) {
            loadHotelData(existingHotelId);
        }

        Scene scene = new Scene(grid, 450, 400);
        stage.setScene(scene);
    }

    private void loadHotelData(int hotelId) {
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT * FROM hotels WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, hotelId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                nameField.setText(rs.getString("name"));
                descriptionArea.setText(rs.getString("description") != null ? rs.getString("description") : "");
                addressArea.setText(rs.getString("address") != null ? rs.getString("address") : "");
                ratingComboBox.setValue(rs.getDouble("rating"));
            }
        } catch (Exception e) {
            showError("Error loading hotel data: " + e.getMessage());
        }
    }

    private boolean validateInput() {
        if (nameField.getText().trim().isEmpty()) {
            showError("Hotel name is required!");
            return false;
        }

        if (ratingComboBox.getValue() == null) {
            showError("Rating is required!");
            return false;
        }

        double rating = ratingComboBox.getValue();
        if (rating < 1.0 || rating > 5.0) {
            showError("Rating must be between 1.0 and 5.0!");
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
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

    public String getHotelName() {
        return nameField.getText().trim();
    }

    public String getDescription() {
        return descriptionArea.getText().trim();
    }

    public String getAddress() {
        return addressArea.getText().trim();
    }

    public double getRating() {
        return ratingComboBox.getValue();
    }
}