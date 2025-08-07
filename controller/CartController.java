package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import database.Database;
import model.CartItem;
import model.Product;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CartController {
    @FXML private VBox productListVBox;
    @FXML private Label totalLabel;
    @FXML private Button placeOrderButton;
    @FXML private ComboBox<String> paymentMethodCombo;
    @FXML private Button selectAllButton;

    private List<CartItem> cartItems = new ArrayList<>();
    private int userId;


    @FXML
    public void initialize() {
        if (productListVBox == null) {
            System.out.println("Lỗi: productListVBox không được liên kết đúng trong FXML!");
            return;
        }
        if (selectAllButton != null) {
            selectAllButton.setOnAction(event -> toggleSelectAll());
        }

        paymentMethodCombo.setItems(FXCollections.observableArrayList(
            "Thanh toán khi nhận hàng", "Chuyển khoản ngân hàng", "Ví điện tử"
        ));
        paymentMethodCombo.getSelectionModel().selectFirst();
    }


    @FXML
    private void toggleSelectAll() {
        boolean allSelected = cartItems.stream().allMatch(item -> item.getCheckBox().isSelected());
        cartItems.forEach(item -> item.getCheckBox().setSelected(!allSelected));
        updateTotal();
    }

    public void loadCartItemsFromDB(int userId) {
        productListVBox.getChildren().clear();
        cartItems.clear();

        Connection conn = Database.connectDB();
        String sql = "SELECT gh.ma_san_pham, gh.so_luong, sp.ten, sp.gia, sp.hinh_anh " +
                     "FROM gio_hang gh JOIN san_pham sp ON gh.ma_san_pham = sp.ma_san_pham " +
                     "WHERE gh.user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Product product = new Product(
                    rs.getInt("ma_san_pham"),
                    null,
                    rs.getString("ten"),
                    rs.getDouble("gia"),
                    rs.getString("hinh_anh")
                );
                int quantity = rs.getInt("so_luong");
                addProductToCart(product, quantity);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Lỗi tải giỏ hàng: " + e.getMessage(), ButtonType.OK);
            alert.show();
        } finally {
            Database.closeConnection(conn);
        }
        updateTotal();
    }

    public void addOrUpdateCartItem(Product product) {
        for (CartItem existingItem : cartItems) {
            if (existingItem.getProduct().getId() == product.getId()) {
                int currentQuantity = existingItem.getQuantitySpinner().getValue();
                int newQuantity = currentQuantity + 1;
                existingItem.getQuantitySpinner().getValueFactory().setValue(newQuantity);
                updateQuantityInDB(product.getId(), newQuantity);
                updateTotal();
                return;
            }
        }
        addProductToCart(product, 1);
    }

    private void updateQuantityInDB(int productId, int newQuantity) {
        Connection conn = Database.connectDB();
        String sql = "UPDATE gio_hang SET so_luong = ? WHERE ma_san_pham = ? AND user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newQuantity);
            stmt.setInt(2, productId);
            stmt.setInt(3, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Lỗi cập nhật số lượng: " + e.getMessage(), ButtonType.OK);
            alert.show();
        } finally {
            Database.closeConnection(conn);
        }
    }

    

    private void addProductToCart(Product product, int quantity) {
        CheckBox checkBox = new CheckBox();
        
        ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("/assets/img/" + product.getImagePath())));
        imageView.setFitHeight(50);
        imageView.setFitWidth(50);
        imageView.setPreserveRatio(true);
    
        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle("-fx-font-size: 14px;");
        nameLabel.setPrefWidth(150);
    
        Spinner<Integer> quantitySpinner = new Spinner<>(1, 100, quantity);
        quantitySpinner.setPrefWidth(70);
    
        Button deleteButton = new Button("Xóa");
        deleteButton.setPrefWidth(60);
        deleteButton.setPrefHeight(30);
    
        // Lấy số lượng trong kho từ cơ sở dữ liệu
        int stockQuantity = getStockQuantity(product.getId());
        Label stockLabel = new Label("Còn: " + stockQuantity);
        stockLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: gray;");
    
        CartItem cartItem = new CartItem(product, checkBox, quantitySpinner);
        cartItems.add(cartItem);
    
        // Cập nhật tổng tiền khi chọn hoặc thay đổi số lượng
        checkBox.setOnAction(event -> updateTotal());
        quantitySpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateTotal();
            updateStockLabel(stockLabel, stockQuantity, newVal); // Cập nhật label số lượng
            updateQuantityInDB(product.getId(), newVal); // Cập nhật số lượng trong DB
        });
        deleteButton.setOnAction(event -> removeProductFromCart(cartItem));
    
        HBox mainContentBox = new HBox(15, checkBox, imageView, nameLabel, quantitySpinner, stockLabel);
        mainContentBox.setAlignment(Pos.CENTER_LEFT);
    
        HBox deleteButtonBox = new HBox(deleteButton);
        deleteButtonBox.setAlignment(Pos.CENTER);
        deleteButtonBox.setPrefWidth(80);
    
        HBox productRow = new HBox();
        productRow.getStyleClass().add("product-row");
    
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
    
        productRow.getChildren().addAll(mainContentBox, spacer, deleteButtonBox);
        productRow.setAlignment(Pos.CENTER);
        productRow.setPadding(new Insets(5));
    
        productListVBox.getChildren().add(productRow);
    }

    private int getStockQuantity(int productId) {
        Connection conn = Database.connectDB();
        String sql = "SELECT so_luong FROM san_pham WHERE ma_san_pham = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("so_luong");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            Database.closeConnection(conn);
        }
        return 0; // Trả về 0 nếu không tìm thấy
    }

    private void updateStockLabel(Label stockLabel, int stockQuantity, int selectedQuantity) {
        int remaining = stockQuantity - selectedQuantity;
        stockLabel.setText("Còn: " + remaining);
        if (remaining < 0) {
            stockLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: red;");
        } else {
            stockLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: gray;");
        }
    }

private void removeProductFromCart(CartItem cartItem) {
    Connection conn = Database.connectDB();
    String sql = "DELETE FROM gio_hang WHERE ma_san_pham = ? AND user_id = ?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, cartItem.getProduct().getId());
        stmt.setInt(2, userId);
        stmt.executeUpdate();
    } catch (SQLException e) {
        e.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR, "Lỗi xóa sản phẩm: " + e.getMessage(), ButtonType.OK);
        alert.show();
    } finally {
        Database.closeConnection(conn);
    }

    // Làm mới giỏ hàng từ cơ sở dữ liệu
    loadCartItemsFromDB(userId);
}

    public void setCartItems(List<Product> products) {
        productListVBox.getChildren().clear();
        cartItems.clear();
        for (Product product : products) {
            addProductToCart(product, 1);
        }
        updateTotal();
    }

    private void updateTotal() {
        double total = cartItems.stream()
            .filter(item -> item.getCheckBox().isSelected())
            .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
            .sum();
        totalLabel.setText("Tổng tiền: " + total + " VNĐ");
    }

    public void setUserId(int userId) {
        this.userId = userId;
        // System.out.println("CartController: Đã đặt userId = " + userId);
        if (userId > 0) {
            loadCartItemsFromDB(userId);
        } else {
            // Nếu userId không hợp lệ, thử lấy từ Scene
            if (productListVBox.getScene() != null) {
                Integer sceneUserId = (Integer) productListVBox.getScene().getProperties().get("userId");
                if (sceneUserId != null && sceneUserId > 0) {
                    this.userId = sceneUserId;
                    // System.out.println("CartController: Đã cập nhật userId từ Scene = " + this.userId);
                    loadCartItemsFromDB(this.userId);
                } else {
                    productListVBox.getChildren().clear();
                    cartItems.clear();
                    totalLabel.setText("Tổng tiền: 0 VNĐ");
                }
            }
        }
    }

    @FXML
public void placeOrder() {
    if (userId <= 0) {
        Alert alert = new Alert(Alert.AlertType.ERROR, "Lỗi: userId không hợp lệ (" + userId + "). Vui lòng đăng nhập lại!", ButtonType.OK);
        alert.showAndWait();
        return;
    }

    List<String> selectedItems = new ArrayList<>();
    double total = 0;

    for (CartItem item : cartItems) {
        if (item.getCheckBox().isSelected()) {
            total += item.getProduct().getPrice() * item.getQuantity();
            selectedItems.add(item.getProduct().getName() + " x " + item.getQuantity());
        }
    }

    if (selectedItems.isEmpty()) {
        Alert alert = new Alert(Alert.AlertType.WARNING, "Chưa chọn sản phẩm!", ButtonType.OK);
        alert.showAndWait();
    } else {
        String paymentMethod = paymentMethodCombo.getValue();
        String orderId = "DH" + System.currentTimeMillis();
        String orderDetails = "Bạn đã đặt hàng:\n" + String.join("\n", selectedItems) +
                "\n\nMã đơn: " + orderId +
                "\nTổng tiền: " + total + " VNĐ" +
                "\nPhương thức: " + paymentMethod;

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Xác nhận đặt hàng");
        confirmation.setHeaderText("Bạn có chắc chắn muốn đặt hàng?");
        confirmation.setContentText(orderDetails);
        // Đặt icon cho Alert
        Stage alertStage = (Stage) confirmation.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(new Image("/assets/img/LOGO.png"));

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/PaymentProcessing.fxml"));
                Parent root = loader.load();
                PaymentProcessingController processingController = loader.getController();
                processingController.setOrderDetails(orderDetails, cartItems, userId);
                // System.out.println("CartController: Truyền userId = " + userId + " đến PaymentProcessingController");

                Stage stage = new Stage();
                Scene scene = new Scene(root);
                scene.setUserData(this); // Lưu CartController vào userData (tùy chọn)
                scene.getProperties().put("userId", userId); // Lưu userId vào Properties của Scene
                KHhomeController khController = (KHhomeController) placeOrderButton.getScene().getUserData();
                scene.setUserData(khController); // Ưu tiên lưu KHhomeController

                stage.setScene(scene);
                stage.setTitle("Xử lý thanh toán");
                stage.setResizable(false);
                stage.getIcons().add(new Image("/assets/img/LOGO.png"));
                stage.show();

                Stage currentStage = (Stage) placeOrderButton.getScene().getWindow();
                currentStage.close();
            } catch (IOException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR, "Không thể mở trang xử lý thanh toán: " + e.getMessage(), ButtonType.OK);
                alert.showAndWait();
            }
        }
    }
}


    public Parent getView() { 
            return productListVBox; // Hoặc node chính của CartButton.fxml
    }
}