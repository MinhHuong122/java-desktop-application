package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import database.Database;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class HomeQLController {
    @FXML private Label totalCustomersLabel;
    @FXML private Label totalProductsLabel;
    @FXML private Label totalRevenueLabel;
    @FXML private AreaChart<String, Number> salesChart;

    private int userId;

    @FXML
    public void initialize() {
        loadHomeData();
    }

    public void setUserId(int userId) {
        this.userId = userId;
        loadHomeData();
    }

    void loadHomeData() {
        System.out.println("loadHomeData called");
        Connection conn = Database.connectDB();
        if (conn == null) {
            showError("Không thể kết nối tới cơ sở dữ liệu! Kiểm tra cấu hình kết nối.");
            System.out.println("Database connection failed");
            return;
        }
    
        try {
            // Total customers
            String sql = "SELECT COUNT(*) as total FROM user";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String customers = String.valueOf(rs.getInt("total"));
                    totalCustomersLabel.setText(customers);
                    System.out.println("Total customers: " + customers);
                } else {
                    totalCustomersLabel.setText("0");
                    System.out.println("Total customers: 0");
                }
            }
    
            // Total products sold
            sql = "SELECT COALESCE(SUM(so_luong), 0) as total FROM chi_tiet_don_hang";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String products = String.valueOf(rs.getInt("total"));
                    totalProductsLabel.setText(products);
                    System.out.println("Total products: " + products);
                } else {
                    totalProductsLabel.setText("0");
                    System.out.println("Total products: 0");
                }
            }
    
            // Total revenue
            sql = "SELECT COALESCE(SUM(tong_tien), 0) as total FROM don_hang";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String revenue = String.format("%.0f", rs.getDouble("total"));
                    totalRevenueLabel.setText(revenue);
                    System.out.println("Total revenue: " + revenue);
                } else {
                    totalRevenueLabel.setText("0 VNĐ");
                    System.out.println("Total revenue: 0 VNĐ");
                }
            }
    
            // Sales chart
            salesChart.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Doanh thu (VNĐ)");
            sql = "SELECT DATE_FORMAT(thoi_gian, '%Y-%m') as month, COALESCE(SUM(tong_tien), 0) as total " +
                  "FROM don_hang WHERE thoi_gian >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH) " +
                  "GROUP BY month ORDER BY month";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                int dataCount = 0;
                while (rs.next()) {
                    String month = rs.getString("month");
                    double total = rs.getDouble("total");
                    series.getData().add(new XYChart.Data<>(month, total));
                    dataCount++;
                }
                System.out.println("Sales chart data points: " + dataCount);
            }
            salesChart.getData().add(series);
            System.out.println("Sales chart updated");
    
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Lỗi tải dữ liệu trang chủ: " + e.getMessage());
            System.out.println("SQLException: " + e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    Database.closeConnection(conn);
                    System.out.println("Database connection closed");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                showError("Lỗi khi đóng kết nối: " + ex.getMessage());
            }
        }
    }    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.showAndWait();
    }
}