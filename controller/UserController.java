package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserController {
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> idColumn, pointsColumn;
    @FXML private TableColumn<User, String> emailColumn, passwordColumn, updateDateColumn;

    public static class User {
        private final int id;
        private final String email;
        private final String password;
        private final int points;
        private final String updateDate;

        public User(int id, String email, String password, int points, String updateDate) {
            this.id = id;
            this.email = email;
            this.password = password;
            this.points = points;
            this.updateDate = updateDate;
        }

        public int getId() { return id; }
        public String getEmail() { return email; }
        public String getPassword() { return password; }
        public int getPoints() { return points; }
        public String getUpdateDate() { return updateDate; }
    }

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        emailColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEmail()));
        passwordColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPassword()));
        pointsColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getPoints()).asObject());
        updateDateColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getUpdateDate()));

        loadUsers();
    }

    public void loadUsers() {
        userTable.getItems().clear();
        Connection conn = Database.connectDB();
        if (conn == null) {
            showError("Không thể kết nối tới cơ sở dữ liệu! Kiểm tra cấu hình kết nối.");
            return;
        }
    
        String sql = "SELECT u.user_id, u.email, u.password, COALESCE(d.diem_tich_luy, 0) AS diem_tich_luy, d.ngay_cap_nhat " +
                     "FROM user u " +
                     "LEFT JOIN diem d ON u.user_id = d.user_id";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                User user = new User(
                    rs.getInt("user_id"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getInt("diem_tich_luy"),
                    rs.getString("ngay_cap_nhat")
                );
                userTable.getItems().add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Lỗi tải danh sách khách hàng: " + e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    Database.closeConnection(conn);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                showError("Lỗi khi đóng kết nối: " + ex.getMessage());
            }
        }
    }

    @FXML
    private void handleAccessData() {
        loadUsers();
    }

    @FXML
    private void handleAddData() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Thêm khách hàng");
        dialog.setHeaderText("Nhập thông tin khách hàng");

        TextField emailField = new TextField();
        TextField passwordField = new TextField();
        TextField pointsField = new TextField();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Email:"), 0, 0);
        grid.add(emailField, 1, 0);
        grid.add(new Label("Mật khẩu:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(new Label("Điểm:"), 0, 2);
        grid.add(pointsField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        ButtonType saveButton = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == saveButton) {
                try {
                    String email = emailField.getText();
                    String password = passwordField.getText();
                    int points = Integer.parseInt(pointsField.getText());
                    if (email.isEmpty() || password.isEmpty()) {
                        showError("Email và mật khẩu không được để trống!");
                        return null;
                    }
                    return new User(0, email, password, points, null); // ID sẽ tự tăng trong DB
                } catch (NumberFormatException e) {
                    showError("Điểm phải là số!");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newUser -> {
            if (newUser != null) {
                Connection conn = Database.connectDB();
                if (conn == null) {
                    showError("Không thể kết nối tới cơ sở dữ liệu! Kiểm tra cấu hình kết nối.");
                    return;
                }

                try {
                    conn.setAutoCommit(false);

                    String userSql = "INSERT INTO user (email, password) VALUES (?, ?)";
                    try (PreparedStatement userStmt = conn.prepareStatement(userSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                        userStmt.setString(1, newUser.getEmail());
                        userStmt.setString(2, newUser.getPassword());
                        userStmt.executeUpdate();

                        ResultSet generatedKeys = userStmt.getGeneratedKeys();
                        int userId = 0;
                        if (generatedKeys.next()) {
                            userId = generatedKeys.getInt(1);
                        }

                        String diemSql = "INSERT INTO diem (user_id, diem_tich_luy, ngay_cap_nhat) VALUES (?, ?, NOW())";
                        try (PreparedStatement diemStmt = conn.prepareStatement(diemSql)) {
                            diemStmt.setInt(1, userId);
                            diemStmt.setInt(2, newUser.getPoints());
                            diemStmt.executeUpdate();
                        }
                    }

                    conn.commit();
                    loadUsers();
                    showInfo("Thành công", "Đã thêm khách hàng!");
                } catch (SQLException e) {
                    try {
                        if (conn != null) {
                            conn.rollback();
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    e.printStackTrace();
                    showError("Lỗi thêm khách hàng: " + e.getMessage());
                } finally {
                    try {
                        if (conn != null) {
                            conn.setAutoCommit(true);
                            Database.closeConnection(conn);
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        showError("Lỗi khi đóng kết nối: " + ex.getMessage());
                    }
                }
            }
        });
    }

    @FXML
    private void handleDelete() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showError("Vui lòng chọn một khách hàng để xóa!");
            return;
        }

        // Hiển thị hộp thoại xác nhận xóa
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận xóa");
        confirmAlert.setHeaderText("Bạn có chắc chắn muốn xóa khách hàng này?");
        confirmAlert.setContentText("Khách hàng: " + selectedUser.getEmail() + " (ID: " + selectedUser.getId() + ")");
        confirmAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                Connection conn = Database.connectDB();
                if (conn == null) {
                    showError("Không thể kết nối tới cơ sở dữ liệu! Kiểm tra cấu hình kết nối.");
                    return;
                }

                try {
                    conn.setAutoCommit(false);

                    // Xóa dữ liệu liên quan trong bảng diem trước
                    String deleteDiemSql = "DELETE FROM diem WHERE user_id = ?";
                    try (PreparedStatement diemStmt = conn.prepareStatement(deleteDiemSql)) {
                        diemStmt.setInt(1, selectedUser.getId());
                        diemStmt.executeUpdate();
                    }

                    // Xóa dữ liệu trong bảng user
                    String deleteUserSql = "DELETE FROM user WHERE user_id = ?";
                    try (PreparedStatement userStmt = conn.prepareStatement(deleteUserSql)) {
                        userStmt.setInt(1, selectedUser.getId());
                        userStmt.executeUpdate();
                    }

                    conn.commit();
                    loadUsers();
                    showInfo("Thành công", "Đã xóa khách hàng!");
                } catch (SQLException e) {
                    try {
                        conn.rollback();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    e.printStackTrace();
                    showError("Lỗi xóa khách hàng: " + e.getMessage());
                } finally {
                    try {
                        if (conn != null) {
                            conn.setAutoCommit(true);
                            Database.closeConnection(conn);
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        showError("Lỗi khi đóng kết nối: " + ex.getMessage());
                    }
                }
            }
        });
    }

    @FXML
    private void handleEdit() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showError("Vui lòng chọn một khách hàng để sửa!");
            return;
        }

        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Sửa khách hàng");
        dialog.setHeaderText("Cập nhật thông tin khách hàng");

        TextField emailField = new TextField(selectedUser.getEmail());
        TextField passwordField = new TextField(selectedUser.getPassword());
        TextField pointsField = new TextField(String.valueOf(selectedUser.getPoints()));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Email:"), 0, 0);
        grid.add(emailField, 1, 0);
        grid.add(new Label("Mật khẩu:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(new Label("Điểm:"), 0, 2);
        grid.add(pointsField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        ButtonType saveButton = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == saveButton) {
                try {
                    String email = emailField.getText();
                    String password = passwordField.getText();
                    int points = Integer.parseInt(pointsField.getText());
                    if (email.isEmpty() || password.isEmpty()) {
                        showError("Email và mật khẩu không được để trống!");
                        return null;
                    }
                    return new User(selectedUser.getId(), email, password, points, selectedUser.getUpdateDate());
                } catch (NumberFormatException e) {
                    showError("Điểm phải là số!");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedUser -> {
            if (updatedUser != null) {
                Connection conn = Database.connectDB();
                if (conn == null) {
                    showError("Không thể kết nối tới cơ sở dữ liệu! Kiểm tra cấu hình kết nối.");
                    return;
                }

                try {
                    conn.setAutoCommit(false);

                    String userSql = "UPDATE user SET email = ?, password = ? WHERE user_id = ?";
                    try (PreparedStatement userStmt = conn.prepareStatement(userSql)) {
                        userStmt.setString(1, updatedUser.getEmail());
                        userStmt.setString(2, updatedUser.getPassword());
                        userStmt.setInt(3, updatedUser.getId());
                        userStmt.executeUpdate();
                    }

                    String diemSql = "INSERT INTO diem (user_id, diem_tich_luy, ngay_cap_nhat) VALUES (?, ?, NOW()) " +
                                   "ON DUPLICATE KEY UPDATE diem_tich_luy = ?, ngay_cap_nhat = NOW()";
                    try (PreparedStatement diemStmt = conn.prepareStatement(diemSql)) {
                        diemStmt.setInt(1, updatedUser.getId());
                        diemStmt.setInt(2, updatedUser.getPoints());
                        diemStmt.setInt(3, updatedUser.getPoints());
                        diemStmt.executeUpdate();
                    }

                    conn.commit();
                    loadUsers();
                    showInfo("Thành công", "Đã cập nhật khách hàng!");
                } catch (SQLException e) {
                    try {
                        if (conn != null) {
                            conn.rollback();
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    e.printStackTrace();
                    showError("Lỗi cập nhật khách hàng: " + e.getMessage());
                } finally {
                    try {
                        if (conn != null) {
                            conn.setAutoCommit(true);
                            Database.closeConnection(conn);
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        showError("Lỗi khi đóng kết nối: " + ex.getMessage());
                    }
                }
            }
        });
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
    }
}