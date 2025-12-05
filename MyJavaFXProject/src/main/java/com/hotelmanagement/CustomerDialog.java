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

public class CustomerDialog {
    private Stage stage;
    private boolean confirmed = false;
    private TextField nameField;
    private TextField emailField;
    private TextField phoneField;
    private TextArea addressArea;
    private Integer customerId;

    public CustomerDialog(Stage owner, Integer existingCustomerId) {
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);

        this.customerId = existingCustomerId;

        if (existingCustomerId != null) {
            stage.setTitle("Edit Customer");
        } else {
            stage.setTitle("Add Customer");
        }

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(15);

        Label nameLabel = new Label("Name:");
        nameField = new TextField();
        nameField.setPrefWidth(250);

        Label emailLabel = new Label("Email:");
        emailField = new TextField();
        emailField.setPrefWidth(250);

        Label phoneLabel = new Label("Phone:");
        phoneField = new TextField();
        phoneField.setPrefWidth(250);

        Label addressLabel = new Label("Address:");
        addressArea = new TextArea();
        addressArea.setPrefWidth(250);
        addressArea.setPrefRowCount(3);
        addressArea.setWrapText(true);

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
        grid.add(emailLabel, 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(phoneLabel, 0, 2);
        grid.add(phoneField, 1, 2);
        grid.add(addressLabel, 0, 3);
        grid.add(addressArea, 1, 3);
        grid.add(saveButton, 0, 4);
        grid.add(cancelButton, 1, 4);

        if (existingCustomerId != null) {
            loadCustomerData(existingCustomerId);
        }

        Scene scene = new Scene(grid, 450, 350);
        stage.setScene(scene);
    }

    private void loadCustomerData(int customerId) {
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT * FROM customers WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                nameField.setText(rs.getString("name"));
                emailField.setText(rs.getString("email"));
                phoneField.setText(rs.getString("phone") != null ? rs.getString("phone") : "");
                addressArea.setText(rs.getString("address") != null ? rs.getString("address") : "");
            }
        } catch (Exception e) {
            showError("Error loading customer data: " + e.getMessage());
        }
    }

    private boolean validateInput() {
        if (nameField.getText().trim().isEmpty()) {
            showError("Customer name is required!");
            return false;
        }

        if (emailField.getText().trim().isEmpty()) {
            showError("Email is required!");
            return false;
        }

        if (!isValidEmail(emailField.getText().trim())) {
            showError("Please enter a valid email address!");
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

    public String getCustomerName() {
        return nameField.getText().trim();
    }

    public String getEmail() {
        return emailField.getText().trim();
    }

    public String getPhone() {
        return phoneField.getText().trim();
    }

    public String getAddress() {
        return addressArea.getText().trim();
    }
}
