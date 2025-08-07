package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import database.Database;
import model.CartItem;
import javafx.stage.Screen;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class PaymentProcessingController {
    @FXML private Label orderDetailsLabel;
    @FXML private Button confirmButton;
    @FXML private TextField addressField;
    @FXML private Label addressLabel;
    @FXML private Label firstOrderLabel;
    @FXML private Label savedAddressLabel;
    @FXML private Button editAddressButton;

    private List<CartItem> cartItems;
    private String orderDetails;
    private int userId;
    private boolean isEditingAddress = false;

    public void setOrderDetails(String orderDetails, List<CartItem> cartItems, int userId) {
        this.orderDetailsLabel.setText(orderDetails);
        this.orderDetails = orderDetails;
        this.cartItems = cartItems;
        this.userId = userId;

        if (userId <= 0 && orderDetailsLabel.getScene() != null) {
            Integer sceneUserId = (Integer) orderDetailsLabel.getScene().getProperties().get("userId");
            if (sceneUserId != null && sceneUserId > 0) {
                this.userId = sceneUserId;
            }
        }

        // Kiểm tra userId ngay từ đầu
        if (userId <= 0) {
            return;
        }

        boolean isFirst = isFirstOrder();
        if (isFirst) {
            addressField.setVisible(true);
            if (addressLabel != null) {
                addressLabel.setVisible(true);
                addressLabel.setText("Địa chỉ nhận hàng:");
            }
            if (firstOrderLabel != null) {
                firstOrderLabel.setVisible(true);
                firstOrderLabel.setText("Nhập địa chỉ nhận hàng");
            }
            if (savedAddressLabel != null) {
                savedAddressLabel.setVisible(false);
            }
            if (editAddressButton != null) {
                editAddressButton.setVisible(false);
            }
        } else {
            addressField.setVisible(false);
            if (addressLabel != null) {
                addressLabel.setVisible(true);
                addressLabel.setText("Địa chỉ nhận hàng:");
            }
            if (firstOrderLabel != null) {
                firstOrderLabel.setVisible(false);
                firstOrderLabel.setText("");
            }
            String savedAddress = getSavedAddress();
            if (savedAddressLabel != null) {
                savedAddressLabel.setVisible(true);
                savedAddressLabel.setText(savedAddress != null ? savedAddress : "Chưa có địa chỉ");
            }
            if (editAddressButton != null) {
                editAddressButton.setVisible(true);
            }
        }
        setupLayout();
    }

    private String getSavedAddress() {
        Connection conn = Database.connectDB();
        if (conn == null) {
            showError("Lỗi: Không thể kết nối tới cơ sở dữ liệu!");
            return null;
        }

        String sql = "SELECT address FROM user WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("address");
            }
        } catch (SQLException e) {
            showError("Lỗi lấy địa chỉ: " + e.getMessage());
        } finally {
            Database.closeConnection(conn);
        }
        return null;
    }

    private void setupLayout() {
        if (addressLabel != null && addressField != null && firstOrderLabel != null && savedAddressLabel != null) {
            // Lấy chiều rộng màn hình
            double screenWidth = Screen.getPrimary().getBounds().getWidth();
            // Tính chiều rộng tối đa của addressField (ví dụ: 30% chiều rộng màn hình)
            double maxWidth = screenWidth * 0.3; // 30% chiều rộng màn hình
            double prefWidth = Math.min(maxWidth * 0.8, 250); // Chiều rộng mặc định là 80% của maxWidth, nhưng không vượt quá 250px
    
            // Tạo addressBox để chứa các thành phần liên quan đến địa chỉ
            HBox addressBox = new HBox(15); // Khoảng cách giữa các thành phần là 15px
            addressBox.setAlignment(Pos.CENTER_LEFT);
            addressBox.setStyle("-fx-padding: 15px; -fx-background-color: #f9f9f9; -fx-border-color: #ddd; -fx-border-radius: 5px; -fx-background-radius: 5px;");
    
            if (isFirstOrder() || isEditingAddress) {
                addressBox.getChildren().addAll(addressLabel, addressField);
                if (isEditingAddress) {
                    Button saveAddressButton = new Button("Lưu");
                    saveAddressButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 5px; -fx-padding: 5px 15px; -fx-font-size: 13px;");
                    saveAddressButton.setOnAction(e -> saveEditedAddress());
                    addressBox.getChildren().add(saveAddressButton);
                }
            } else {
                addressBox.getChildren().addAll(addressLabel, savedAddressLabel);
                if (editAddressButton != null) {
                    addressBox.getChildren().add(editAddressButton);
                }
            }
    
            // Tạo confirmButtonBox để chứa nút "Xác nhận"
            HBox confirmButtonBox = new HBox();
            confirmButtonBox.setAlignment(Pos.CENTER);
            confirmButtonBox.setStyle("-fx-padding: 10px 0;");
            confirmButtonBox.getChildren().add(confirmButton);
    
            // Cập nhật phong cách cho confirmButton
            confirmButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 10px 30px; -fx-background-radius: 5px; -fx-cursor: hand;");
            confirmButton.setOnMouseEntered(e -> confirmButton.setStyle("-fx-background-color: #218838; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 10px 30px; -fx-background-radius: 5px; -fx-cursor: hand;"));
            confirmButton.setOnMouseExited(e -> confirmButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 10px 30px; -fx-background-radius: 5px; -fx-cursor: hand;"));
    
            // Lấy rootLayout (VBox chứa toàn bộ giao diện)
            VBox rootLayout = (VBox) orderDetailsLabel.getParent();
            if (rootLayout != null) {
                // Xóa các thành phần cũ trước khi thêm mới
                rootLayout.getChildren().removeIf(node -> node instanceof HBox || node == firstOrderLabel);
    
                // Thêm các thành phần vào rootLayout theo thứ tự mong muốn
                int insertIndex = rootLayout.getChildren().indexOf(orderDetailsLabel) + 1;
    
                // Nếu là đơn hàng đầu tiên, thêm firstOrderLabel trước addressBox
                if (isFirstOrder()) {
                    if (!rootLayout.getChildren().contains(firstOrderLabel)) {
                        rootLayout.getChildren().add(insertIndex, firstOrderLabel);
                    }
                    insertIndex++; // Tăng insertIndex để addressBox được thêm vào sau firstOrderLabel
                }
    
                // Thêm addressBox
                rootLayout.getChildren().add(insertIndex, addressBox);
    
                // Thêm confirmButtonBox ngay sau addressBox
                rootLayout.getChildren().add(insertIndex + 1, confirmButtonBox);
            }
    
            // Cập nhật phong cách cho các thành phần
            addressField.setStyle(
                "-fx-pref-width: " + prefWidth + "px; " +  // Chiều rộng mặc định
                "-fx-max-width: " + maxWidth + "px; " +    // Chiều rộng tối đa dựa trên kích thước màn hình
                "-fx-font-size: 14px; " +
                "-fx-border-color: #ccc; " +
                "-fx-border-radius: 5px; " +
                "-fx-background-radius: 5px; " +
                "-fx-padding: 8px; " +
                "-fx-background-color: #fff; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 1);"
            );
            addressLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");
            firstOrderLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666; -fx-padding: 5 0 5 0;");
            savedAddressLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333; -fx-padding: 2 0 0 0;");
            if (editAddressButton != null) {
                editAddressButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 13px; -fx-background-radius: 5px; -fx-padding: 5px 15px; -fx-cursor: hand; -fx-margin-left: 10px;");
                editAddressButton.setOnMouseEntered(e -> editAddressButton.setStyle("-fx-background-color: #0056b3; -fx-text-fill: white; -fx-font-size: 13px; -fx-background-radius: 5px; -fx-padding: 5px 15px; -fx-cursor: hand; -fx-margin-left: 10px;"));
                editAddressButton.setOnMouseExited(e -> editAddressButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 13px; -fx-background-radius: 5px; -fx-padding: 5px 15px; -fx-cursor: hand; -fx-margin-left: 10px;"));
            }
        }
    }

    @FXML
private void editAddress() {
    isEditingAddress = true;
    addressField.setText(savedAddressLabel.getText());
    addressField.setVisible(true);
    savedAddressLabel.setVisible(false);
    editAddressButton.setVisible(false);

    // Lấy chiều rộng màn hình
    double screenWidth = Screen.getPrimary().getBounds().getWidth();
    // Tính chiều rộng tối đa của addressField (ví dụ: 30% chiều rộng màn hình)
    double maxWidth = screenWidth * 0.3; // 30% chiều rộng màn hình
    double prefWidth = Math.min(maxWidth * 0.8, 250); // Chiều rộng mặc định là 80% của maxWidth, nhưng không vượt quá 250px

    // Đặt giới hạn chiều rộng tối đa cho addressField
    addressField.setStyle(
        "-fx-pref-width: " + prefWidth + "px; " +  // Chiều rộng mặc định
        "-fx-max-width: " + maxWidth + "px; " +    // Chiều rộng tối đa dựa trên kích thước màn hình
        "-fx-font-size: 14px; " +
        "-fx-border-color: #ccc; " +
        "-fx-border-radius: 5px; " +
        "-fx-background-radius: 5px; " +
        "-fx-padding: 8px; " +
        "-fx-background-color: #fff; " +
        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 1);"
    );

    setupLayout();
}

    private void saveEditedAddress() {
        String newAddress = addressField.getText().trim();
        if (newAddress.isEmpty()) {
            showError("Địa chỉ không được để trống!");
            return;
        }
        if (newAddress.length() < 10) {
            showError("Địa chỉ phải có ít nhất 10 ký tự!");
            return;
        }
        if (!newAddress.matches("^[\\p{L}\\p{N}\\s,.]+$")) {
            showError("Địa chỉ chứa ký tự không hợp lệ!");
            return;
        }

        if (userId <= 0) {
            return;
        }

        Connection conn = Database.connectDB();
        if (conn == null) {
            showError("Lỗi: Không thể kết nối tới cơ sở dữ liệu!");
            return;
        }

        String sql = "UPDATE user SET address = ? WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newAddress);
            stmt.setInt(2, userId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                savedAddressLabel.setText(newAddress);
                showSuccess("Cập nhật địa chỉ thành công!");
            } else {
                showError("Không thể cập nhật địa chỉ. userId " + userId + " không tồn tại!");
            }
        } catch (SQLException e) {
            showError("Lỗi cập nhật địa chỉ: " + e.getMessage());
        } finally {
            Database.closeConnection(conn);
        }

        isEditingAddress = false;
        addressField.setVisible(false);
        savedAddressLabel.setVisible(true);
        editAddressButton.setVisible(true);
        setupLayout();
    }

    private boolean isFirstOrder() {
        Connection conn = Database.connectDB();
        if (conn == null) {
            showError("Lỗi: Không thể kết nối tới cơ sở dữ liệu!");
            return true;
        }

        String sql = "SELECT COUNT(*) FROM don_hang WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        } catch (SQLException e) {
            showError("Lỗi kiểm tra đơn hàng: " + e.getMessage());
        } finally {
            Database.closeConnection(conn);
        }
        return true;
    }

    private boolean checkStockAvailability() {
        Connection conn = Database.connectDB();
        if (conn == null) {
            showError("Lỗi: Không thể kết nối tới cơ sở dữ liệu!");
            return false;
        }

        String sql = "SELECT ma_san_pham, so_luong FROM san_pham WHERE ma_san_pham = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (CartItem item : cartItems) {
                if (item.getCheckBox().isSelected()) {
                    stmt.setInt(1, item.getProduct().getId());
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        int availableStock = rs.getInt("so_luong");
                        if (availableStock < item.getQuantity()) {
                            showError("Sản phẩm " + item.getProduct().getName() + " không đủ số lượng trong kho! (Còn: " + availableStock + ")");
                            return false;
                        }
                    } else {
                        showError("Sản phẩm " + item.getProduct().getName() + " không tồn tại trong kho!");
                        return false;
                    }
                }
            }
        } catch (SQLException e) {
            showError("Lỗi kiểm tra số lượng tồn kho: " + e.getMessage());
            return false;
        } finally {
            Database.closeConnection(conn);
        }
        return true;
    }

    private void saveOrder(String orderId, double total, String paymentMethod) {
        if (userId <= 0) {
            showError("Lỗi: userId không hợp lệ (" + userId + "). Vui lòng đăng nhập lại!");
            return;
        }

        if (!checkStockAvailability()) {
            return;
        }

        Connection conn = Database.connectDB();
        if (conn == null) {
            showError("Lỗi: Không thể kết nối tới cơ sở dữ liệu!");
            return;
        }

        String sqlOrder = "INSERT INTO don_hang (ma_don_hang, tong_tien, phuong_thuc_thanh_toan, thoi_gian, user_id) VALUES (?, ?, ?, NOW(), ?)";
        String sqlDetail = "INSERT INTO chi_tiet_don_hang (ma_don_hang, ma_san_pham, so_luong) VALUES (?, ?, ?)";
        String sqlUpdateStock = "UPDATE san_pham SET so_luong = so_luong - ? WHERE ma_san_pham = ?";
        String sqlUpdateAddress = "UPDATE user SET address = ? WHERE user_id = ?";

        try {
            conn.setAutoCommit(false);

            String checkUserSql = "SELECT COUNT(*) FROM user WHERE user_id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkUserSql)) {
                checkStmt.setInt(1, userId);
                ResultSet rs = checkStmt.executeQuery();
                rs.next();
                if (rs.getInt(1) == 0) {
                    throw new SQLException("userId " + userId + " không tồn tại trong bảng user!");
                }
            }

            try (PreparedStatement stmtOrder = conn.prepareStatement(sqlOrder)) {
                stmtOrder.setString(1, orderId);
                stmtOrder.setDouble(2, total);
                stmtOrder.setString(3, paymentMethod);
                stmtOrder.setInt(4, userId);
                stmtOrder.executeUpdate();
            }

            try (PreparedStatement stmtDetail = conn.prepareStatement(sqlDetail);
                 PreparedStatement stmtStock = conn.prepareStatement(sqlUpdateStock)) {
                for (CartItem item : cartItems) {
                    if (item.getCheckBox().isSelected()) {
                        stmtDetail.setString(1, orderId);
                        stmtDetail.setInt(2, item.getProduct().getId());
                        stmtDetail.setInt(3, item.getQuantity());
                        stmtDetail.addBatch();

                        stmtStock.setInt(1, item.getQuantity());
                        stmtStock.setInt(2, item.getProduct().getId());
                        stmtStock.addBatch();
                    }
                }
                int[] detailBatch = stmtDetail.executeBatch();
                int[] stockBatch = stmtStock.executeBatch();

                if (countRowsAffected(detailBatch) == countRowsAffected(stockBatch) && detailBatch.length > 0) {
                    if (isFirstOrder()) {
                        try (PreparedStatement stmtAddress = conn.prepareStatement(sqlUpdateAddress)) {
                            stmtAddress.setString(1, addressField.getText());
                            stmtAddress.setInt(2, userId);
                            stmtAddress.executeUpdate();
                        }
                    }
                    conn.commit();
                    clearCart();
                } else {
                    throw new SQLException("Không đủ sản phẩm trong kho!");
                }
            }
        } catch (SQLException e) {
            try {
                conn.rollback();
                showError("Lỗi lưu đơn hàng: " + e.getMessage());
            } catch (SQLException ex) {
                showError("Lỗi rollback: " + ex.getMessage());
            }
        } finally {
            Database.closeConnection(conn);
        }
    }

    private void clearCart() {
        Connection conn = Database.connectDB();
        if (conn == null) {
            showError("Lỗi: Không thể kết nối tới cơ sở dữ liệu!");
            return;
        }

        String sql = "DELETE FROM gio_hang WHERE user_id = ? AND ma_san_pham = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (CartItem item : cartItems) {
                if (item.getCheckBox().isSelected()) {
                    stmt.setInt(1, userId);
                    stmt.setInt(2, item.getProduct().getId());
                    stmt.addBatch();
                }
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            showError("Lỗi xóa giỏ hàng: " + e.getMessage());
        } finally {
            Database.closeConnection(conn);
        }
    }

    @FXML
    private void confirmPayment() {
        if (userId <= 0) {
            return;
        }

        if (addressField.isVisible()) {
            String address = addressField.getText().trim();
            if (address.isEmpty()) {
                showError("Vui lòng nhập địa chỉ!");
                return;
            }
            if (address.length() < 10) {
                showError("Địa chỉ phải có ít nhất 10 ký tự!");
                return;
            }
            if (!address.matches("^[\\p{L}\\p{N}\\s,.]+$")) {
                showError("Địa chỉ chứa ký tự không hợp lệ!");
                return;
            }
        }

        String orderId = "DH" + System.currentTimeMillis();
        double total = calculateTotal();
        if (total == 0) {
            showError("Không có sản phẩm nào được chọn để thanh toán!");
            return;
        }
        String paymentMethod = orderDetails.split("Phương thức: ")[1].split("\n")[0];
        saveOrder(orderId, total, paymentMethod);

        updatePoints(total);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/homePageKH.fxml"));
            Parent khHomeRoot = loader.load();
            KHhomeController khController = loader.getController();
            khController.setUserId(userId);

            try {
                FXMLLoader payLoader = new FXMLLoader(getClass().getResource("/view/PayButton.fxml"));
                Parent payPage = payLoader.load();
                PayButtonController payController = payLoader.getController();
                payController.setUserId(userId);
                khController.loadAndShowPage(payPage);
            } catch (IOException e) {
                showError("Không thể tải lịch sử thanh toán: " + e.getMessage() + "\nĐang chuyển về trang chủ...");
                // Load the home page as a fallback
                FXMLLoader homeLoader = new FXMLLoader(getClass().getResource("/view/HomeButton.fxml"));
                Parent homePage = homeLoader.load();
                khController.loadAndShowPage(homePage);
            }

            Scene khScene = new Scene(khHomeRoot);
            khScene.setUserData(khController);
            khScene.getProperties().put("userId", userId);

            Stage currentStage = (Stage) confirmButton.getScene().getWindow();
            currentStage.setScene(khScene);
            currentStage.setTitle("Trang chủ");
            currentStage.setResizable(false);
            currentStage.show();
        } catch (IOException e) {
            showError("Không thể tải trang chủ: " + e.getMessage());
        }
    }

    private void updatePoints(double total) {
        int pointsToAdd = (int) (total * 0.1);

        Connection conn = Database.connectDB();
        if (conn == null) {
            showError("Lỗi: Không thể kết nối tới cơ sở dữ liệu!");
            return;
        }

        String sql = "UPDATE diem SET diem_tich_luy = diem_tich_luy + ?, ngay_cap_nhat = CURRENT_TIMESTAMP WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pointsToAdd);
            stmt.setInt(2, userId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                
            }
        } catch (SQLException e) {
            showError("Lỗi cập nhật điểm: " + e.getMessage());
        } finally {
            Database.closeConnection(conn);
        }
    }

    private double calculateTotal() {
        return cartItems.stream()
            .filter(item -> item.getCheckBox().isSelected())
            .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
            .sum();
    }

    private int countRowsAffected(int[] batchResult) {
        return (int) java.util.Arrays.stream(batchResult).filter(result -> result > 0).count();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.showAndWait();
    }
}