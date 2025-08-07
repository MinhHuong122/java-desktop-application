package model;

import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;

public class CartItem {
    private Product product;
    private CheckBox checkBox;
    private Spinner<Integer> quantitySpinner;

    public CartItem(Product product, CheckBox checkBox, Spinner<Integer> quantitySpinner) {
        this.product = product;
        this.checkBox = checkBox;
        this.quantitySpinner = quantitySpinner;
    }

    public Product getProduct() { return product; }
    public CheckBox getCheckBox() { return checkBox; }
    public Spinner<Integer> getQuantitySpinner() { return quantitySpinner; }
    public int getQuantity() { return quantitySpinner.getValue(); }
}