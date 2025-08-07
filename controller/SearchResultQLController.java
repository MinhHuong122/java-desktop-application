package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import database.Database;
import model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SearchResultQLController {
    @FXML private Label titleLabel;
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Integer> productIdColumn, productQuantityColumn;
    @FXML private TableColumn<Product, String> productNameColumn;
    @FXML private TableColumn<Product, Double> productPriceColumn;
    @FXML private TableColumn<Product, Boolean> productFeaturedColumn;

    @FXML private TableView<Order> orderTable;
    @FXML private TableColumn<Order, String> orderIdColumn, orderTimeColumn, orderPaymentColumn;
    @FXML private TableColumn<Order, Integer> orderUserIdColumn;
    @FXML private TableColumn<Order, Double> orderTotalColumn;

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> userIdColumn, userPointsColumn;
    @FXML private TableColumn<User, String> userEmailColumn, userUpdateDateColumn;

    public static class Order {
        private final String orderId;
        private final int userId;
        private final double total;
        private final String time;
        private final String paymentMethod;

        public Order(String orderId, int userId, double total, String time, String paymentMethod) {
            this.orderId = orderId;
            this.userId = userId;
            this.total = total;
            this.time = time;
            this.paymentMethod = paymentMethod;
        }

        public String getOrderId() { return orderId; }
        public int getUserId() { return userId; }
        public double getTotal() { return total; }
        public String getTime() { return time; }
        public String getPaymentMethod() { return paymentMethod; }
    }

    public static class User {
        private final int id;
        private final String email;
        private final int points;
        private final String updateDate;

        public User(int id, String email, int points, String updateDate) {
            this.id = id;
            this.email = email;
            this.points = points;
            this.updateDate = updateDate;
        }

        public int getId() { return id; }
        public String getEmail() { return email; }
        public int getPoints() { return points; }
        public String getUpdateDate() { return updateDate; }
    }

    @FXML
    public void initialize() {
        // Thiết lập cột cho bảng sản phẩm
        productIdColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        productNameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        productPriceColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getPrice()).asObject());
        productQuantityColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject());
        productFeaturedColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleBooleanProperty(cellData.getValue().isFeatured()));

        // Thiết lập cột cho bảng đơn hàng
        orderIdColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getOrderId()));
        orderUserIdColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getUserId()).asObject());
        orderTotalColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getTotal()).asObject());
        orderTimeColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTime()));
        orderPaymentColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPaymentMethod()));

        // Thiết lập cột cho bảng khách hàng
        userIdColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        userEmailColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEmail()));
        userPointsColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getPoints()).asObject());
        userUpdateDateColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getUpdateDate()));
    }

    public void displaySearchResults(String query) {
        // Tìm kiếm sản phẩm
        productTable.getItems().clear();
        Connection conn = Database.connectDB();
        String sql = "SELECT * FROM san_pham WHERE ten LIKE ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + query + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Product product = new Product(
                    rs.getInt("ma_san_pham"),
                    rs.getString("ma_loai_san_pham"),
                    rs.getString("ten"),
                    rs.getDouble("gia"),
                    rs.getString("hinh_anh")
                );
                product.setQuantity(rs.getInt("so_luong"));
                product.setFeatured(rs.getBoolean("noi_bat"));
                productTable.getItems().add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Lỗi tìm kiếm sản phẩm: " + e.getMessage());
        }

        // Tìm kiếm đơn hàng
        orderTable.getItems().clear();
        sql = "SELECT * FROM don_hang WHERE ma_don_hang LIKE ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + query + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Order order = new Order(
                    rs.getString("ma_don_hang"),
                    rs.getInt("user_id"),
                    rs.getDouble("tong_tien"),
                    rs.getString("thoi_gian"),
                    rs.getString("phuong_thuc_thanh_toan")
                );
                orderTable.getItems().add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Lỗi tìm kiếm đơn hàng: " + e.getMessage());
        }

        // Tìm kiếm khách hàng
        // Tìm kiếm khách hàng
        userTable.getItems().clear();
        sql = "SELECT u.user_id, u.email, d.diem_tich_luy, d.ngay_cap_nhat " +
            "FROM user u " +
            "LEFT JOIN diem d ON u.user_id = d.user_id " +
            "WHERE u.email LIKE ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + query + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                // Xử lý trường hợp diem_tich_luy và ngay_cap_nhat có thể là null
                int points = rs.getInt("diem_tich_luy");
                if (rs.wasNull()) {
                    points = 0; // Giá trị mặc định nếu diem_tich_luy là null
                }
                String updateDate = rs.getString("ngay_cap_nhat");
                if (updateDate == null) {
                    updateDate = "Chưa cập nhật"; // Giá trị mặc định nếu ngay_cap_nhat là null
                }
                User user = new User(
                    rs.getInt("user_id"),
                    rs.getString("email"),
                    points,
                    updateDate
                );
                userTable.getItems().add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Lỗi tìm kiếm khách hàng: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.showAndWait();
    }
}