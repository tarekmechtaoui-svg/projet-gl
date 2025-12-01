package com.hotelmanagement;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        LoginPage loginPage = new LoginPage();
        loginPage.start(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

