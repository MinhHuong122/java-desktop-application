package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import database.Database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PayButtonController {

    @FXML private VBox paymentHistoryVBox; // Changed from ListView to VBox
    @FXML private Label orderDetailsLabel;

    private int userId;
    private List<OrderSummary> orderSummaries = new ArrayList<>();

    // Class to hold order summary data
    private static class OrderSummary {
        private String orderId;
        private double total;
        private String paymentMethod;
        private String timestamp;
        private String status;

        public OrderSummary(String orderId, double total, String paymentMethod, String timestamp, String status) {
            this.orderId = orderId;
            this.total = total;
            this.paymentMethod = paymentMethod;
            this.timestamp = timestamp;
            this.status = status;
        }

        public String getOrderId() {
            return orderId;
        }

        public double getTotal() {
            return total;
        }
    }

    @FXML
    public void initialize() {
        if (paymentHistoryVBox == null) {
            System.out.println("Lỗi: paymentHistoryVBox không được liên kết đúng trong FXML!");
            return;
        }
        loadOrdersFromDB();
    }

    public void setUserId(int userId) {
        this.userId = userId;
        loadOrdersFromDB();
    }

    public void loadOrdersFromDB() {
        paymentHistoryVBox.getChildren().clear();
        orderSummaries.clear();

        Connection conn = Database.connectDB();
        String sql = "SELECT dh.ma_don_hang, dh.tong_tien, dh.phuong_thuc_thanh_toan, dh.thoi_gian, dh.status " +
                     "FROM don_hang dh WHERE dh.user_id = ? AND dh.status = 'active'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String orderId = rs.getString("ma_don_hang");
                double total = rs.getDouble("tong_tien");
                String paymentMethod = rs.getString("phuong_thuc_thanh_toan");
                String timestamp = rs.getTimestamp("thoi_gian").toLocalDateTime()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
                String status = rs.getString("status");

                OrderSummary orderSummary = new OrderSummary(orderId, total, paymentMethod, timestamp, status);
                orderSummaries.add(orderSummary);
                addOrderToHistory(orderSummary);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Lỗi", "Lỗi tải lịch sử đơn hàng: " + e.getMessage());
        } finally {
            Database.closeConnection(conn);
        }
    }

    private void addOrderToHistory(OrderSummary orderSummary) {
        Label orderIdLabel = new Label("Mã đơn: " + orderSummary.orderId);
        orderIdLabel.setStyle("-fx-font-size: 14px;");
        orderIdLabel.setPrefWidth(150);

        Label totalLabel = new Label("Tổng tiền: " + orderSummary.total + " VNĐ");
        totalLabel.setStyle("-fx-font-size: 14px;");
        totalLabel.setPrefWidth(150);

        Label paymentMethodLabel = new Label("Phương thức: " + orderSummary.paymentMethod);
        paymentMethodLabel.setStyle("-fx-font-size: 14px;");
        paymentMethodLabel.setPrefWidth(200);

        Label timestampLabel = new Label("Thời gian: " + orderSummary.timestamp);
        timestampLabel.setStyle("-fx-font-size: 14px;");
        timestampLabel.setPrefWidth(200);

        Button viewInvoiceButton = new Button("Xem hóa đơn");
        viewInvoiceButton.setPrefWidth(100);
        viewInvoiceButton.setPrefHeight(30);
        viewInvoiceButton.setOnAction(event -> viewInvoice(orderSummary.orderId));

        Button cancelOrderButton = new Button("Hủy đơn");
        cancelOrderButton.setPrefWidth(100);
        cancelOrderButton.setPrefHeight(30);
        cancelOrderButton.setOnAction(event -> cancelOrder(orderSummary));

        HBox mainContentBox = new HBox(15, orderIdLabel, totalLabel, paymentMethodLabel, timestampLabel);
        mainContentBox.setAlignment(Pos.CENTER_LEFT);

        HBox buttonBox = new HBox(10, viewInvoiceButton, cancelOrderButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPrefWidth(220);

        HBox orderRow = new HBox();
        orderRow.getStyleClass().add("order-row");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        orderRow.getChildren().addAll(mainContentBox, spacer, buttonBox);
        orderRow.setAlignment(Pos.CENTER);
        orderRow.setPadding(new Insets(5));

        paymentHistoryVBox.getChildren().add(orderRow);
    }

    private void viewInvoice(String orderId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/invoice.fxml"));
            Parent root = loader.load();
            InvoiceController invoiceController = loader.getController();
            invoiceController.setOrderId(orderId);
            invoiceController.setUserId(userId);
            invoiceController.loadInvoiceDetails();

            Stage stage = new Stage();
            Scene scene = new Scene(root);
            scene.getProperties().put("userId", userId);
            stage.setScene(scene);
            stage.setTitle("Hóa đơn");
            stage.setResizable(false);
            stage.getIcons().add(new Image("/assets/img/LOGO.png"));
            stage.show();

            // Close the current stage (optional, depending on your flow)
            // Stage currentStage = (Stage) paymentHistoryVBox.getScene().getWindow();
            // currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể mở trang hóa đơn: " + e.getMessage());
        }
    }

    private void cancelOrder(OrderSummary orderSummary) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Xác nhận hủy đơn");
        confirmation.setHeaderText("Bạn có chắc chắn muốn hủy đơn hàng " + orderSummary.orderId + "?");
        confirmation.setContentText("Hành động này không thể hoàn tác và sẽ trừ điểm tích lũy tương ứng.");

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            Connection conn = Database.connectDB();
            try {
                conn.setAutoCommit(false);

                // Update order status to 'canceled'
                String updateOrderSql = "UPDATE don_hang SET status = 'canceled' WHERE ma_don_hang = ? AND user_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updateOrderSql)) {
                    stmt.setString(1, orderSummary.orderId);
                    stmt.setInt(2, userId);
                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected == 0) {
                        throw new SQLException("Không thể hủy đơn hàng: Đơn hàng không tồn tại hoặc không thuộc về người dùng này.");
                    }
                }

                // Restore stock quantities
                String selectItemsSql = "SELECT ma_san_pham, so_luong FROM chi_tiet_don_hang WHERE ma_don_hang = ?";
                String updateStockSql = "UPDATE san_pham SET so_luong = so_luong + ? WHERE ma_san_pham = ?";
                try (PreparedStatement selectStmt = conn.prepareStatement(selectItemsSql);
                     PreparedStatement updateStmt = conn.prepareStatement(updateStockSql)) {
                    selectStmt.setString(1, orderSummary.orderId);
                    ResultSet rs = selectStmt.executeQuery();
                    while (rs.next()) {
                        int productId = rs.getInt("ma_san_pham");
                        int quantity = rs.getInt("so_luong");
                        updateStmt.setInt(1, quantity);
                        updateStmt.setInt(2, productId);
                        updateStmt.addBatch();
                    }
                    updateStmt.executeBatch();
                }

                // Deduct points
                int pointsToDeduct = (int) (orderSummary.total * 0.1); // Same calculation as in PaymentProcessingController
                String updatePointsSql = "UPDATE diem SET diem_tich_luy = diem_tich_luy - ?, ngay_cap_nhat = CURRENT_TIMESTAMP WHERE user_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updatePointsSql)) {
                    stmt.setInt(1, pointsToDeduct);
                    stmt.setInt(2, userId);
                    stmt.executeUpdate();
                }

                conn.commit();
                showAlert("Thành công", "Đơn hàng " + orderSummary.orderId + " đã được hủy thành công. Điểm đã được trừ: " + pointsToDeduct);
                loadOrdersFromDB(); // Refresh the payment history
            } catch (SQLException e) {
                try {
                    conn.rollback();
                    showAlert("Lỗi", "Lỗi hủy đơn hàng: " + e.getMessage());
                } catch (SQLException ex) {
                    showAlert("Lỗi", "Lỗi rollback: " + ex.getMessage());
                }
            } finally {
                Database.closeConnection(conn);
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}