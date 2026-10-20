package com.example.demo.dto;

import java.math.BigDecimal;

public class OrderItemDTO {
    private String productName;
    private Long id;
    private int quantity;
    private BigDecimal unitPrice;
    private String vendorBusinessName;

    public OrderItemDTO() {
    }

    public OrderItemDTO(String productName, Long id, int quantity, BigDecimal unitPrice, String vendorBusinessName) {
        this.productName = productName;
        this.id = id;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.vendorBusinessName = vendorBusinessName;
    }

    public OrderItemDTO(String productName, int quantity, BigDecimal unitPrice, String vendorBusinessName) {
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.vendorBusinessName = vendorBusinessName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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