package com.example.supplychaintracker;

// A simple model for the data sent from the front-end
public class SupplyChainEvent {
    private String productId;
    private String status;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "SupplyChainEvent{" +
                "productId='" + productId + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
