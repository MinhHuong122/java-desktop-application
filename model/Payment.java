package model;

import java.time.LocalDateTime;

public class Payment {
    private String orderId;
    private double totalAmount;
    private String paymentMethod;
    private LocalDateTime timestamp;

    public Payment(String orderId, double totalAmount, String paymentMethod) {
        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
        this.timestamp = LocalDateTime.now();
    }

    public String getOrderId() {
        return orderId;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Mã đơn: " + orderId + ", Tổng tiền: " + totalAmount + " VNĐ, Phương thức: " + paymentMethod + ", Thời gian: " + timestamp;
    }
}
