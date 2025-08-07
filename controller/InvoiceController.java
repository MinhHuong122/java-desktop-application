package controller;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class InvoiceController {
    @FXML private Label titleLabel;
    @FXML private Label orderIdLabel;
    @FXML private Label dateLabel;
    @FXML private Label paymentMethodLabel;
    @FXML private Label customerNameLabel;
    @FXML private Label totalLabel;
    @FXML private VBox itemsVBox; // VBox chứa danh sách sản phẩm

    private String orderId;
    private int userId;

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @FXML
public void initialize() {
    // Tạo tiêu đề cột với dấu "|"
    HBox header = new HBox();
    header.setAlignment(Pos.CENTER_LEFT);
    header.setSpacing(10);
    header.setPadding(new Insets(5));

    Label sttHeader = new Label("STT");
    sttHeader.setPrefWidth(50);
    Label separator1 = new Label("|");
    Label productHeader = new Label("Sản phẩm");
    productHeader.setPrefWidth(150);
    Label separator2 = new Label("|");
    Label quantityHeader = new Label("SL");
    quantityHeader.setPrefWidth(50);
    Label separator3 = new Label("|");
    Label unitPriceHeader = new Label("Đơn giá");
    unitPriceHeader.setPrefWidth(100);
    Label separator4 = new Label("|");
    Label totalPriceHeader = new Label("Thành tiền");
    totalPriceHeader.setPrefWidth(100);

    header.getChildren().addAll(sttHeader, separator1, productHeader, separator2, quantityHeader, separator3, unitPriceHeader, separator4, totalPriceHeader);
    itemsVBox.getChildren().add(header);

    // Thêm dòng phân cách
    Label separator = new Label("----------------------------------------");
    separator.setStyle("-fx-font-size: 14px;");
    itemsVBox.getChildren().add(separator);
}

public void loadInvoiceDetails() {
    Connection conn = Database.connectDB();
    try {
        // Tải thông tin hóa đơn (phần này giữ nguyên)
        String orderSql = "SELECT dh.ma_don_hang, dh.tong_tien, dh.phuong_thuc_thanh_toan, dh.thoi_gian, u.name " +
                         "FROM don_hang dh JOIN user u ON dh.user_id = u.user_id " +
                         "WHERE dh.ma_don_hang = ? AND dh.user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(orderSql)) {
            stmt.setString(1, orderId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                titleLabel.setText("HÓA ĐƠN");
                orderIdLabel.setText("Mã đơn hàng: " + rs.getString("ma_don_hang"));
                dateLabel.setText("Ngày thanh toán: " + rs.getTimestamp("thoi_gian").toLocalDateTime()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                paymentMethodLabel.setText("Phương thức thanh toán: " + rs.getString("phuong_thuc_thanh_toan"));
                customerNameLabel.setText("Khách hàng: " + rs.getString("name"));
                totalLabel.setText("TỔNG TIỀN: " + rs.getDouble("tong_tien") + " VNĐ");
            } else {
                showAlert("Lỗi", "Không tìm thấy đơn hàng!");
                return;
            }
        }

        // Tải danh sách sản phẩm
        String itemsSql = "SELECT ctdh.so_luong, sp.ten, sp.gia " +
                         "FROM chi_tiet_don_hang ctdh JOIN san_pham sp ON ctdh.ma_san_pham = sp.ma_san_pham " +
                         "WHERE ctdh.ma_don_hang = ?";
        try (PreparedStatement stmt = conn.prepareStatement(itemsSql)) {
            stmt.setString(1, orderId);
            ResultSet rs = stmt.executeQuery();
            int stt = 1;
            while (rs.next()) {
                String itemName = rs.getString("ten");
                int quantity = rs.getInt("so_luong");
                double price = rs.getDouble("gia");
                double itemTotal = quantity * price;

                // Tạo HBox cho mỗi dòng sản phẩm với dấu "|"
                HBox row = new HBox();
                row.setAlignment(Pos.CENTER_LEFT);
                row.setSpacing(10);
                row.setPadding(new Insets(5));

                Label sttLabel = new Label(String.valueOf(stt++));
                sttLabel.setPrefWidth(50);
                Label separator1 = new Label("|");
                Label productLabel = new Label(itemName);
                productLabel.setPrefWidth(150);
                Label separator2 = new Label("|");
                Label quantityLabel = new Label(String.valueOf(quantity));
                quantityLabel.setPrefWidth(50);
                Label separator3 = new Label("|");
                Label unitPriceLabel = new Label(String.format("%,.0f", price));
                unitPriceLabel.setPrefWidth(100);
                Label separator4 = new Label("|");
                Label totalPriceLabel = new Label(String.format("%,.0f", itemTotal));
                totalPriceLabel.setPrefWidth(100);

                row.getChildren().addAll(sttLabel, separator1, productLabel, separator2, quantityLabel, separator3, unitPriceLabel, separator4, totalPriceLabel);
                itemsVBox.getChildren().add(row);
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
        showAlert("Lỗi", "Lỗi tải chi tiết hóa đơn: " + e.getMessage());
    } finally {
        Database.closeConnection(conn);
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