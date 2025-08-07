package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import database.Database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class StatisticalController {
    @FXML private TableView<Order> orderTable;
    @FXML private TableColumn<Order, String> orderIdColumn, paymentMethodColumn, timeColumn, statusColumn;
    @FXML private TableColumn<Order, Integer> userIdColumn;
    @FXML private TableColumn<Order, Double> totalColumn;
    @FXML private TableColumn<Order, Void> actionColumn; // Added for the action column
    @FXML private ComboBox<String> timeComboBox, paymentComboBox;
    @FXML private Label totalLabel;

    public static class Order {
        private final String orderId;
        private final int userId;
        private final String paymentMethod;
        private final String time;
        private final double total;
        private final String status;

        public Order(String orderId, int userId, String paymentMethod, String time, double total, String status) {
            this.orderId = orderId;
            this.userId = userId;
            this.paymentMethod = paymentMethod;
            this.time = time;
            this.total = total;
            this.status = status;
        }

        public String getOrderId() { return orderId; }
        public int getUserId() { return userId; }
        public String getPaymentMethod() { return paymentMethod; }
        public String getTime() { return time; }
        public double getTotal() { return total; }
        public String getStatus() { return status; }
    }

    @FXML
    public void initialize() {
        // Thiết lập cột bảng
        orderIdColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getOrderId()));
        userIdColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getUserId()).asObject());
        paymentMethodColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPaymentMethod()));
        timeColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTime()));
        totalColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getTotal()).asObject());
        statusColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus()));

        // Set up the action column with a "View Invoice" button
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewInvoiceButton = new Button("Xem hóa đơn");

            {
                viewInvoiceButton.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    viewInvoice(order.getOrderId(), order.getUserId());
                });
                viewInvoiceButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 12px;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(viewInvoiceButton);
                }
            }
        });

        // Thiết lập ComboBox
        timeComboBox.setItems(FXCollections.observableArrayList("Tất cả", "Hôm nay", "Tháng này", "Năm nay"));
        paymentComboBox.setItems(FXCollections.observableArrayList("Tất cả", "Thanh toán khi nhận hàng", "Chuyển khoản ngân hàng", "Ví điện tử"));
        timeComboBox.getSelectionModel().selectFirst();
        paymentComboBox.getSelectionModel().selectFirst();

        timeComboBox.setOnAction(event -> loadOrders());
        paymentComboBox.setOnAction(event -> loadOrders());

        // Load orders initially
        loadOrders();
    }

    public void loadOrders() {
        orderTable.getItems().clear();
        String timeFilter = timeComboBox.getValue();
        String paymentFilter = paymentComboBox.getValue();

        // Explicitly list the columns we need
        String sql = "SELECT ma_don_hang, user_id, phuong_thuc_thanh_toan, thoi_gian, tong_tien, status FROM don_hang WHERE 1=1";
        if (!timeFilter.equals("Tất cả")) {
            if (timeFilter.equals("Hôm nay")) {
                sql += " AND DATE(thoi_gian) = CURDATE()";
            } else if (timeFilter.equals("Tháng này")) {
                sql += " AND MONTH(thoi_gian) = MONTH(CURDATE()) AND YEAR(thoi_gian) = YEAR(CURDATE())";
            } else if (timeFilter.equals("Năm nay")) {
                sql += " AND YEAR(thoi_gian) = YEAR(CURDATE())";
            }
        }
        if (!paymentFilter.equals("Tất cả")) {
            sql += " AND phuong_thuc_thanh_toan = ?";
        }

        Connection conn = Database.connectDB();
        if (conn == null) {
            showError("Không thể kết nối tới cơ sở dữ liệu!");
            return;
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (!paymentFilter.equals("Tất cả")) {
                stmt.setString(1, paymentFilter);
            }
            ResultSet rs = stmt.executeQuery();
            double totalSum = 0;
            while (rs.next()) {
                Order order = new Order(
                    rs.getString("ma_don_hang"),
                    rs.getInt("user_id"),
                    rs.getString("phuong_thuc_thanh_toan"),
                    rs.getTimestamp("thoi_gian").toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
                    rs.getDouble("tong_tien"),
                    rs.getString("status")
                );
                orderTable.getItems().add(order);

                // Only add to totalSum if status is 'active'
                if (order.getStatus().equalsIgnoreCase("active")) {
                    System.out.println("Including order " + order.getOrderId() + " in total: " + order.getTotal());
                    totalSum += order.getTotal();
                } else {
                    System.out.println("Excluding order " + order.getOrderId() + " from total (status: " + order.getStatus() + ")");
                }
            }
            System.out.println("Total sum of active orders: " + totalSum);
            totalLabel.setText(String.format("%.0f VNĐ", totalSum));
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Lỗi tải đơn hàng: " + e.getMessage());
        } finally {
            Database.closeConnection(conn);
        }
    }

    private void viewInvoice(String orderId, int userId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/invoice.fxml"));
            Parent root = loader.load();
            InvoiceController invoiceController = loader.getController();
            invoiceController.setOrderId(orderId);
            invoiceController.setUserId(userId);
            invoiceController.loadInvoiceDetails();

            Stage stage = new Stage();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Hóa đơn");
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Không thể mở trang hóa đơn: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.showAndWait();
    }
}