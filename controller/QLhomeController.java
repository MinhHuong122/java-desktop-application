package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import database.Database;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class QLhomeController {
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private StackPane centerStackPane;
    @FXML private Button homeButton, btnUser, werehouseButton, reportButton, userButton, infoButton, statisticalButton;
    @FXML private ImageView userImage;

    private int userId;
    private Stage searchStage; // Thêm biến để quản lý Stage tìm kiếm
    private final Map<String, Parent> pageCache = new HashMap<>();
    private final Map<String, Object> controllerCache = new HashMap<>();

    @FXML
    public void initialize() {
        if (centerStackPane == null) {
            showError("Lỗi: Không tìm thấy centerStackPane trong FXML!");
            return;
        }

        userImage.setSmooth(true);

        homeButton.setOnAction(event -> showHomePage());
        statisticalButton.setOnAction(event -> loadAndShowPage("StatisticalButton.fxml", StatisticalController.class, controller -> ((StatisticalController) controller).loadOrders()));
        werehouseButton.setOnAction(event -> loadAndShowPage("WerehouseButton.fxml", WerehouseController.class, controller -> ((WerehouseController) controller).loadProducts()));
        reportButton.setOnAction(event -> loadAndShowPage("ReportButton.fxml", ReportController.class, controller -> ((ReportController) controller).loadTopProducts()));
        userButton.setOnAction(event -> loadAndShowPage("UserButton.fxml", UserController.class, controller -> ((UserController) controller).loadUsers()));
        infoButton.setOnAction(event -> loadAndShowPage("InfoButton.fxml", null, controller -> {}));
        btnUser.setOnAction(event -> showUserSettings());
        searchButton.setOnAction(event -> handleSearch());

        try {
            loadPage("/view/HomeButtonQL.fxml", loader -> {});
        } catch (IOException e) {
            showError("Lỗi tải HomePage: " + e.getMessage());
            e.printStackTrace();
        }

        // Tải tên hiển thị từ cơ sở dữ liệu khi khởi tạo
        loadUserDisplayName();
    }

    // Tải tên hiển thị từ cơ sở dữ liệu và đặt cho btnUser
    private void loadUserDisplayName() {
        Connection conn = Database.connectDB();
        String sql = "SELECT name FROM admin WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String displayName = rs.getString("name");
                if (displayName != null && !displayName.isEmpty()) {
                    btnUser.setText(displayName);
                } else {
                    btnUser.setText("Admin"); // Giá trị mặc định nếu không có tên
                }
            }
        } catch (SQLException e) {
            showError("Lỗi tải tên hiển thị: " + e.getMessage());
        } finally {
            Database.closeConnection(conn);
        }
    }

    public void setUserId(int userId) {
        this.userId = userId;
        loadUserDisplayName(); // Tải tên hiển thị khi set userId
        showHomePage();
    }

    private Parent loadPage(String path, java.util.function.Consumer<FXMLLoader> setController) throws IOException {
        if (pageCache.containsKey(path)) {
            return pageCache.get(path);
        }
        FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
        if (loader.getLocation() == null) {
            throw new IOException("Không tìm thấy file FXML tại đường dẫn: " + path);
        }
        Parent page = loader.load();
        setController.accept(loader);
        pageCache.put(path, page);
        controllerCache.put(path, loader.getController());
        return page;
    }

    private <T> void loadAndShowPage(String fxmlPath, Class<T> controllerClass, java.util.function.Consumer<T> initializeMethod) {
        try {
            String fullPath = "/view/" + fxmlPath;
            Parent page = loadPage(fullPath, loader -> {});
            if (page == null) {
                showError("Không thể tải trang: " + fxmlPath + ". Kiểm tra đường dẫn hoặc file FXML.");
                return;
            }
            if (centerStackPane != null) {
                centerStackPane.getChildren().setAll(page);
            } else {
                showError("Lỗi: centerStackPane bị null");
                return;
            }
            if (controllerClass != null) {
                T controller = getController(fxmlPath, controllerClass);
                if (controller != null) {
                    initializeMethod.accept(controller);
                }
            }
        } catch (IOException e) {
            showError("Lỗi tải trang " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getController(String fxmlPath, Class<T> controllerClass) {
        String fullPath = "/view/" + fxmlPath;
        Object controller = controllerCache.get(fullPath);
        if (controller != null && controllerClass.isInstance(controller)) {
            return (T) controller;
        }
        return null;
    }

    @FXML
    private void showHomePage() {
        try {
            String fxmlPath = "/view/HomeButtonQL.fxml";
            Parent homePage = loadPage(fxmlPath, loader -> {});
            centerStackPane.getChildren().setAll(homePage);
            HomeQLController controller = getController("HomeButtonQL.fxml", HomeQLController.class);
            if (controller != null) {
                controller.setUserId(userId);
            }
            if (centerStackPane.getScene() != null) {
                centerStackPane.getScene().getProperties().put("userId", userId);
            }
        } catch (IOException e) {
            showError("Lỗi tải HomeButtonQL.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void showUserSettings() {
        Stage settingsStage = new Stage();
        VBox settingsPane = new VBox(15);
        settingsPane.setPadding(new Insets(20));
        settingsPane.setAlignment(Pos.CENTER);
        settingsStage.getIcons().add(new Image(getClass().getResourceAsStream("/assets/img/LOGO.png")));

        Label titleLabel = new Label("Cài đặt tài khoản");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        Button uploadImageButton = new Button("Tải ảnh mới");
        uploadImageButton.setStyle("-fx-pref-width: 150px; -fx-font-size: 14px; -fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 5px; -fx-padding: 8px;");
        uploadImageButton.setOnAction(e -> uploadUserImage());

        TextField nameField = new TextField();
        nameField.setPromptText("Tên mới");
        nameField.setStyle("-fx-pref-width: 250px; -fx-font-size: 14px; -fx-border-color: #CCCCCC; -fx-border-radius: 5px; -fx-padding: 8px;");

        Button updateNameButton = new Button("Cập nhật tên");
        updateNameButton.setStyle("-fx-pref-width: 150px; -fx-font-size: 14px; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 5px; -fx-padding: 8px;");
        updateNameButton.setOnAction(e -> updateUserName(nameField.getText()));

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mật khẩu mới");
        passwordField.setStyle("-fx-pref-width: 250px; -fx-font-size: 14px; -fx-border-color: #CCCCCC; -fx-border-radius: 5px; -fx-padding: 8px;");

        Button updatePasswordButton = new Button("Cập nhật mật khẩu");
        updatePasswordButton.setStyle("-fx-pref-width: 150px; -fx-font-size: 14px; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 5px; -fx-padding: 8px;");
        updatePasswordButton.setOnAction(e -> updatePassword(passwordField.getText()));

        Button logoutButton = new Button("Đăng xuất");
        logoutButton.setStyle("-fx-pref-width: 150px; -fx-font-size: 14px; -fx-background-color: #FF0000; -fx-text-fill: white; -fx-background-radius: 5px; -fx-padding: 8px; -fx-font-weight: bold;");
        logoutButton.setOnAction(e -> logoutUser(settingsStage));

        settingsPane.getChildren().addAll(
            titleLabel,
            uploadImageButton,
            nameField, updateNameButton,
            passwordField, updatePasswordButton,
            logoutButton
        );

        Scene scene = new Scene(settingsPane, 350, 400); // Giảm chiều cao vì không có phần địa chỉ
        settingsStage.setScene(scene);
        settingsStage.setTitle("Cài đặt tài khoản");
        settingsStage.setResizable(false);
        settingsStage.show();

        loadUserData(nameField);
    }

    private void loadUserData(TextField nameField) {
        Connection conn = Database.connectDB();
        String sql = "SELECT name FROM admin WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // Không điền dữ liệu vào nameField để trống (giống KHhomeController)
            }
        } catch (SQLException e) {
            showError("Lỗi tải thông tin quản lý: " + e.getMessage());
        } finally {
            Database.closeConnection(conn);
        }
    }

    private void updateUserName(String newName) {
        if (newName.isEmpty()) {
            showError("Tên không được để trống!");
            return;
        }
        Connection conn = Database.connectDB();
        String sql = "UPDATE admin SET name = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newName);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            btnUser.setText(newName); // Cập nhật tên hiển thị trên giao diện
            showSuccess("Cập nhật tên thành công!");
        } catch (SQLException e) {
            showError("Lỗi cập nhật tên: " + e.getMessage());
        } finally {
            Database.closeConnection(conn);
        }
    }

    private void uploadUserImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh đại diện");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Hình ảnh", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(new Stage());
        if (file != null) {
            userImage.setImage(new Image(file.toURI().toString()));
            showSuccess("Cập nhật ảnh thành công!");
        }
    }

    private void updatePassword(String newPassword) {
        if (newPassword.isEmpty()) {
            showError("Mật khẩu không được để trống!");
            return;
        }
        Connection conn = Database.connectDB();
        String sql = "UPDATE admin SET password = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newPassword);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            showSuccess("Cập nhật mật khẩu thành công!");
        } catch (SQLException e) {
            showError("Lỗi cập nhật mật khẩu: " + e.getMessage());
        } finally {
            Database.closeConnection(conn);
        }
    }

    private void logoutUser(Stage settingsStage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Bạn có chắc muốn đăng xuất?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                Platform.runLater(() -> {
                    try {
                        Parent root = FXMLLoader.load(getClass().getResource("/view/loginPage.fxml"));
                        Stage loginStage = new Stage();
                        loginStage.setTitle("Đăng nhập");
                        loginStage.setScene(new Scene(root));
                        loginStage.setResizable(false);

                        Stage currentStage = (Stage) btnUser.getScene().getWindow();
                        currentStage.close();

                        settingsStage.close();

                        // Đóng tất cả các cửa sổ khác
                        for (Window stage : Stage.getWindows().filtered(window -> window instanceof Stage)) {
                            if (stage != loginStage) {
                                ((Stage) stage).close();
                            }
                        }

                        loginStage.show();
                    } catch (IOException e) {
                        showError("Lỗi tải trang đăng nhập: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            showHomePage();
            return;
        }
        Connection conn = Database.connectDB();
        if (conn == null) {
            showError("Không thể kết nối tới cơ sở dữ liệu!");
            return;
        }
        try {
            if (query.matches("\\d+")) {
                int searchUserId = Integer.parseInt(query);
                String userSql = "SELECT user_id, email FROM user WHERE user_id = ?";
                try (PreparedStatement userStmt = conn.prepareStatement(userSql)) {
                    userStmt.setInt(1, searchUserId);
                    ResultSet userRs = userStmt.executeQuery();
                    if (userRs.next()) {
                        String customerInfo = String.format("ID: %d, Email: %s", userRs.getInt("user_id"), userRs.getString("email"));
                        showSearchResult("Thông tin khách hàng", customerInfo);
                    } else {
                        showError("Không tìm thấy khách hàng với user_id: " + searchUserId);
                    }
                }
                String orderSql = "SELECT ma_don_hang, thoi_gian, tong_tien, phuong_thuc_thanh_toan, user_id FROM don_hang WHERE user_id = ?";
                try (PreparedStatement orderStmt = conn.prepareStatement(orderSql)) {
                    orderStmt.setInt(1, searchUserId);
                    ResultSet orderRs = orderStmt.executeQuery();
                    StringBuilder orderInfo = new StringBuilder("Danh sách đơn hàng:\n");
                    boolean hasOrders = false;
                    while (orderRs.next()) {
                        hasOrders = true;
                        orderInfo.append(String.format("Mã: %s, Thời gian: %s, Tổng tiền: %.2f VNĐ, Phương thức: %s\n",
                                orderRs.getString("ma_don_hang"), orderRs.getString("thoi_gian"),
                                orderRs.getDouble("tong_tien"), orderRs.getString("phuong_thuc_thanh_toan")));
                    }
                    if (hasOrders) {
                        showSearchResult("Danh sách đơn hàng", orderInfo.toString());
                    } else {
                        showError("Không tìm thấy đơn hàng cho user_id: " + searchUserId);
                    }
                }
            } else if (query.startsWith("DH")) {
                String orderSql = "SELECT ma_don_hang, thoi_gian, tong_tien, phuong_thuc_thanh_toan, user_id FROM don_hang WHERE ma_don_hang = ?";
                try (PreparedStatement stmt = conn.prepareStatement(orderSql)) {
                    stmt.setString(1, query);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        String orderInfo = String.format("Mã: %s, Thời gian: %s, Tổng tiền: %.2f VNĐ, Phương thức: %s, User ID: %d",
                                rs.getString("ma_don_hang"), rs.getString("thoi_gian"),
                                rs.getDouble("tong_tien"), rs.getString("phuong_thuc_thanh_toan"), rs.getInt("user_id"));
                        showSearchResult("Thông tin đơn hàng", orderInfo);
                        loadAndShowPage("WerehouseButton.fxml", WerehouseController.class, controller -> ((WerehouseController) controller).loadProducts());
                    } else {
                        showError("Không tìm thấy đơn hàng với mã: " + query);
                    }
                }
            } else {
                // Kiểm tra trạng thái của searchStage
                if (searchStage != null && searchStage.isShowing()) {
                    showError("Đang hiển thị kết quả tìm kiếm. Vui lòng đóng trang kết quả trước khi tìm kiếm tiếp!");
                    return;
                }

                // Đóng searchStage nếu nó tồn tại và đặt lại thành null
                if (searchStage != null) {
                    try {
                        searchStage.close();
                    } catch (Exception e) {
                        System.err.println("Lỗi khi đóng searchStage cũ: " + e.getMessage());
                    }
                    searchStage = null;
                }

                // Xóa SearchResultQL.fxml khỏi cache trước khi tạo mới
                String fullPath = "/view/SearchResultQL.fxml";
                pageCache.remove(fullPath);
                controllerCache.remove(fullPath);

                // Tải trang tìm kiếm mới
                try {
                    String searchPath = "/view/SearchResultQL.fxml";
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(searchPath));
                    if (loader.getLocation() == null) {
                        throw new IOException("Không tìm thấy file FXML tại đường dẫn: " + searchPath);
                    }
                    Parent searchResultPage = loader.load();

                    SearchResultQLController controller = loader.getController();
                    if (controller == null) {
                        throw new IllegalStateException("SearchResultQLController không được khởi tạo!");
                    }
                    controller.displaySearchResults(query);

                    searchStage = new Stage();
                    Scene searchScene = new Scene(searchResultPage);
                    searchStage.setScene(searchScene);
                    searchStage.setTitle("Kết quả tìm kiếm (Quản lý)");
                    searchStage.setResizable(false);
                    try {
                        searchStage.getIcons().add(new Image(getClass().getResourceAsStream("/assets/img/LOGO.png")));
                    } catch (Exception e) {
                        System.err.println("Lỗi khi tải icon cho searchStage: " + e.getMessage());
                    }

                    // Đặt lại trạng thái khi đóng cửa sổ
                    searchStage.setOnHidden(event -> {
                        if (controller != null) {
                            // Gọi resetSearchState() nếu có (thêm vào SearchResultQLController nếu cần)
                            // controller.resetSearchState();
                        }
                        pageCache.remove(fullPath);
                        controllerCache.remove(fullPath);
                        searchStage = null;
                        System.out.println("searchStage đã được đặt lại thành null sau khi đóng cửa sổ");
                    });

                    searchStage.show();
                    System.out.println("Đã hiển thị searchStage");
                } catch (Exception e) {
                    showError("Lỗi hiển thị kết quả tìm kiếm: " + e.getMessage());
                    e.printStackTrace();
                    if (searchStage != null) {
                        searchStage.close();
                        searchStage = null;
                    }
                    // Đảm bảo xóa cache ngay cả khi có lỗi
                    pageCache.remove(fullPath);
                    controllerCache.remove(fullPath);
                }
            }
        } catch (SQLException e) {
            showError("Lỗi truy vấn cơ sở dữ liệu: " + e.getMessage());
            e.printStackTrace();
        } finally {
            Database.closeConnection(conn);
        }
    }

    private void showSearchResult(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.showAndWait();
    }
}