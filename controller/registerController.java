package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import database.Database;
import util.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class registerController {
    @FXML
    private TextField txtEmail;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private PasswordField txtConfirmPassword;
    @FXML
    private CheckBox userCheck, adminCheck;
    @FXML
    private Button btnRegister;
    @FXML
    private Hyperlink toggleToLogin;
    @FXML
    private HBox error_email, error_password, error_confirm_password;
    @FXML
    private TextField txtPasswordVisible;
    @FXML
    private ImageView eyeIcon;

    private boolean isPasswordVisible = false;

    @FXML
    public void initialize() {
        hideAllErrors();
        setupPasswordToggle();

        // Đồng bộ giá trị giữa txtPassword và txtPasswordVisible
        txtPassword.textProperty().bindBidirectional(txtPasswordVisible.textProperty());

        // Xử lý sự kiện chỉ cho phép chọn 1 CheckBox
        userCheck.setOnAction(e -> {
            if (userCheck.isSelected()) adminCheck.setSelected(false);
        });

        adminCheck.setOnAction(e -> {
            if (adminCheck.isSelected()) userCheck.setSelected(false);
        });

        btnRegister.setOnAction(e -> handleRegister());

        toggleToLogin.setOnAction(e -> {
            Stage currentStage = (Stage) toggleToLogin.getScene().getWindow();
            modalUtil.showModal(contantPropertyUtil.FXML_PATH + contantPropertyUtil.FXML_LOGIN, "ĐĂNG NHẬP", currentStage);
        });
    }

    private void handleRegister() {
        boolean valid = true;

        // Kiểm tra các trường đầu vào
        valid &= ValidationUtil.isNotEmpty(txtEmail, error_email);
        valid &= ValidationUtil.isValidEmail(txtEmail, error_email);
        valid &= ValidationUtil.isValidPassword(txtPassword, error_password);
        valid &= ValidationUtil.isPasswordMatch(txtPassword, txtConfirmPassword, error_confirm_password);

        String email = txtEmail.getText();
        String password = txtPassword.getText();

        // Lấy giá trị từ CheckBox
        String role = null;
        if (userCheck.isSelected()) role = "user"; // Đổi thành "user"
        else if (adminCheck.isSelected()) role = "admin";

        if (role == null) {
            AlertComponents.showError("Đăng ký thất bại", null, "Vui lòng chọn vai trò!");
            return;
        }

        if (valid) {
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                // Kết nối tới cơ sở dữ liệu
                try {
                    conn = Database.connectDB();
                } catch (Exception ex) {
                    AlertComponents.showError("Lỗi kết nối", null, "Không thể kết nối tới cơ sở dữ liệu: " + ex.getMessage());
                    return;
                }

                if (conn == null) {
                    AlertComponents.showError("Lỗi kết nối", null, "Không thể kết nối tới cơ sở dữ liệu!");
                    return;
                }

                // Kiểm tra email đã tồn tại chưa trong bảng tương ứng
                String checkEmailSql = (role.equals("admin"))
                    ? "SELECT email FROM admin WHERE email = ?"
                    : "SELECT email FROM user WHERE email = ?"; // Sử dụng "user"
                stmt = conn.prepareStatement(checkEmailSql);
                stmt.setString(1, email);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    AlertComponents.showError("Đăng ký thất bại", null, "Email đã tồn tại!");
                    return;
                }

                // Lưu mật khẩu trực tiếp (plaintext)
                String plainPassword = password;

                // Thêm user mới vào bảng tương ứng
                String insertSql = (role.equals("admin"))
                    ? "INSERT INTO admin (email, password) VALUES (?, ?)"
                    : "INSERT INTO user (email, password) VALUES (?, ?)"; // Sử dụng "user"
                stmt = conn.prepareStatement(insertSql);
                stmt.setString(1, email);
                stmt.setString(2, plainPassword);

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    AlertComponents.showInformation("Đăng ký thành công", null, "Đăng ký thành công! Vui lòng đăng nhập.");

                    // Chuyển hướng sang trang đăng nhập
                    Stage currentStage = (Stage) btnRegister.getScene().getWindow();
                    modalUtil.showModal(contantPropertyUtil.FXML_PATH + contantPropertyUtil.FXML_LOGIN, "ĐĂNG NHẬP", currentStage);
                } else {
                    AlertComponents.showError("Đăng ký thất bại", null, "Không thể đăng ký. Vui lòng thử lại!");
                }
            } catch (SQLException ex) {
                if (ex.getSQLState().equals("23000")) { // Mã lỗi cho vi phạm UNIQUE
                    AlertComponents.showError("Đăng ký thất bại", null, "Email đã tồn tại trong hệ thống!");
                } else {
                    AlertComponents.showError("Lỗi cơ sở dữ liệu", null, "Lỗi: " + ex.getMessage());
                }
                ex.printStackTrace();
            } catch (Exception ex) {
                AlertComponents.showError("Lỗi", null, "Lỗi không xác định: " + ex.getMessage());
                ex.printStackTrace();
            } finally {
                // Đóng tài nguyên theo thứ tự ngược lại
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    private void setupPasswordToggle() {
        eyeIcon.setOnMouseClicked(event -> togglePasswordVisibility());
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            txtPassword.setText(txtPasswordVisible.getText());
            txtPassword.setVisible(true);
            txtPassword.setManaged(true);
            txtPasswordVisible.setVisible(false);
            txtPasswordVisible.setManaged(false);

            Image visibleImage = new Image(getClass().getResourceAsStream("/assets/img/visible.png")); 
            if (visibleImage != null) {
                eyeIcon.setImage(visibleImage);
            } else {
                System.out.println("Không thể tải hình ảnh: /assets/img/visible.png");
            }
        } else {
            txtPasswordVisible.setText(txtPassword.getText());
            txtPasswordVisible.setVisible(true);
            txtPasswordVisible.setManaged(true);
            txtPassword.setVisible(false);
            txtPassword.setManaged(false);

            Image hiddenImage = new Image(getClass().getResourceAsStream("/assets/img/hidden.png")); 
            if (hiddenImage != null) {
                eyeIcon.setImage(hiddenImage);
            } else {
                System.out.println("Không thể tải hình ảnh: /assets/img/hidden.png");
            }
        }
        isPasswordVisible = !isPasswordVisible;
    }

    private void hideAllErrors() {
        error_email.setVisible(false);
        error_email.setManaged(false);
        error_password.setVisible(false);
        error_password.setManaged(false);
        error_confirm_password.setVisible(false);
        error_confirm_password.setManaged(false);
    }
}