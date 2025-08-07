package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import database.Database;
import util.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.IOException;

public class loginController {
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private Hyperlink toggleToRegister;
    @FXML private Hyperlink forgotPasswordLink;
    @FXML private HBox error_email, error_password;
    @FXML private TextField txtPasswordVisible;
    @FXML private ImageView eyeIcon;

    private boolean isPasswordVisible = false;

    // Lưu trữ thông tin người dùng đăng nhập
    private String loggedInEmail;
    private int loggedInUserId;

    @FXML
    public void initialize() {
        hideAllErrors();
        setupPasswordToggle();

        btnLogin.setOnAction(e -> handleLogin());

        toggleToRegister.setOnAction(e -> {
            Stage currentStage = (Stage) toggleToRegister.getScene().getWindow();
            modalUtil.showModal(contantPropertyUtil.FXML_PATH + contantPropertyUtil.FXML_REGISTER, "ĐĂNG KÝ", currentStage);
        });

        forgotPasswordLink.setOnAction(e -> handleForgotPassword());

        txtPassword.textProperty().bindBidirectional(txtPasswordVisible.textProperty());
        eyeIcon.setOnMouseClicked(event -> togglePasswordVisibility());
    }

    private void handleLogin() {
        String sqlAdmin = "SELECT id FROM admin WHERE email = ? AND password = ?";
        String sqlKhachHang = "SELECT user_id, email FROM user WHERE email = ? AND password = ?";

        boolean valid = true;
        valid &= ValidationUtil.isNotEmpty(txtEmail, error_email);
        valid &= ValidationUtil.isValidEmail(txtEmail, error_email);
        valid &= ValidationUtil.isNotEmpty(txtPassword, error_password);

        if (!valid) return;

        try (Connection connect = Database.connectDB();
             PreparedStatement preparedStatement = connect.prepareStatement(sqlAdmin)) {

            preparedStatement.setString(1, txtEmail.getText());
            preparedStatement.setString(2, txtPassword.getText());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    loggedInUserId = resultSet.getInt("id");
                    loggedInEmail = txtEmail.getText();
                    AlertComponents.showInformation("Đăng nhập thành công", null, "Đăng nhập thành công với vai trò Admin!");
                    btnLogin.getScene().getWindow().hide();
                    openQLhomePage(loggedInUserId);
                } else {
                    try (PreparedStatement psKhachHang = connect.prepareStatement(sqlKhachHang)) {
                        psKhachHang.setString(1, txtEmail.getText());
                        psKhachHang.setString(2, txtPassword.getText());
                        try (ResultSet rsKhachHang = psKhachHang.executeQuery()) {
                            if (rsKhachHang.next()) {
                                loggedInUserId = rsKhachHang.getInt("user_id");
                                if (loggedInUserId <= 0) {
                                    throw new SQLException("user_id không hợp lệ từ cơ sở dữ liệu");
                                }
                                loggedInEmail = txtEmail.getText();
                                AlertComponents.showInformation("Đăng nhập thành công", null, "Đăng nhập thành công với vai trò Khách hàng!");
                                btnLogin.getScene().getWindow().hide();
                                openKHHomePage(loggedInUserId);
                            } else {
                                AlertComponents.showError("Đăng nhập thất bại", null, "Sai email hoặc mật khẩu!");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertComponents.showError("Lỗi", null, "Đã xảy ra lỗi: " + e.getMessage());
        }
    }

    private void handleForgotPassword() {
        TextInputDialog emailDialog = new TextInputDialog();
        emailDialog.setTitle("Quên mật khẩu");
        emailDialog.setHeaderText("Nhập email của bạn");
        emailDialog.setContentText("Email:");
    
        emailDialog.showAndWait().ifPresent(email -> {
            if (!ValidationUtil.isValidEmail(email)) {
                AlertComponents.showError("Lỗi", null, "Email không hợp lệ!");
                return;
            }
    
            try (Connection connect = Database.connectDB()) {
                String checkAdminSql = "SELECT id FROM admin WHERE email = ?";
                String checkUserSql = "SELECT user_id FROM user WHERE email = ?";
                boolean emailExists = false;
                int userId = -1;
                final boolean[] isAdmin = {false};
    
                try (PreparedStatement psAdmin = connect.prepareStatement(checkAdminSql)) {
                    psAdmin.setString(1, email);
                    try (ResultSet rsAdmin = psAdmin.executeQuery()) {
                        if (rsAdmin.next()) {
                            emailExists = true;
                            userId = rsAdmin.getInt("id");
                            isAdmin[0] = true;
                        }
                    }
                }
    
                if (!emailExists) {
                    try (PreparedStatement psUser = connect.prepareStatement(checkUserSql)) {
                        psUser.setString(1, email);
                        try (ResultSet rsUser = psUser.executeQuery()) {
                            if (rsUser.next()) {
                                emailExists = true;
                                userId = rsUser.getInt("user_id");
                                isAdmin[0] = false;
                            }
                        }
                    }
                }
    
                if (!emailExists) {
                    AlertComponents.showError("Lỗi", null, "Email không tồn tại trong hệ thống!");
                    return;
                }
    
                // Logic đặt lại mật khẩu
                Dialog<ButtonType> passwordDialog = new Dialog<>();
                passwordDialog.setTitle("Đặt lại mật khẩu");
                passwordDialog.setHeaderText("Nhập mật khẩu mới và xác nhận");
    
                PasswordField newPasswordField = new PasswordField();
                PasswordField confirmPasswordField = new PasswordField();
                VBox content = new VBox(10, new Label("Mật khẩu mới:"), newPasswordField,
                        new Label("Xác nhận mật khẩu:"), confirmPasswordField);
                passwordDialog.getDialogPane().setContent(content);
                passwordDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
    
                Button okButton = (Button) passwordDialog.getDialogPane().lookupButton(ButtonType.OK);
                okButton.setDisable(true);
    
                newPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
                    okButton.setDisable(!ValidationUtil.isValidPassword(newVal) ||
                            !ValidationUtil.isPasswordMatch(newVal, confirmPasswordField.getText()));
                });
                confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
                    okButton.setDisable(!ValidationUtil.isValidPassword(newPasswordField.getText()) ||
                            !ValidationUtil.isPasswordMatch(newPasswordField.getText(), newVal));
                });
    
                passwordDialog.showAndWait().ifPresent(result -> {
                    if (result == ButtonType.OK) {
                        String newPassword = newPasswordField.getText();
                        if (!ValidationUtil.isValidPassword(newPassword) || 
                            !ValidationUtil.isPasswordMatch(newPassword, confirmPasswordField.getText())) {
                            AlertComponents.showError("Lỗi", null, "Mật khẩu không hợp lệ hoặc không khớp!");
                            return;
                        }
    
                        String updateSql = isAdmin[0] ? "UPDATE admin SET password = ? WHERE email = ?" 
                                                     : "UPDATE user SET password = ? WHERE email = ?";
                        // Di chuyển try-with-resources vào trong lambda để xử lý SQLException
                        try (PreparedStatement psUpdate = connect.prepareStatement(updateSql)) {
                            psUpdate.setString(1, newPassword);
                            psUpdate.setString(2, email);
                            int rowsAffected = psUpdate.executeUpdate();
                            if (rowsAffected > 0) {
                                AlertComponents.showInformation("Thành công", null, "Mật khẩu đã được đặt lại!");
                            } else {
                                AlertComponents.showError("Lỗi", null, "Không thể cập nhật mật khẩu!");
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                            AlertComponents.showError("Lỗi", null, "Đã xảy ra lỗi khi cập nhật mật khẩu: " + e.getMessage());
                        }
                    }
                });
            } catch (SQLException e) {
                e.printStackTrace();
                AlertComponents.showError("Lỗi", null, "Đã xảy ra lỗi: " + e.getMessage());
            }
        });
    }

    private void openKHHomePage(int userId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/homePageKH.fxml"));
            Parent root = loader.load();
            KHhomeController khController = loader.getController();
            khController.setUserId(userId);

            Stage newStage = new Stage();
            Scene khScene = new Scene(root);
            khScene.setUserData(khController);
            khScene.getProperties().put("userId", userId);
            newStage.setScene(khScene);
            newStage.setTitle("Tiệm ăn vặt Totoro");
            newStage.setResizable(false);
            newStage.getIcons().add(new Image("/assets/img/LOGO.png"));
            newStage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
            AlertComponents.showError("Lỗi tải giao diện", null, "Không thể mở homePageKH.fxml: " + ex.getMessage());
        }
    }

    private void openQLhomePage(int userId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/homePageQL.fxml"));
            Parent root = loader.load();
            QLhomeController qlController = loader.getController();

            Stage newStage = new Stage();
            Scene scene = new Scene(root);
            newStage.setScene(scene);
            newStage.setTitle("Quản lý Tiệm ăn vặt Totoro");
            newStage.setResizable(false);
            newStage.getIcons().add(new Image("/assets/img/LOGO.png"));

            newStage.show();
            qlController.setUserId(userId);

        } catch (IOException ex) {
            ex.printStackTrace();
            AlertComponents.showError("Lỗi tải giao diện", null, "Không thể mở homePageQL.fxml: " + ex.getMessage());
        }
    }

    private void hideAllErrors() {
        error_email.setVisible(false);
        error_email.setManaged(false);
        error_password.setVisible(false);
        error_password.setManaged(false);
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
            eyeIcon.setImage(new Image(getClass().getResourceAsStream("/assets/img/visible.png")));
        } else {
            txtPasswordVisible.setText(txtPassword.getText());
            txtPasswordVisible.setVisible(true);
            txtPasswordVisible.setManaged(true);
            txtPassword.setVisible(false);
            txtPassword.setManaged(false);
            eyeIcon.setImage(new Image(getClass().getResourceAsStream("/assets/img/hidden.png")));
        }
        isPasswordVisible = !isPasswordVisible;
    }

    public String getLoggedInEmail() {
        return loggedInEmail;
    }

    public int getLoggedInUserId() {
        return loggedInUserId;
    }
}
