package com.hotelmanagement;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class Dashboard {

    public void start(Stage stage) {
        stage.setTitle("Hotel Management - Dashboard");

        // --- Tabs ---
        TabPane tabPane = new TabPane();

        Tab roomsTab = new Tab("Rooms");
        roomsTab.setContent(createRoomsTab());
        roomsTab.setClosable(false);

        Tab reservationsTab = new Tab("Reservations");
        reservationsTab.setContent(createReservationsTab());
        reservationsTab.setClosable(false);

        Tab customersTab = new Tab("Customers");
        customersTab.setContent(createCustomersTab());
        customersTab.setClosable(false);

        tabPane.getTabs().addAll(roomsTab, reservationsTab, customersTab);

        // --- Logout button ---
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(e -> {
            LoginPage loginPage = new LoginPage();
            loginPage.start(stage);
        });

        BorderPane root = new BorderPane();
        root.setTop(logoutButton);
        root.setCenter(tabPane);

        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    // =================== Rooms ===================
    private BorderPane createRoomsTab() {
        TableView<Room> table = new TableView<>();
        ObservableList<Room> data = getRoomsFromDB();

        TableColumn<Room, Integer> colNumber = new TableColumn<>("Room Number");
        colNumber.setCellValueFactory(new PropertyValueFactory<>("number"));

        TableColumn<Room, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));

        TableColumn<Room, Boolean> colAvailable = new TableColumn<>("Available");
        colAvailable.setCellValueFactory(new PropertyValueFactory<>("available"));

        table.setItems(data);
        table.getColumns().addAll(colNumber, colType, colAvailable);

        Button addButton = new Button("Add Room");
        Button editButton = new Button("Edit Room");
        Button deleteButton = new Button("Delete Room");
        TextField searchField = new TextField();
        searchField.setPromptText("Search...");

        HBox controls = new HBox(10, addButton, editButton, deleteButton, searchField);

        BorderPane pane = new BorderPane();
        pane.setTop(controls);
        pane.setCenter(table);

        return pane;
    }

    private ObservableList<Room> getRoomsFromDB() {
        ObservableList<Room> list = FXCollections.observableArrayList();
        try (Connection conn = Database.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM rooms");
            while (rs.next()) {
                list.add(new Room(
                        rs.getInt("number"),
                        rs.getString("type"),
                        rs.getBoolean("available")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // =================== Reservations ===================
    private BorderPane createReservationsTab() {
        TableView<Reservation> table = new TableView<>();
        ObservableList<Reservation> data = getReservationsFromDB();

        TableColumn<Reservation, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Reservation, String> colCustomer = new TableColumn<>("Customer");
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));

        TableColumn<Reservation, Integer> colRoom = new TableColumn<>("Room");
        colRoom.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));

        TableColumn<Reservation, String> colCheckIn = new TableColumn<>("Check-in");
        colCheckIn.setCellValueFactory(new PropertyValueFactory<>("checkIn"));

        TableColumn<Reservation, String> colCheckOut = new TableColumn<>("Check-out");
        colCheckOut.setCellValueFactory(new PropertyValueFactory<>("checkOut"));

        table.setItems(data);
        table.getColumns().addAll(colId, colCustomer, colRoom, colCheckIn, colCheckOut);

        Button addButton = new Button("Add Reservation");
        Button editButton = new Button("Edit Reservation");
        Button deleteButton = new Button("Delete Reservation");
        TextField searchField = new TextField();
        searchField.setPromptText("Search...");

        HBox controls = new HBox(10, addButton, editButton, deleteButton, searchField);

        BorderPane pane = new BorderPane();
        pane.setTop(controls);
        pane.setCenter(table);

        return pane;
    }

    private ObservableList<Reservation> getReservationsFromDB() {
        ObservableList<Reservation> list = FXCollections.observableArrayList();
        try (Connection conn = Database.getConnection()) {
            Statement stmt = conn.createStatement();
            String sql = "SELECT r.id, c.name AS customerName, r.room_number, r.check_in, r.check_out " +
                    "FROM reservations r " +
                    "JOIN customers c ON r.customer_id = c.id";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                list.add(new Reservation(
                        rs.getInt("id"),
                        rs.getString("customerName"),
                        rs.getInt("room_number"),
                        rs.getString("check_in"),
                        rs.getString("check_out")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // =================== Customers ===================
    private BorderPane createCustomersTab() {
        TableView<Customer> table = new TableView<>();
        ObservableList<Customer> data = getCustomersFromDB();

        TableColumn<Customer, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Customer, String> colName = new TableColumn<>("Name");
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Customer, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        table.setItems(data);
        table.getColumns().addAll(colId, colName, colEmail);

        Button addButton = new Button("Add Customer");
        Button editButton = new Button("Edit Customer");
        Button deleteButton = new Button("Delete Customer");
        TextField searchField = new TextField();
        searchField.setPromptText("Search...");

        HBox controls = new HBox(10, addButton, editButton, deleteButton, searchField);

        BorderPane pane = new BorderPane();
        pane.setTop(controls);
        pane.setCenter(table);

        return pane;
    }

    private ObservableList<Customer> getCustomersFromDB() {
        ObservableList<Customer> list = FXCollections.observableArrayList();
        try (Connection conn = Database.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM customers");
            while (rs.next()) {
                list.add(new Customer(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // =================== Nested Model Classes ===================
    public static class Room {
        private int number;
        private String type;
        private boolean available;

        public Room(int number, String type, boolean available) {
            this.number = number;
            this.type = type;
            this.available = available;
        }

        public int getNumber() { return number; }
        public String getType() { return type; }
        public boolean getAvailable() { return available; }
    }

    public static class Reservation {
        private int id;
        private String customerName;
        private int roomNumber;
        private String checkIn;
        private String checkOut;

        public Reservation(int id, String customerName, int roomNumber, String checkIn, String checkOut) {
            this.id = id;
            this.customerName = customerName;
            this.roomNumber = roomNumber;
            this.checkIn = checkIn;
            this.checkOut = checkOut;
        }

        public int getId() { return id; }
        public String getCustomerName() { return customerName; }
        public int getRoomNumber() { return roomNumber; }
        public String getCheckIn() { return checkIn; }
        public String getCheckOut() { return checkOut; }
    }

    public static class Customer {
        private int id;
        private String name;
        private String email;

        public Customer(int id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
    }
}
