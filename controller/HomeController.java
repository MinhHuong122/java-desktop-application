package controller;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import database.Database;
import model.Product;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeController {
    private Map<String, Image> imageCache = new HashMap<>();
    private Map<Button, Product> buttonToProductMap = new HashMap<>();
    private CartController cartController;
    private int userId;

    @FXML private ScrollPane mainScrollPane;
    @FXML private StackPane centerStackPane;
    @FXML private Label categoryLabel, snackLabel, candyLabel, ricepaperLabel, jamLabel, cakeLabel, chocolateLabel, dryLabel,
            drinkLabel, creamLabel, ortherLabel;
    @FXML private HBox snackHBox, candyHBox, ricepaperHBox, jamHBox, cakeHBox, chocolateHBox, dryHBox, drinkHBox, creamHBox, ortherHBox;
    @FXML private HBox featuredHBox;

    @FXML
    public void initialize() {
        loadProducts();
        loadFeaturedProduct();
    }

    private void loadFeaturedProduct() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = Database.connectDB();
            if (conn == null) {
                showAlert("Lỗi", "Không thể kết nối tới cơ sở dữ liệu!");
                return;
            }

            String featuredQuery = "SELECT ma_san_pham, ma_loai_san_pham, ten, gia, hinh_anh " +
                                  "FROM san_pham WHERE noi_bat = 1";
            stmt = conn.prepareStatement(featuredQuery);
            rs = stmt.executeQuery();

            featuredHBox.getChildren().clear();

            while (rs.next()) {
                int id = rs.getInt("ma_san_pham");
                String categoryId = rs.getString("ma_loai_san_pham");
                String name = rs.getString("ten");
                double price = rs.getDouble("gia");
                String imagePath = rs.getString("hinh_anh");

                Product product = new Product(id, categoryId, name, price, imagePath);

                VBox productBox = createFeaturedProductBox(product);
                featuredHBox.getChildren().add(productBox);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể tải sản phẩm nổi bật: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadProducts() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = Database.connectDB();
            if (conn == null) {
                showAlert("Lỗi", "Không thể kết nối tới cơ sở dữ liệu!");
                return;
            }

            String query = "SELECT sp.ma_san_pham, sp.ma_loai_san_pham, sp.ten, sp.gia, sp.hinh_anh " +
                          "FROM san_pham sp JOIN loai_san_pham lsp ON sp.ma_loai_san_pham = lsp.ma_loai_san_pham " +
                          "WHERE lsp.ten_loai = ?";
            stmt = conn.prepareStatement(query);

            loadCategoryProducts("Snack", snackHBox, stmt);
            loadCategoryProducts("Kẹo", candyHBox, stmt);
            loadCategoryProducts("Bánh tráng", ricepaperHBox, stmt);
            loadCategoryProducts("Mứt", jamHBox, stmt);
            loadCategoryProducts("Bánh ngọt", cakeHBox, stmt);
            loadCategoryProducts("Chocolate", chocolateHBox, stmt);
            loadCategoryProducts("Khô", dryHBox, stmt);
            loadCategoryProducts("Nước ngọt", drinkHBox, stmt);
            loadCategoryProducts("Kem", creamHBox, stmt);
            loadCategoryProducts("Khác", ortherHBox, stmt);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể tải sản phẩm: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadCategoryProducts(String categoryName, HBox hbox, PreparedStatement stmt) throws SQLException {
        stmt.setString(1, categoryName);
        ResultSet rs = stmt.executeQuery();

        hbox.getChildren().clear();

        while (rs.next()) {
            int id = rs.getInt("ma_san_pham");
            String categoryId = rs.getString("ma_loai_san_pham");
            String name = rs.getString("ten");
            double price = rs.getDouble("gia");
            String imagePath = rs.getString("hinh_anh");

            Product product = new Product(id, categoryId, name, price, imagePath);

            VBox productBox = createProductBox(product);
            hbox.getChildren().add(productBox);
        }

        rs.close();
    }

    private VBox createProductBox(Product product) {
        VBox productBox = new VBox(5);
        productBox.setAlignment(javafx.geometry.Pos.CENTER);
        productBox.setStyle("-fx-padding: 10px;");

        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Label priceLabel = new Label(String.format("%.0f VNĐ", product.getPrice()));
        priceLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        ImageView imageView = new ImageView();
        imageView.setFitHeight(140);
        imageView.setFitWidth(228);
        imageView.setPreserveRatio(true);
        try {
            Image image = new Image(getClass().getResourceAsStream("/assets/img/" + product.getImagePath()));
            imageView.setImage(image);
        } catch (Exception e) {
            System.out.println("Không thể tải hình ảnh: " + product.getImagePath());
        }

        Button orderButton = new Button("Đặt hàng");
        orderButton.setStyle("-fx-background-color: #3399CC; -fx-text-fill: white;");
        orderButton.setOnAction(this::handleOrderButton);

        buttonToProductMap.put(orderButton, product);

        productBox.getChildren().addAll(imageView, nameLabel, priceLabel, orderButton);
        return productBox;
    }

    private VBox createFeaturedProductBox(Product product) {
        VBox productBox = new VBox(5);
        productBox.setAlignment(javafx.geometry.Pos.CENTER);
        productBox.setStyle("-fx-padding: 10px;");

        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Label priceLabel = new Label(String.format("%.0f VNĐ", product.getPrice()));
        priceLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        ImageView imageView = new ImageView();
        imageView.setFitHeight(140);
        imageView.setFitWidth(228);
        imageView.setPreserveRatio(true);

        String imagePath = "/assets/img/" + product.getImagePath();
        Image image = imageCache.computeIfAbsent(imagePath, k -> new Image(getClass().getResourceAsStream(k)));
        imageView.setImage(image);

        Button orderButton = new Button("Đặt hàng ngay");
        orderButton.setStyle("-fx-background-color: #3399CC; -fx-text-fill: white;");
        orderButton.setOnAction(this::handleOrderButton);

        buttonToProductMap.put(orderButton, product);

        productBox.getChildren().addAll(imageView, nameLabel, priceLabel, orderButton);
        return productBox;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Parent getView() {
        return mainScrollPane;
    }

    public void setCartController(CartController cartController) {
        this.cartController = cartController;
    }

    private void saveToCart(Product product) {
        if (userId <= 0) {
            showAlert("Lỗi", "Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng!");
            return;
        }
    
        Connection conn = Database.connectDB();
        String checkSql = "SELECT so_luong FROM gio_hang WHERE ma_san_pham = ? AND user_id = ?";
        String updateSql = "UPDATE gio_hang SET so_luong = so_luong + 1 WHERE ma_san_pham = ? AND user_id = ?";
        String insertSql = "INSERT INTO gio_hang (ma_san_pham, so_luong, user_id) VALUES (?, ?, ?)";
    
        try {
            // Kiểm tra xem sản phẩm đã tồn tại trong giỏ hàng chưa
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, product.getId());
            checkStmt.setInt(2, userId);
            ResultSet rs = checkStmt.executeQuery();
    
            if (rs.next()) {
                // Nếu sản phẩm đã tồn tại, tăng số lượng lên 1
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setInt(1, product.getId());
                updateStmt.setInt(2, userId);
                updateStmt.executeUpdate();

                showAlert("Thành công", "Đã thêm '" + product.getName() + "' vào giỏ hàng!");
            } else {
                // Nếu sản phẩm chưa tồn tại, thêm mới với số lượng 1
                PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                insertStmt.setInt(1, product.getId());
                insertStmt.setInt(2, 1); // Số lượng mặc định là 1
                insertStmt.setInt(3, userId);
                insertStmt.executeUpdate();
                // Hiển thị thông báo thành công chỉ khi thêm mới
                showAlert("Thành công", "Đã thêm '" + product.getName() + "' vào giỏ hàng!");
            }
    
            // Cập nhật giao diện giỏ hàng
            if (cartController != null) {
                cartController.addOrUpdateCartItem(product);
            }
    
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể thêm sản phẩm vào giỏ hàng: " + e.getMessage());
        } finally {
            Database.closeConnection(conn);
        }
    }

    @FXML
    private void handleOrderButton(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        Product product = buttonToProductMap.get(clickedButton);

        if (product != null) {
            saveToCart(product);
        } else {
            showAlert("Lỗi", "Không thể thêm sản phẩm vào giỏ hàng!");
        }
    }

    @FXML
    private void handleViewMore(ActionEvent event) {
        scrollTo(categoryLabel);
    }

    @FXML
    private void handleScrollToSnack(ActionEvent event) {
        scrollTo(snackLabel);
    }

    @FXML
    private void handleScrollToCandy(ActionEvent event) {
        scrollTo(candyLabel);
    }

    @FXML
    private void handleScrollToRicepaper(ActionEvent event) {
        scrollTo(ricepaperLabel);
    }

    @FXML
    private void handleScrollToJam(ActionEvent event) {
        scrollTo(jamLabel);
    }

    @FXML
    private void handleScrollToCake(ActionEvent event) {
        scrollTo(cakeLabel);
    }

    @FXML
    private void handleScrollToChocolate(ActionEvent event) {
        scrollTo(chocolateLabel);
    }

    @FXML
    private void handleScrollToDry(ActionEvent event) {
        scrollTo(dryLabel);
    }

    @FXML
    private void handleScrollToDrink(ActionEvent event) {
        scrollTo(drinkLabel);
    }

    @FXML
    private void handleScrollToCream(ActionEvent event) {
        scrollTo(creamLabel);
    }

    @FXML
    private void handleScrollToOther(ActionEvent event) {
        scrollTo(ortherLabel);
    }

    private void scrollTo(Label label) {
        if (label != null && mainScrollPane != null) {
            Platform.runLater(() -> {
                mainScrollPane.requestLayout();
                mainScrollPane.layout();

                double labelSceneY = label.localToScene(0, 0).getY();
                double scrollPaneSceneY = mainScrollPane.localToScene(0, 0).getY();
                double labelPosition = labelSceneY - scrollPaneSceneY;

                double contentHeight = mainScrollPane.getContent().getLayoutBounds().getHeight();
                double viewportHeight = mainScrollPane.getViewportBounds().getHeight();

                double scrollableHeight = contentHeight - viewportHeight;
                double scrollValue = 0.0;
                if (scrollableHeight > 0) {
                    scrollValue = Math.min(1.0, Math.max(0.0, labelPosition / contentHeight));
                } else {
                    scrollValue = Math.min(1.0, Math.max(0.0, labelPosition / (contentHeight > 0 ? contentHeight : 1)));
                }

                Timeline timeline = new Timeline(
                    new KeyFrame(Duration.millis(500),
                        new KeyValue(mainScrollPane.vvalueProperty(), scrollValue, Interpolator.EASE_BOTH))
                );
                timeline.play();
            });
        }
    }

    public void searchProducts(String query) {
        Connection conn = Database.connectDB();
        String sql = "SELECT sp.ma_san_pham, sp.ma_loai_san_pham, sp.ten, sp.gia, sp.hinh_anh " +
                     "FROM san_pham sp WHERE sp.ten LIKE ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + query + "%");
            ResultSet rs = stmt.executeQuery();

            snackHBox.getChildren().clear();
            while (rs.next()) {
                Product product = new Product(
                    rs.getInt("ma_san_pham"),
                    rs.getString("ma_loai_san_pham"),
                    rs.getString("ten"),
                    rs.getDouble("gia"),
                    rs.getString("hinh_anh")
                );
                VBox productBox = createProductBox(product);
                snackHBox.getChildren().add(productBox);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể tìm kiếm sản phẩm: " + e.getMessage());
        } finally {
            Database.closeConnection(conn);
        }
    }

    private VBox createSearchProductBox(Product product) {
        VBox productBox = new VBox(5);
        productBox.setAlignment(javafx.geometry.Pos.CENTER);
        productBox.setStyle("-fx-padding: 10px;");

        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Label priceLabel = new Label(String.format("%.0f VNĐ", product.getPrice()));
        priceLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        ImageView imageView = new ImageView();
        imageView.setFitHeight(100); // Giảm kích thước hình ảnh
        imageView.setFitWidth(150);
        imageView.setPreserveRatio(true);

        String imagePath = "/assets/img/" + product.getImagePath();
        Image image = imageCache.computeIfAbsent(imagePath, k -> new Image(getClass().getResourceAsStream(k)));
        imageView.setImage(image);

        Button orderButton = new Button("Đặt hàng");
        orderButton.setStyle("-fx-background-color: #3399CC; -fx-text-fill: white;");
        orderButton.setOnAction(this::handleOrderButton);

        buttonToProductMap.put(orderButton, product);

        productBox.getChildren().addAll(imageView, nameLabel, priceLabel, orderButton);
        return productBox;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void clearImageCache() {
        imageCache.clear();
    }
}