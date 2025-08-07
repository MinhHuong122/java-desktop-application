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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class KHhomeController {
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private StackPane centerStackPane;
    @FXML private Button homeButton, pointButton, payButton, cartButton, supportButton, infoButton, btnUser;
    @FXML private ImageView userImage;

    private int userId;
    private Stage searchStage;
    private SearchResultController searchResultController;

    private final Map<String, Parent> pageCache = new HashMap<>();
    private final Map<String, Object> controllerCache = new HashMap<>();

    @FXML
    public void initialize() {
        if (centerStackPane == null) {
            showError("Lỗi: Không tìm thấy centerStackPane trong FXML!");
            return;
        }

        // Thêm logo vào centerStackPane
        ImageView logoView = new ImageView(new Image(getClass().getResourceAsStream("/assets/img/LOGO.png")));
        logoView.setFitHeight(100);
        logoView.setFitWidth(100);
        logoView.setPreserveRatio(true);
        centerStackPane.getChildren().add(logoView);
        StackPane.setAlignment(logoView, Pos.TOP_CENTER);

        userImage.setSmooth(true);

        btnUser.setOnAction(event -> showUserSettings());
        searchButton.setOnAction(event -> handleSearch());
        homeButton.setOnAction(event -> showHomePage());
        pointButton.setOnAction(event -> loadAndShowPage("PointButton.fxml", PointController.class, PointController::loadUserPoints));
        payButton.setOnAction(event -> loadAndShowPage("PayButton.fxml", PayButtonController.class, PayButtonController::loadOrdersFromDB));
        cartButton.setOnAction(event -> loadAndShowPage("CartButton.fxml", CartController.class, controller -> {
            checkCartEmpty();
            controller.loadCartItemsFromDB(userId);
        }));
        supportButton.setOnAction(event -> loadAndShowPage("ContactButton.fxml", null, controller -> {}));
        infoButton.setOnAction(event -> loadAndShowPage("InfoButton.fxml", null, controller -> {}));

        // Tải tên hiển thị từ cơ sở dữ liệu khi khởi tạo
        loadUserDisplayName();
    }

    // Tải tên hiển thị từ cơ sở dữ liệu và đặt cho btnUser
    private void loadUserDisplayName() {
        Connection conn = Database.connectDB();
        String sql = "SELECT name FROM user WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String displayName = rs.getString("name");
                if (displayName != null && !displayName.isEmpty()) {
                    btnUser.setText(displayName);
                } else {
                    btnUser.setText("User"); // Giá trị mặc định nếu không có tên
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
        updateControllersWithUserId();
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

    @SuppressWarnings("unchecked")
    private <T> void loadAndShowPage(String fxmlPath, Class<T> controllerClass, java.util.function.Consumer<T> initializeMethod) {
        try {
            String fullPath = "/view/" + fxmlPath;
            Parent page = loadPage(fullPath, loader -> {
                Object controller = loader.getController();
                if (controller == null) {
                    System.err.println("Controller cho " + fxmlPath + " là null! Kiểm tra khai báo fx:controller trong FXML.");
                    return;
                }
                if (controllerClass != null && controllerClass.isInstance(controller)) {
                    try {
                        Method setUserIdMethod = controllerClass.getMethod("setUserId", int.class);
                        setUserIdMethod.invoke(controller, userId);
                    } catch (NoSuchMethodException e) {
                        System.err.println("Controller " + controllerClass.getSimpleName() + " (cho " + fxmlPath + ") không có phương thức setUserId!");
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        System.err.println("Lỗi khi gọi phương thức setUserId trên controller: " + e.getMessage());
                    }
                } else {
                    System.err.println("Controller cho " + fxmlPath + " không khớp với " + (controllerClass != null ? controllerClass.getSimpleName() : "null"));
                }
            });

            if (centerStackPane != null && page != null) {
                centerStackPane.getChildren().setAll(page);
            } else {
                showError("Lỗi: centerStackPane hoặc trang bị null");
                return;
            }

            if (controllerClass != null) {
                T controller = getController(fxmlPath, controllerClass);
                if (controller != null) {
                    initializeMethod.accept(controller);
                } else {
                    showError("Lỗi: Controller cho " + fxmlPath + " bị null");
                }
            }

            if (centerStackPane.getScene() != null) {
                centerStackPane.getScene().getProperties().put("userId", userId);
                centerStackPane.getScene().setUserData(this);
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

    // New method to retrieve cached page
    public Parent getCachedPage(String fxmlPath) {
        String fullPath = "/view/" + fxmlPath;
        return pageCache.get(fullPath);
    }

    // New method to retrieve cached controller
    public <T> T getCachedController(String fxmlPath, Class<T> controllerClass) {
        String fullPath = "/view/" + fxmlPath;
        Object controller = controllerCache.get(fullPath);
        if (controller != null && controllerClass.isInstance(controller)) {
            return controllerClass.cast(controller);
        }
        return null;
    }

    private void updateControllersWithUserId() {
        CartController cartController = getController("CartButton.fxml", CartController.class);
        if (cartController != null) {
            try {
                cartController.setUserId(userId);
            } catch (Exception e) {
                System.err.println("Lỗi khi gọi setUserId trên CartController: " + e.getMessage());
            }
        }

        PayButtonController payController = getController("PayButton.fxml", PayButtonController.class);
        if (payController != null) {
            try {
                payController.setUserId(userId);
            } catch (Exception e) {
                System.err.println("Lỗi khi gọi setUserId trên PayButtonController: " + e.getMessage());
            }
        }

        PointController pointController = getController("PointButton.fxml", PointController.class);
        if (pointController != null) {
            try {
                pointController.setUserId(userId);
            } catch (Exception e) {
                System.err.println("Lỗi khi gọi setUserId trên PointController: " + e.getMessage());
            }
        }

        HomeController homeController = getController("HomeButton.fxml", HomeController.class);
        if (homeController != null) {
            try {
                homeController.setUserId(userId);
            } catch (Exception e) {
                System.err.println("Lỗi khi gọi setUserId trên HomeController: " + e.getMessage());
            }
        }
    }

    @FXML
    private void showHomePage() {
        loadAndShowPage("HomeButton.fxml", HomeController.class, controller -> {
            controller.setCartController(getController("CartButton.fxml", CartController.class));
            controller.setUserId(userId);
        });
    }

    private void checkCartEmpty() {
        Connection conn = Database.connectDB();
        String sql = "SELECT COUNT(*) FROM gio_hang WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Giỏ hàng của bạn hiện đang trống!", ButtonType.OK);
                alert.showAndWait();
            }
        } catch (SQLException e) {
            showError("Lỗi kiểm tra giỏ hàng: " + e.getMessage());
            e.printStackTrace();
        } finally {
            Database.closeConnection(conn);
        }
    }

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

        TextField addressField = new TextField();
        addressField.setPromptText("Địa chỉ mới");
        addressField.setStyle("-fx-pref-width: 250px; -fx-font-size: 14px; -fx-border-color: #CCCCCC; -fx-border-radius: 5px; -fx-padding: 8px;");

        Button updateAddressButton = new Button("Cập nhật địa chỉ");
        updateAddressButton.setStyle("-fx-pref-width: 150px; -fx-font-size: 14px; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 5px; -fx-padding: 8px;");
        updateAddressButton.setOnAction(e -> updateUserAddress(addressField.getText()));
        updateAddressButton.setDisable(!isUser());

        Button logoutButton = new Button("Đăng xuất");
        logoutButton.setStyle("-fx-pref-width: 150px; -fx-font-size: 14px; -fx-background-color: #FF0000; -fx-text-fill: white; -fx-background-radius: 5px; -fx-padding: 8px; -fx-font-weight: bold;");
        logoutButton.setOnAction(e -> logoutUser(settingsStage));

        settingsPane.getChildren().addAll(
            titleLabel,
            uploadImageButton,
            nameField, updateNameButton,
            passwordField, updatePasswordButton,
            addressField, updateAddressButton,
            logoutButton
        );

        Scene scene = new Scene(settingsPane, 350, 500);
        settingsStage.setScene(scene);
        settingsStage.setTitle("Cài đặt tài khoản");
        settingsStage.setResizable(false);
        settingsStage.show();

        loadUserData(nameField, addressField);
    }

    private void loadUserData(TextField nameField, TextField addressField) {
        Connection conn = Database.connectDB();
        String sql = "SELECT name, address FROM user WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // Không điền dữ liệu vào nameField để trống
                addressField.setText(rs.getString("address"));
            }
        } catch (SQLException e) {
            showError("Lỗi tải thông tin người dùng: " + e.getMessage());
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
        // Cập nhật cột name thay vì email
        String sql = "UPDATE user SET name = ? WHERE user_id = ?";
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
            Image newImage = new Image(file.toURI().toString());
            userImage.setImage(newImage);
            showSuccess("Cập nhật ảnh thành công!");
        }
    }

    private void updatePassword(String newPassword) {
        if (newPassword.isEmpty()) {
            showError("Mật khẩu không được để trống!");
            return;
        }
        Connection conn = Database.connectDB();
        String sql = "UPDATE user SET password = ? WHERE user_id = ?";
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

    private void updateUserAddress(String newAddress) {
        if (!isUser()) {
            showError("Chỉ người dùng (USER) mới có thể sửa địa chỉ!");
            return;
        }
        if (newAddress.isEmpty()) {
            showError("Địa chỉ không được để trống!");
            return;
        }
        Connection conn = Database.connectDB();
        String sql = "UPDATE user SET address = ? WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newAddress);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            showSuccess("Cập nhật địa chỉ thành công!");
        } catch (SQLException e) {
            showError("Lỗi cập nhật địa chỉ: " + e.getMessage());
        } finally {
            Database.closeConnection(conn);
        }
    }

    private boolean isUser() {
        return true;
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

                        if (searchStage != null && searchStage.isShowing()) {
                            searchStage.close();
                            searchStage = null;
                        }

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
    
        if (query.startsWith("DH")) {
            Connection conn = Database.connectDB();
            String sql = "SELECT * FROM don_hang WHERE ma_don_hang = ? AND user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, query);
                stmt.setInt(2, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    loadAndShowPage("PayButton.fxml", PayButtonController.class, PayButtonController::loadOrdersFromDB);
                } else {
                    showError("Không tìm thấy đơn hàng!");
                }
            } catch (SQLException e) {
                showError("Lỗi tìm kiếm đơn hàng: " + e.getMessage());
                e.printStackTrace();
            } finally {
                Database.closeConnection(conn);
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
    
            // Tải trang tìm kiếm mới (không sử dụng cache cho searchResultPage)
            try {
                String searchPath = "/view/SearchResult.fxml";
                // Tạo một Parent mới mỗi lần tìm kiếm
                FXMLLoader loader = new FXMLLoader(getClass().getResource(searchPath));
                if (loader.getLocation() == null) {
                    throw new IOException("Không tìm thấy file FXML tại đường dẫn: " + searchPath);
                }
                Parent searchResultPage = loader.load();
    
                SearchResultController controller = loader.getController();
                if (controller == null) {
                    throw new IllegalStateException("SearchResultController không được khởi tạo!");
                }
                controller.setUserId(userId);
                controller.setCartController(getController("CartButton.fxml", CartController.class));
                controller.displaySearchResults(query);
    
                // Xóa SearchResult.fxml khỏi cache để tránh tái sử dụng
                String fullPath = "/view/SearchResult.fxml";
                pageCache.remove(fullPath);
                controllerCache.remove(fullPath);
    
                // Tạo searchStage mới
                searchStage = new Stage();
                Scene searchScene = new Scene(searchResultPage);
                searchStage.setScene(searchScene);
                searchStage.setTitle("Kết quả tìm kiếm");
                searchStage.setResizable(false);
                try {
                    searchStage.getIcons().add(new Image(getClass().getResourceAsStream("/assets/img/LOGO.png")));
                } catch (Exception e) {
                    System.err.println("Lỗi khi tải icon cho searchStage: " + e.getMessage());
                }
    
                // Đặt lại trạng thái khi đóng cửa sổ
                searchStage.setOnHidden(event -> {
                    if (controller != null) {
                        controller.resetSearchState();
                    }
                    // Xóa SearchResult.fxml khỏi cache khi đóng
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
            }
        }
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

    public void loadAndShowPage(Parent page) {
        if (centerStackPane == null) {
            showError("Lỗi: centerStackPane không được khởi tạo trong FXML!");
            return;
        }
        if (page == null) {
            showError("Lỗi: Trang được truyền vào là null!");
            return;
        }
        centerStackPane.getChildren().setAll(page);
    }
}