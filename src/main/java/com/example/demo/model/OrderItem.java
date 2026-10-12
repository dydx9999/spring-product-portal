package com.example.demo.model;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;
    private BigDecimal productPrice;
    private String productName;
    private int productQuantity;

    public OrderItem(Long id, Long orderId, BigDecimal productPrice, String productName, int productQuantity) {
        this.id = id;
        this.orderId = orderId;
        this.productPrice = productPrice;
        this.productName = productName;
        this.productQuantity = productQuantity;
    }

    public BigDecimal getProductPrice() {
        return productPrice;
    }

    public Long getId() {
        return id;
    }

    public void setProductPrice(BigDecimal productPrice) {
        this.productPrice = productPrice;
    }

    public OrderItem() {

    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getProductQuantity() {
        return productQuantity;
    }

    public void setProductQuantity(int productQuantity) {
        this.productQuantity = productQuantity;
    }

}
