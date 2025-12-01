package com.hotelmanagement;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static final String URL = "jdbc:mysql://localhost:3306/hotel_management";
    private static final String USER = "root"; // default WAMP username
    private static final String PASSWORD = ""; // default WAMP password

    // Method to get a connection
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
