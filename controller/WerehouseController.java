package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.GridPane;

import util.AlertComponents;
import database.Database;
import model.Product;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class WerehouseController {
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Integer> idColumn, quantityColumn;
    @FXML private TableColumn<Product, String> categoryColumn, nameColumn, imageColumn;
    @FXML private TableColumn<Product, Double> priceColumn;
    @FXML private TableColumn<Product, Boolean> featuredColumn;

    private Set<Integer> existingProductIds;
    private ObservableList<Product> productList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        imageColumn.setCellValueFactory(new PropertyValueFactory<>("imagePath"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        featuredColumn.setCellValueFactory(new PropertyValueFactory<>("featured"));

        loadExistingProductIds();
        loadProducts();
    }

    private void loadExistingProductIds() {
        existingProductIds = new HashSet<>();
        Connection conn = Database.connectDB();
        String sql = "SELECT ma_san_pham FROM san_pham";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                existingProductIds.add(rs.getInt("ma_san_pham"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Lỗi tải danh sách mã sản phẩm: " + e.getMessage());
        } finally {
            Database.closeConnection(conn);
        }
    }

    public void loadProducts() {
        productTable.getItems().clear();
        Connection conn = Database.connectDB();
        String sql = "SELECT * FROM san_pham";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Product product = new Product(
                    rs.getInt("ma_san_pham"),
                    rs.getString("ma_loai_san_pham"),
                    rs.getString("ten"),
                    rs.getDouble("gia"),
                    rs.getString("hinh_anh")
                );
                product.setQuantity(rs.getInt("so_luong"));
                product.setFeatured(rs.getBoolean("noi_bat"));
                productTable.getItems().add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Lỗi tải sản phẩm: " + e.getMessage());
        } finally {
            Database.closeConnection(conn);
        }
    }

    @FXML
    private void exportToExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu file Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fileChooser.setInitialFileName("DanhSachKho.xlsx");
        File file = fileChooser.showSaveDialog(productTable.getScene().getWindow());

        if (file == null) {
            return;
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Danh sách kho");
            
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("DANH SÁCH KHO HÀNG");
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

            Row headerRow = sheet.createRow(2);
            String[] headers = {"Mã sản phẩm", "Mã loại", "Tên sản phẩm", "Giá", "Hình ảnh", "Số lượng", "Nổi bật"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            ObservableList<Product> products = productTable.getItems();
            for (int i = 0; i < products.size(); i++) {
                Product product = products.get(i);
                Row row = sheet.createRow(i + 3);
                row.createCell(0).setCellValue(product.getId());
                row.createCell(1).setCellValue(product.getCategoryId());
                row.createCell(2).setCellValue(product.getName());
                row.createCell(3).setCellValue(product.getPrice());
                row.createCell(4).setCellValue(product.getImagePath());
                row.createCell(5).setCellValue(product.getQuantity());
                row.createCell(6).setCellValue(product.isFeatured() ? "Có" : "Không");
            }

            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleCell.setCellStyle(titleStyle);

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            setBorder(headerStyle);
            for (Cell cell : headerRow) {
                cell.setCellStyle(headerStyle);
            }

            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setAlignment(HorizontalAlignment.CENTER);
            setBorder(dataStyle);

            CellStyle priceStyle = workbook.createCellStyle();
            priceStyle.cloneStyleFrom(dataStyle);
            DataFormat format = workbook.createDataFormat();
            priceStyle.setDataFormat(format.getFormat("#,##0.00"));

            for (int i = 0; i < products.size(); i++) {
                Row row = sheet.getRow(i + 3);
                for (int j = 0; j < 7; j++) {
                    Cell cell = row.getCell(j);
                    if (j == 3) {
                        cell.setCellStyle(priceStyle);
                    } else {
                        cell.setCellStyle(dataStyle);
                    }
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                workbook.write(fileOut);
                AlertComponents.showInformation("Thành công", null, 
                    "Xuất file Excel thành công tại: " + file.getAbsolutePath());
            }

        } catch (IOException e) {
            e.printStackTrace();
            AlertComponents.showError("Lỗi", null, "Không thể xuất file Excel: " + e.getMessage());
        }
    }

    private void setBorder(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }

    @FXML
    private void importFromExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn file Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fileChooser.showOpenDialog(productTable.getScene().getWindow());

        if (file == null) {
            return;
        }

        List<Product> importedProducts = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            rowIterator.next(); // Skip title
            rowIterator.next(); // Skip empty row
            rowIterator.next(); // Skip header

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (isRowEmpty(row)) {
                    break;
                }

                try {
                    Product product = parseRowToProduct(row);
                    if (product != null) {
                        importedProducts.add(product);
                    }
                } catch (Exception e) {
                    AlertComponents.showWarning("Cảnh báo", null, 
                        "Lỗi tại dòng " + (row.getRowNum() + 1) + ": " + e.getMessage());
                }
            }

            if (!importedProducts.isEmpty()) {
                updateDatabaseFromImportedProducts(importedProducts);
                loadProducts();
                AlertComponents.showInformation("Thành công", null,
                    "Đã nhập " + importedProducts.size() + " sản phẩm!");
            } else {
                AlertComponents.showWarning("Cảnh báo", null, 
                    "Không có dữ liệu hợp lệ để nhập!");
            }

        } catch (IOException e) {
            AlertComponents.showError("Lỗi", null, 
                "Không thể đọc file Excel: " + e.getMessage());
        }
    }

    private Product parseRowToProduct(Row row) {
        try {
            Cell idCell = row.getCell(0);
            Cell categoryCell = row.getCell(1);
            Cell nameCell = row.getCell(2);
            Cell priceCell = row.getCell(3);
            Cell imageCell = row.getCell(4);
            Cell quantityCell = row.getCell(5);
            Cell featuredCell = row.getCell(6);

            if (idCell == null || nameCell == null || priceCell == null || quantityCell == null) {
                throw new IllegalArgumentException("Dữ liệu không đầy đủ");
            }

            int id = (int) idCell.getNumericCellValue();
            String categoryId = categoryCell != null ? getCellValueAsString(categoryCell) : "";
            String name = nameCell.getStringCellValue();
            double price = priceCell.getNumericCellValue();
            String imagePath = imageCell != null ? getCellValueAsString(imageCell) : "";
            int quantity = (int) quantityCell.getNumericCellValue();

            if (!categoryId.isEmpty() && !isValidCategory(categoryId)) {
                throw new IllegalArgumentException("Mã loại sản phẩm không hợp lệ: " + categoryId);
            }

            boolean featured = false;
            if (featuredCell != null) {
                String featuredValue = getCellValueAsString(featuredCell).trim();
                if (featuredValue.equalsIgnoreCase("Có")) {
                    featured = true;
                } else if (featuredValue.equalsIgnoreCase("Không")) {
                    featured = false;
                } else {
                    throw new IllegalArgumentException("Giá trị 'Nổi bật' phải là 'Có' hoặc 'Không'");
                }
            }

            if (name.trim().isEmpty()) {
                throw new IllegalArgumentException("Tên sản phẩm không được để trống");
            }
            if (price < 0) {
                throw new IllegalArgumentException("Giá không được âm");
            }
            if (quantity < 0) {
                throw new IllegalArgumentException("Số lượng không được âm");
            }

            return new Product(id, categoryId, name, price, imagePath, quantity, featured);
        } catch (Exception e) {
            throw new IllegalArgumentException("Dữ liệu không hợp lệ: " + e.getMessage());
        }
    }

    private boolean isValidCategory(String categoryId) {
        Connection conn = Database.connectDB();
        if (conn == null) {
            return false;
        }
        String sql = "SELECT COUNT(*) FROM loai_san_pham WHERE ma_loai_san_pham = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, categoryId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            Database.closeConnection(conn);
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue()); // Fixed: Remove (int) cast
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case BLANK:
                return "";
            default:
                return "";
        }
    }

    private void updateDatabaseFromImportedProducts(List<Product> products) {
        Connection conn = Database.connectDB();
        if (conn == null) {
            AlertComponents.showError("Lỗi", null, "Không thể kết nối database");
            return;
        }

        String sql = "INSERT INTO san_pham (ma_san_pham, ma_loai_san_pham, ten, gia, hinh_anh, so_luong, noi_bat) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
                    "ma_loai_san_pham = VALUES(ma_loai_san_pham), ten = VALUES(ten), " +
                    "gia = VALUES(gia), hinh_anh = VALUES(hinh_anh), " +
                    "so_luong = VALUES(so_luong), noi_bat = VALUES(noi_bat)";

        try {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (Product product : products) {
                    stmt.setInt(1, product.getId());
                    stmt.setString(2, product.getCategoryId());
                    stmt.setString(3, product.getName());
                    stmt.setDouble(4, product.getPrice());
                    stmt.setString(5, product.getImagePath());
                    stmt.setInt(6, product.getQuantity());
                    stmt.setBoolean(7, product.isFeatured());
                    stmt.addBatch();
                }
                stmt.executeBatch();
                conn.commit();
            }
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            AlertComponents.showError("Lỗi", null, "Không thể cập nhật database: " + e.getMessage());
        } finally {
            Database.closeConnection(conn);
        }
    }

    private boolean isRowEmpty(Row row) {
        if (row == null)
            return true;
        Cell firstCell = row.getCell(0);
        return firstCell == null || firstCell.getCellType() == CellType.BLANK;
    }


    @FXML
    private void handleDelete() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            showError("Vui lòng chọn một sản phẩm để xóa!");
            return;
        }

        // Hiển thị hộp thoại xác nhận xóa
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận xóa");
        confirmAlert.setHeaderText("Bạn có chắc chắn muốn xóa sản phẩm này?");
        confirmAlert.setContentText("Sản phẩm: " + selectedProduct.getName() + " (ID: " + selectedProduct.getId() + ")");
        confirmAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
        
        Connection conn = Database.connectDB();
        String sql = "DELETE FROM san_pham WHERE ma_san_pham = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, selectedProduct.getId());
            stmt.executeUpdate();
            existingProductIds.remove(selectedProduct.getId());
            loadProducts();
            showInfo("Thành công", "Đã xóa sản phẩm!");
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Lỗi xóa sản phẩm: " + e.getMessage());
        } finally {
            Database.closeConnection(conn);
        }
    }
    });
}

    @FXML
    private void handleEdit() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            showError("Vui lòng chọn một sản phẩm để sửa!");
            return;
        }

        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Sửa sản phẩm");
        dialog.setHeaderText("Cập nhật thông tin sản phẩm");

        TextField idField = new TextField(String.valueOf(selectedProduct.getId()));
        idField.setDisable(true);
        TextField categoryField = new TextField(selectedProduct.getCategoryId());
        TextField nameField = new TextField(selectedProduct.getName());
        TextField priceField = new TextField(String.valueOf(selectedProduct.getPrice()));
        TextField imageField = new TextField(selectedProduct.getImagePath());
        TextField quantityField = new TextField(String.valueOf(selectedProduct.getQuantity()));
        CheckBox featuredCheck = new CheckBox("Nổi bật");
        featuredCheck.setSelected(selectedProduct.isFeatured());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Mã sản phẩm:"), 0, 0);
        grid.add(idField, 1, 0);
        grid.add(new Label("Mã loại sản phẩm:"), 0, 1);
        grid.add(categoryField, 1, 1);
        grid.add(new Label("Tên:"), 0, 2);
        grid.add(nameField, 1, 2);
        grid.add(new Label("Giá:"), 0, 3);
        grid.add(priceField, 1, 3);
        grid.add(new Label("Hình ảnh:"), 0, 4);
        grid.add(imageField, 1, 4);
        grid.add(new Label("Số lượng:"), 0, 5);
        grid.add(quantityField, 1, 5);
        grid.add(new Label("Nổi bật:"), 0, 6);
        grid.add(featuredCheck, 1, 6);

        dialog.getDialogPane().setContent(grid);

        ButtonType saveButton = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == saveButton) {
                try {
                    String name = nameField.getText();
                    if (name.isEmpty()) {
                        showError("Tên sản phẩm không được để trống!");
                        return null;
                    }
                    double price = Double.parseDouble(priceField.getText());
                    if (price < 0) {
                        showError("Giá không được âm!");
                        return null;
                    }
                    int quantity = Integer.parseInt(quantityField.getText());
                    if (quantity < 0) {
                        showError("Số lượng không được âm!");
                        return null;
                    }
                    String categoryId = categoryField.getText();
                    if (!categoryId.isEmpty() && !isValidCategory(categoryId)) {
                        showError("Mã loại sản phẩm không hợp lệ: " + categoryId);
                        return null;
                    }
                    return new Product(
                        selectedProduct.getId(),
                        categoryId,
                        name,
                        price,
                        imageField.getText(),
                        quantity,
                        featuredCheck.isSelected()
                    );
                } catch (NumberFormatException e) {
                    showError("Giá và số lượng phải là số!");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedProduct -> {
            if (updatedProduct != null) {
                Connection conn = Database.connectDB();
                String sql = "UPDATE san_pham SET ma_loai_san_pham = ?, ten = ?, gia = ?, hinh_anh = ?, so_luong = ?, noi_bat = ? WHERE ma_san_pham = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, updatedProduct.getCategoryId());
                    stmt.setString(2, updatedProduct.getName());
                    stmt.setDouble(3, updatedProduct.getPrice());
                    stmt.setString(4, updatedProduct.getImagePath());
                    stmt.setInt(5, updatedProduct.getQuantity());
                    stmt.setBoolean(6, updatedProduct.isFeatured());
                    stmt.setInt(7, updatedProduct.getId());
                    stmt.executeUpdate();
                    loadProducts();
                    showInfo("Thành công", "Đã cập nhật sản phẩm!");
                } catch (SQLException e) {
                    e.printStackTrace();
                    showError("Lỗi cập nhật sản phẩm: " + e.getMessage());
                } finally {
                    Database.closeConnection(conn);
                }
            }
        });
    }

    @FXML
    private void handleAddProduct() {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Thêm sản phẩm");
        dialog.setHeaderText("Nhập thông tin sản phẩm mới");

        // Tạo các trường nhập liệu
        TextField idField = new TextField();
        TextField categoryField = new TextField();
        TextField nameField = new TextField();
        TextField priceField = new TextField();
        TextField imageField = new TextField();
        TextField quantityField = new TextField();
        CheckBox featuredCheck = new CheckBox("Nổi bật");

        // Thiết lập GridPane
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Mã sản phẩm:"), 0, 0);
        grid.add(idField, 1, 0);
        grid.add(new Label("Mã loại sản phẩm:"), 0, 1);
        grid.add(categoryField, 1, 1);
        grid.add(new Label("Tên:"), 0, 2);
        grid.add(nameField, 1, 2);
        grid.add(new Label("Giá:"), 0, 3);
        grid.add(priceField, 1, 3);
        grid.add(new Label("Hình ảnh:"), 0, 4);
        grid.add(imageField, 1, 4);
        grid.add(new Label("Số lượng:"), 0, 5);
        grid.add(quantityField, 1, 5);
        grid.add(new Label("Nổi bật:"), 0, 6);
        grid.add(featuredCheck, 1, 6);

        dialog.getDialogPane().setContent(grid);

        // Thêm nút Lưu và Hủy
        ButtonType saveButton = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        // Xử lý kết quả khi nhấn Lưu
        dialog.setResultConverter(button -> {
            if (button == saveButton) {
                try {
                    int id = Integer.parseInt(idField.getText());
                    if (existingProductIds.contains(id)) {
                        showError("Mã sản phẩm đã tồn tại!");
                        return null;
                    }
                    String categoryId = categoryField.getText();
                    if (!categoryId.isEmpty() && !isValidCategory(categoryId)) {
                        showError("Mã loại sản phẩm không hợp lệ: " + categoryId);
                        return null;
                    }
                    String name = nameField.getText();
                    if (name.isEmpty()) {
                        showError("Tên sản phẩm không được để trống!");
                        return null;
                    }
                    double price = Double.parseDouble(priceField.getText());
                    if (price < 0) {
                        showError("Giá không được âm!");
                        return null;
                    }
                    int quantity = Integer.parseInt(quantityField.getText());
                    if (quantity < 0) {
                        showError("Số lượng không được âm!");
                        return null;
                    }
                    String imagePath = imageField.getText();

                    return new Product(id, categoryId, name, price, imagePath, quantity, featuredCheck.isSelected());
                } catch (NumberFormatException e) {
                    showError("Mã sản phẩm, giá, và số lượng phải là số!");
                    return null;
                }
            }
            return null;
        });

        // Xử lý khi người dùng xác nhận
        dialog.showAndWait().ifPresent(newProduct -> {
            if (newProduct != null) {
                Connection conn = Database.connectDB();
                String sql = "INSERT INTO san_pham (ma_san_pham, ma_loai_san_pham, ten, gia, hinh_anh, so_luong, noi_bat) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, newProduct.getId());
                    stmt.setString(2, newProduct.getCategoryId());
                    stmt.setString(3, newProduct.getName());
                    stmt.setDouble(4, newProduct.getPrice());
                    stmt.setString(5, newProduct.getImagePath());
                    stmt.setInt(6, newProduct.getQuantity());
                    stmt.setBoolean(7, newProduct.isFeatured());
                    stmt.executeUpdate();
                    existingProductIds.add(newProduct.getId());
                    loadProducts();
                    showInfo("Thành công", "Đã thêm sản phẩm mới!");
                } catch (SQLException e) {
                    e.printStackTrace();
                    showError("Lỗi thêm sản phẩm: " + e.getMessage());
                } finally {
                    Database.closeConnection(conn);
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