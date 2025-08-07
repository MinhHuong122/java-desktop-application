package controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import database.Database;
import model.Product;

import java.util.HashMap;
import java.util.Map;

public class SearchResultController {
    @FXML private Label titleLabel;
    @FXML private VBox resultBox;

    private Map<String, Image> imageCache = new HashMap<>();
    private int userId;
    private CartController cartController;
    private boolean isSearching = false; // Biến kiểm tra trạng thái tìm kiếm

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setCartController(CartController cartController) {
        this.cartController = cartController;
    }

    public void displaySearchResults(String query) {
        System.out.println("Bắt đầu displaySearchResults, isSearching = " + isSearching);
        // Kiểm tra nếu đang tìm kiếm thì không thực hiện tìm kiếm mới
        if (isSearching) {
            showAlert(Alert.AlertType.WARNING, "Thông báo", "Đang hiển thị kết quả tìm kiếm. Vui lòng đóng trang kết quả trước khi tìm kiếm tiếp!");
            return;
        }
    
        isSearching = true; // Đặt trạng thái đang tìm kiếm
        titleLabel.setText("Kết quả tìm kiếm cho: " + query);
        resultBox.getChildren().clear();
        imageCache.clear();
    
        resultBox.setAlignment(Pos.CENTER);
    
        Connection conn = Database.connectDB();
        String sql = "SELECT sp.ma_san_pham, sp.ma_loai_san_pham, sp.ten, sp.gia, sp.hinh_anh " +
                     "FROM san_pham sp WHERE sp.ten LIKE ? LIMIT 10";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + query + "%");
            ResultSet rs = stmt.executeQuery();
    
            boolean hasResults = false;
            while (rs.next()) {
                hasResults = true;
                Product product = new Product(
                    rs.getInt("ma_san_pham"),
                    rs.getString("ma_loai_san_pham"),
                    rs.getString("ten"),
                    rs.getDouble("gia"),
                    rs.getString("hinh_anh")
                );
                HBox productBox = createProductBox(product);
                resultBox.getChildren().add(productBox);
            }
    
            if (!hasResults) {
                Label noResultLabel = new Label("Không tìm thấy kết quả!");
                noResultLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: red;");
                resultBox.getChildren().add(noResultLabel);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi tìm kiếm", "Không thể tìm kiếm sản phẩm: " + e.getMessage());
        } finally {
            Database.closeConnection(conn);
        }
    }

    // Thêm phương thức để reset trạng thái khi đóng trang tìm kiếm
    public void resetSearchState() {
        isSearching = false;
        resultBox.getChildren().clear();
        imageCache.clear();
        System.out.println("Đã gọi resetSearchState, isSearching = " + isSearching);
    }

    private HBox createProductBox(Product product) {
        HBox productBox = new HBox(10);
        productBox.setStyle("-fx-padding: 10px; -fx-border-color: lightgray; -fx-border-width: 1px; -fx-border-radius: 5px;");
        productBox.setAlignment(Pos.CENTER);
    
        VBox imageColumn = new VBox();
        imageColumn.setAlignment(Pos.CENTER);
        imageColumn.setPrefWidth(60);
    
        ImageView imageView = new ImageView();
        imageView.setFitHeight(60);
        imageView.setFitWidth(60);
        imageView.setPreserveRatio(true);
    
        String imagePath = "/assets/img/" + product.getImagePath();
    Image image = imageCache.computeIfAbsent(imagePath, k -> {
        try {
            return new Image(getClass().getResourceAsStream(k));
        } catch (Exception e) {
            System.err.println("Lỗi khi tải hình ảnh cho sản phẩm " + product.getName() + ": " + e.getMessage());
            // Trả về một hình ảnh mặc định hoặc null nếu không tải được
            return new Image(getClass().getResourceAsStream("/assets/img/default.png")); // Đảm bảo có hình ảnh mặc định
        }
    });
        imageView.setImage(image);
    
        imageColumn.getChildren().add(imageView);
    
        VBox nameColumn = new VBox();
        nameColumn.setAlignment(Pos.CENTER_LEFT);
        nameColumn.setPrefWidth(150);
    
        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle("-fx-font-size: 14px; -fx-wrap-text: true;");
    
        nameColumn.getChildren().add(nameLabel);
    
        VBox priceAndButtonColumn = new VBox(10);
        priceAndButtonColumn.setAlignment(Pos.CENTER_RIGHT);
        priceAndButtonColumn.setPrefWidth(100);
    
        Label priceLabel = new Label(String.format("%.0f VNĐ", product.getPrice()));
        priceLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
    
        Button addToCartButton = new Button("Đặt hàng");
        addToCartButton.setStyle("-fx-background-color: #3399CC; -fx-text-fill: white; -fx-padding: 5px 10px; -fx-border-radius: 5px;");
        addToCartButton.setOnAction(event -> addToCart(product));
    
        priceAndButtonColumn.getChildren().addAll(priceLabel, addToCartButton);
    
        productBox.getChildren().addAll(imageColumn, nameColumn, priceAndButtonColumn);
    
        return productBox;
    }

    private void addToCart(Product product) {
        if (userId <= 0) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng!");
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
            } else {
                // Nếu sản phẩm chưa tồn tại, thêm mới với số lượng 1
                PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                insertStmt.setInt(1, product.getId());
                insertStmt.setInt(2, 1); // Số lượng mặc định là 1
                insertStmt.setInt(3, userId);
                insertStmt.executeUpdate();
            }
    
            // Cập nhật giao diện ngay lập tức nếu có tham chiếu đến CartController
            if (cartController != null) {
                cartController.addOrUpdateCartItem(product);
            }
    
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể thêm sản phẩm vào giỏ hàng: " + e.getMessage());
        } finally {
            Database.closeConnection(conn);
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}