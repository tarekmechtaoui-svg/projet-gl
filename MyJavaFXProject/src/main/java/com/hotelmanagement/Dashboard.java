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
import java.sql.PreparedStatement;
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

        Button logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 25; -fx-cursor: hand;");
        logoutButton.setOnAction(e -> {
            LoginPage loginPage = new LoginPage();
            loginPage.start(stage);
        });

        Label titleLabel = new Label("Hotel Management System");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");

        HBox topBar = new HBox();
        topBar.setSpacing(10);
        topBar.setStyle("-fx-padding: 15; -fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 2 0;");
        topBar.getChildren().addAll(titleLabel, new javafx.scene.layout.Region());
        HBox.setHgrow(topBar.getChildren().get(1), javafx.geometry.Priority.ALWAYS);
        topBar.getChildren().add(logoutButton);

        tabPane.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(tabPane);
        root.setStyle("-fx-background-color: #fafafa;");

        Scene scene = new Scene(root, 1000, 700);
        stage.setScene(scene);
        stage.show();
    }

    // =================== Rooms ===================
    private BorderPane createRoomsTab() {
        TableView<Room> table = new TableView<>();
        ObservableList<Room> data = getRoomsFromDB();
        ObservableList<Room> filteredData = FXCollections.observableArrayList(data);

        TableColumn<Room, Integer> colNumber = new TableColumn<>("Room Number");
        colNumber.setCellValueFactory(new PropertyValueFactory<>("number"));
        colNumber.setPrefWidth(150);

        TableColumn<Room, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colType.setPrefWidth(150);

        TableColumn<Room, Boolean> colAvailable = new TableColumn<>("Available");
        colAvailable.setCellValueFactory(new PropertyValueFactory<>("available"));
        colAvailable.setPrefWidth(120);

        table.setItems(filteredData);
        table.getColumns().addAll(colNumber, colType, colAvailable);
        table.setStyle("-fx-font-size: 13px;");

        Button addButton = new Button("+ Add Room");
        addButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand;");
        addButton.setOnAction(e -> handleAddRoom(table, data, filteredData));

        Button editButton = new Button("Edit Room");
        editButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand;");
        editButton.setOnAction(e -> handleEditRoom(table, data, filteredData));

        Button deleteButton = new Button("Delete Room");
        deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand;");
        deleteButton.setOnAction(e -> handleDeleteRoom(table, data, filteredData));

        TextField searchField = new TextField();
        searchField.setPromptText("Search by room number or type...");
        searchField.setPrefWidth(250);
        searchField.setStyle("-fx-padding: 10; -fx-font-size: 13px;");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterRooms(newVal, data, filteredData);
        });

        HBox controls = new HBox(15, addButton, editButton, deleteButton, searchField);
        controls.setStyle("-fx-padding: 15; -fx-background-color: #f5f5f5;");

        BorderPane pane = new BorderPane();
        pane.setTop(controls);
        pane.setCenter(table);
        pane.setStyle("-fx-background-color: white;");

        return pane;
    }

    private void handleAddRoom(TableView<Room> table, ObservableList<Room> data, ObservableList<Room> filteredData) {
        Stage stage = (Stage) table.getScene().getWindow();
        RoomDialog dialog = new RoomDialog(stage, null);
        dialog.showAndWait();

        if (dialog.isConfirmed()) {
            try (Connection conn = Database.getConnection()) {
                String sql = "INSERT INTO rooms (number, type, available) VALUES (?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, dialog.getRoomNumber());
                pstmt.setString(2, dialog.getRoomType());
                pstmt.setBoolean(3, dialog.isAvailable());
                pstmt.executeUpdate();

                Room newRoom = new Room(dialog.getRoomNumber(), dialog.getRoomType(), dialog.isAvailable());
                data.add(newRoom);
                filteredData.add(newRoom);

                showSuccess("Room added successfully!");
            } catch (Exception e) {
                showError("Error adding room: " + e.getMessage());
            }
        }
    }

    private void handleEditRoom(TableView<Room> table, ObservableList<Room> data, ObservableList<Room> filteredData) {
        Room selectedRoom = table.getSelectionModel().getSelectedItem();
        if (selectedRoom == null) {
            showError("Please select a room to edit!");
            return;
        }

        Stage stage = (Stage) table.getScene().getWindow();
        RoomDialog dialog = new RoomDialog(stage, selectedRoom.getNumber());
        dialog.showAndWait();

        if (dialog.isConfirmed()) {
            try (Connection conn = Database.getConnection()) {
                String sql = "UPDATE rooms SET type = ?, available = ? WHERE number = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, dialog.getRoomType());
                pstmt.setBoolean(2, dialog.isAvailable());
                pstmt.setInt(3, selectedRoom.getNumber());
                pstmt.executeUpdate();

                data.remove(selectedRoom);
                filteredData.remove(selectedRoom);
                Room updatedRoom = new Room(selectedRoom.getNumber(), dialog.getRoomType(), dialog.isAvailable());
                data.add(updatedRoom);
                filteredData.add(updatedRoom);

                showSuccess("Room updated successfully!");
            } catch (Exception e) {
                showError("Error updating room: " + e.getMessage());
            }
        }
    }

    private void handleDeleteRoom(TableView<Room> table, ObservableList<Room> data, ObservableList<Room> filteredData) {
        Room selectedRoom = table.getSelectionModel().getSelectedItem();
        if (selectedRoom == null) {
            showError("Please select a room to delete!");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Room #" + selectedRoom.getNumber());
        confirmAlert.setContentText("Are you sure you want to delete this room? This action cannot be undone.");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = Database.getConnection()) {
                    String sql = "DELETE FROM rooms WHERE number = ?";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, selectedRoom.getNumber());
                    pstmt.executeUpdate();

                    data.remove(selectedRoom);
                    filteredData.remove(selectedRoom);

                    showSuccess("Room deleted successfully!");
                } catch (Exception e) {
                    showError("Error deleting room: " + e.getMessage());
                }
            }
        });
    }

    private void filterRooms(String searchText, ObservableList<Room> data, ObservableList<Room> filteredData) {
        filteredData.clear();
        if (searchText == null || searchText.trim().isEmpty()) {
            filteredData.addAll(data);
        } else {
            String lowerSearch = searchText.toLowerCase();
            for (Room room : data) {
                if (String.valueOf(room.getNumber()).contains(lowerSearch) ||
                    room.getType().toLowerCase().contains(lowerSearch)) {
                    filteredData.add(room);
                }
            }
        }
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
        ObservableList<Reservation> filteredData = FXCollections.observableArrayList(data);

        TableColumn<Reservation, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(80);

        TableColumn<Reservation, String> colCustomer = new TableColumn<>("Customer");
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colCustomer.setPrefWidth(150);

        TableColumn<Reservation, Integer> colRoom = new TableColumn<>("Room");
        colRoom.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colRoom.setPrefWidth(100);

        TableColumn<Reservation, String> colCheckIn = new TableColumn<>("Check-in");
        colCheckIn.setCellValueFactory(new PropertyValueFactory<>("checkIn"));
        colCheckIn.setPrefWidth(130);

        TableColumn<Reservation, String> colCheckOut = new TableColumn<>("Check-out");
        colCheckOut.setCellValueFactory(new PropertyValueFactory<>("checkOut"));
        colCheckOut.setPrefWidth(130);

        table.setItems(filteredData);
        table.getColumns().addAll(colId, colCustomer, colRoom, colCheckIn, colCheckOut);
        table.setStyle("-fx-font-size: 13px;");

        Button addButton = new Button("+ Add Reservation");
        addButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand;");
        addButton.setOnAction(e -> handleAddReservation(table, data, filteredData));

        Button editButton = new Button("Edit Reservation");
        editButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand;");
        editButton.setOnAction(e -> handleEditReservation(table, data, filteredData));

        Button deleteButton = new Button("Delete Reservation");
        deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand;");
        deleteButton.setOnAction(e -> handleDeleteReservation(table, data, filteredData));

        TextField searchField = new TextField();
        searchField.setPromptText("Search by customer or room...");
        searchField.setPrefWidth(250);
        searchField.setStyle("-fx-padding: 10; -fx-font-size: 13px;");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterReservations(newVal, data, filteredData);
        });

        HBox controls = new HBox(15, addButton, editButton, deleteButton, searchField);
        controls.setStyle("-fx-padding: 15; -fx-background-color: #f5f5f5;");

        BorderPane pane = new BorderPane();
        pane.setTop(controls);
        pane.setCenter(table);
        pane.setStyle("-fx-background-color: white;");

        return pane;
    }

    private void handleAddReservation(TableView<Reservation> table, ObservableList<Reservation> data, ObservableList<Reservation> filteredData) {
        Stage stage = (Stage) table.getScene().getWindow();
        ReservationDialog dialog = new ReservationDialog(stage, null);
        dialog.showAndWait();

        if (dialog.isConfirmed()) {
            try (Connection conn = Database.getConnection()) {
                String sql = "INSERT INTO reservations (customer_id, room_number, check_in, check_out) VALUES (?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, dialog.getCustomerId());
                pstmt.setInt(2, dialog.getRoomNumber());
                pstmt.setString(3, dialog.getCheckIn());
                pstmt.setString(4, dialog.getCheckOut());
                pstmt.executeUpdate();

                updateRoomsAvailability();
                ObservableList<Reservation> newData = getReservationsFromDB();
                data.clear();
                data.addAll(newData);
                filteredData.clear();
                filteredData.addAll(newData);

                showSuccess("Reservation added successfully!");
            } catch (Exception e) {
                showError("Error adding reservation: " + e.getMessage());
            }
        }
    }

    private void handleEditReservation(TableView<Reservation> table, ObservableList<Reservation> data, ObservableList<Reservation> filteredData) {
        Reservation selectedReservation = table.getSelectionModel().getSelectedItem();
        if (selectedReservation == null) {
            showError("Please select a reservation to edit!");
            return;
        }

        Stage stage = (Stage) table.getScene().getWindow();
        ReservationDialog dialog = new ReservationDialog(stage, selectedReservation.getId());
        dialog.showAndWait();

        if (dialog.isConfirmed()) {
            try (Connection conn = Database.getConnection()) {
                String sql = "UPDATE reservations SET check_in = ?, check_out = ? WHERE id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, dialog.getCheckIn());
                pstmt.setString(2, dialog.getCheckOut());
                pstmt.setInt(3, selectedReservation.getId());
                pstmt.executeUpdate();

                updateRoomsAvailability();
                ObservableList<Reservation> newData = getReservationsFromDB();
                data.clear();
                data.addAll(newData);
                filteredData.clear();
                filteredData.addAll(newData);

                showSuccess("Reservation updated successfully!");
            } catch (Exception e) {
                showError("Error updating reservation: " + e.getMessage());
            }
        }
    }

    private void handleDeleteReservation(TableView<Reservation> table, ObservableList<Reservation> data, ObservableList<Reservation> filteredData) {
        Reservation selectedReservation = table.getSelectionModel().getSelectedItem();
        if (selectedReservation == null) {
            showError("Please select a reservation to delete!");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Reservation #" + selectedReservation.getId());
        confirmAlert.setContentText("Are you sure you want to delete this reservation? This action cannot be undone.");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = Database.getConnection()) {
                    String sql = "DELETE FROM reservations WHERE id = ?";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, selectedReservation.getId());
                    pstmt.executeUpdate();

                    updateRoomsAvailability();
                    data.remove(selectedReservation);
                    filteredData.remove(selectedReservation);

                    showSuccess("Reservation deleted successfully!");
                } catch (Exception e) {
                    showError("Error deleting reservation: " + e.getMessage());
                }
            }
        });
    }

    private void filterReservations(String searchText, ObservableList<Reservation> data, ObservableList<Reservation> filteredData) {
        filteredData.clear();
        if (searchText == null || searchText.trim().isEmpty()) {
            filteredData.addAll(data);
        } else {
            String lowerSearch = searchText.toLowerCase();
            for (Reservation res : data) {
                if (res.getCustomerName().toLowerCase().contains(lowerSearch) ||
                    String.valueOf(res.getRoomNumber()).contains(lowerSearch)) {
                    filteredData.add(res);
                }
            }
        }
    }

    private void updateRoomsAvailability() {
        try (Connection conn = Database.getConnection()) {
            String sql = "UPDATE rooms SET available = CASE WHEN number NOT IN " +
                    "(SELECT room_number FROM reservations WHERE CURDATE() BETWEEN check_in AND check_out) " +
                    "THEN true ELSE false END";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
