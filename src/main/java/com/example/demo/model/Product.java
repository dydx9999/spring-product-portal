package com.example.demo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")

public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "product_name", nullable = false)
    private String productName;
    @Column(name = "product_price", nullable = false)
    private BigDecimal productPrice;
    @Column(name = "available_quantity", nullable = false)
    private int productQuantity;

    @Column(name = "product_sku", nullable = false)
    private String productSKU;

    public Product(Long id, String productName, BigDecimal productPrice, int productQuantity, String productSKU) {

        this.id = id;
        this.productName = productName;
        this.productPrice = productPrice;
        this.productQuantity = productQuantity;
        this.productSKU = productSKU;
    }

    public Product() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(BigDecimal productPrice) {
        this.productPrice = productPrice;
    }

    public int getProductQuantity() {
        return productQuantity;
    }

    public void setProductQuantity(int productQuantity) {
        this.productQuantity = productQuantity;
    }

    public String getProductSKU() {
        return productSKU;
    }

    public void setProductSKU(String productSKU) {
        this.productSKU = productSKU;
    }

}
