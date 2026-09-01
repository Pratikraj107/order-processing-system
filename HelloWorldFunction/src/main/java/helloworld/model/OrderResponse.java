package helloworld.model;

public class OrderResponse {

    private String orderId;
    private String customerId;
    private String productId;
    private int quantity;
    private double amount;
    private String status;

    public OrderResponse() {
    }

    public OrderResponse(
            String orderId,
            String customerId,
            String productId,
            int quantity,
            double amount,
            String status) {

        this.orderId = orderId;
        this.customerId = customerId;
        this.productId = productId;
        this.quantity = quantity;
        this.amount = amount;
        this.status = status;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }
}