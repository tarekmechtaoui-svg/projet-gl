package com.hotelmanagement;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class LoginPage {

    public void start(Stage stage) {
        stage.setTitle("Hotel Management - Login");

        Label userLabel = new Label("Username:");
        TextField userField = new TextField();

        Label passLabel = new Label("Password:");
        PasswordField passField = new PasswordField();

        Button loginButton = new Button("Login");
        Label message = new Label();

        loginButton.setOnAction(e -> {
            String user = userField.getText();
            String pass = passField.getText();

            // Simple check, replace with DB check later
            if (user.equals("admin") && pass.equals("1234")) {
                Dashboard dashboard = new Dashboard();
                dashboard.start(stage);
            } else {
                message.setText("Invalid credentials!");
            }
        });

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.add(userLabel, 0, 0);
        grid.add(userField, 1, 0);
        grid.add(passLabel, 0, 1);
        grid.add(passField, 1, 1);
        grid.add(loginButton, 1, 2);
        grid.add(message, 1, 3);

        Scene scene = new Scene(grid, 300, 200);
        stage.setScene(scene);
        stage.show();
    }
}
