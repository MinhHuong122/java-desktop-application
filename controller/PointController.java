package controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import database.Database;

public class PointController {
    private double userPoints;
    private int userId;

    @FXML private Label pointsLabel;
    @FXML private ImageView rankImageView;

    public void setUserId(int userId) {
        this.userId = userId;
        loadUserPoints();
    }



    @FXML
    private void initialize() {
        // Không gọi loadUserPoints trực tiếp, chờ setUserId
    }

    void loadUserPoints() {
        Connection conn = Database.connectDB();
        String sql = "SELECT diem_tich_luy FROM diem WHERE user_id = ?"; // Sửa truy vấn
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                userPoints = rs.getDouble("diem_tich_luy");
                pointsLabel.setText("Điểm hiện có: " + userPoints);
                updateRankImage();
            } else {
                userPoints = 0;
                pointsLabel.setText("Điểm hiện có: " + userPoints);
                // Tạo bản ghi mới nếu chưa có
                String insertSql = "INSERT INTO diem (user_id, diem_tich_luy) VALUES (?, 0)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setInt(1, userId);
                    insertStmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể tải điểm: " + e.getMessage());
        } finally {
            Database.closeConnection(conn);
        }
    }

    @FXML
    private void handleRedeem(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        int cost = Integer.parseInt(clickedButton.getText().replaceAll("[^0-9]", "")); // Lấy số từ text

        if (userPoints >= cost) {
            userPoints -= cost;
            updatePointsInDB(); // Cập nhật vào cơ sở dữ liệu
            pointsLabel.setText("Điểm hiện có: " + userPoints);
            updateRankImage();
            showAlert("Thành công", "Đổi thưởng thành công!");
        } else {
            showAlert("Thông báo", "Không đủ điểm để đổi thưởng!");
        }
    }

    private void updatePointsInDB() {
        Connection conn = Database.connectDB();
        String sql = "UPDATE diem SET diem_tich_luy = ?, ngay_cap_nhat = CURRENT_TIMESTAMP WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, userPoints);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể cập nhật điểm: " + e.getMessage());
        } finally {
            Database.closeConnection(conn);
        }
    }

    private void updateRankImage() {
        String imagePath = "";
        if (userPoints >= 15000) {
            imagePath = "/assets/img/vang.png"; // Sửa đường dẫn
        } else if (userPoints >= 10000) {
            imagePath = "/assets/img/bac.png"; // Sửa đường dẫn
        } else {
            imagePath = "/assets/img/dong.png"; // Sửa đường dẫn
        }

        Image image = new Image(getClass().getResourceAsStream(imagePath));
        if (image.isError()) {
            System.out.println("Lỗi tải hình ảnh: " + imagePath);
            showAlert("Lỗi", "Không thể tải hình ảnh bậc hạng: " + imagePath);
            rankImageView.setImage(null);
        } else {
            rankImageView.setImage(image);
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