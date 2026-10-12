package com.example.demo.model;

import java.math.BigDecimal;

public class OrderItemDTO {
    private Long id;
    private String productName;
    private int quantity;
    private BigDecimal unitPrice;
    private String vendorBusinessName;

    public OrderItemDTO(String productName, int quantity, BigDecimal unitPrice) {
        this(productName, quantity, unitPrice, null);
    }

    public OrderItemDTO(String productName, int quantity, BigDecimal unitPrice, String vendorBusinessName) {
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.vendorBusinessName = vendorBusinessName;
    }

    public OrderItemDTO() {

    }

    public String getProductName() {
        return productName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getVendorBusinessName() {
        return vendorBusinessName;
    }

    public void setVendorBusinessName(String vendorBusinessName) {
        this.vendorBusinessName = vendorBusinessName;
    }
}
