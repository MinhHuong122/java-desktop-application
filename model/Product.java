package model;

public class Product {
    private int id;
    private String categoryId;
    private String name;
    private double price;
    private String imagePath;
    private int quantity;
    private boolean featured;

    public Product(int id, String categoryId, String name, double price, String imagePath) {
        this.id = id;
        this.categoryId = categoryId;
        this.name = name;
        this.price = price;
        this.imagePath = imagePath;
    }

    // Constructor mới cho handleEdit
    public Product(int id, String categoryId, String name, double price, String imagePath, int quantity, boolean featured) {
        this.id = id;
        this.categoryId = categoryId;
        this.name = name;
        this.price = price;
        this.imagePath = imagePath;
        this.quantity = quantity;
        this.featured = featured;
    }

    // Getters và Setters
    public int getId() { return id; }
    public String getCategoryId() { return categoryId; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getImagePath() { return imagePath; }
    public int getQuantity() { return quantity; }
    public boolean isFeatured() { return featured; }

    public void setId(int id) { this.id = id; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public void setName(String name) { this.name = name; }
    public void setPrice(double price) { this.price = price; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setFeatured(boolean featured) { this.featured = featured; }
}