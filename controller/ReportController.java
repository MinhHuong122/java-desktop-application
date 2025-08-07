package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import database.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReportController {
    @FXML private ListView<String> topProductsList;
    @FXML private Label totalProductsLabel, totalCustomersLabel;

    @FXML
    public void initialize() {
        loadTopProducts();
    }

    public void loadTopProducts() {
        topProductsList.getItems().clear();
        Connection conn = Database.connectDB();
        if (conn == null) {
            showError("Không thể kết nối tới cơ sở dữ liệu!");
            return;
        }

        String sql = "SELECT sp.ten, SUM(ct.so_luong) as total_sold " +
                    "FROM chi_tiet_don_hang ct JOIN san_pham sp ON ct.ma_san_pham = sp.ma_san_pham " +
                    "GROUP BY ct.ma_san_pham, sp.ten " +
                    "ORDER BY total_sold DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                topProductsList.getItems().add(rs.getString("ten") + " - " + rs.getInt("total_sold") + " sản phẩm");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Lỗi tải sản phẩm bán chạy: " + e.getMessage());
        }

        // Tổng sản phẩm đã bán
        sql = "SELECT SUM(so_luong) as total FROM chi_tiet_don_hang";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                totalProductsLabel.setText("Tổng sản phẩm được bán: " + rs.getInt("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Lỗi tải tổng sản phẩm: " + e.getMessage());
        }

        // Tổng khách hàng
        sql = "SELECT COUNT(DISTINCT user_id) as total FROM don_hang";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                totalCustomersLabel.setText("Tổng khách hàng: " + rs.getInt("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Lỗi tải tổng khách hàng: " + e.getMessage());
        } finally {
            Database.closeConnection(conn);
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.showAndWait();
    }
}