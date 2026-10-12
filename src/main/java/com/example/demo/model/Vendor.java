package com.example.demo.model;

import jakarta.persistence.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

@Entity
@Table(name = "vendors")
public class Vendor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @ElementCollection
    @CollectionTable(name = "vendor_products_listed", joinColumns = @JoinColumn(name = "vendor_pk"))
    @OrderColumn(name = "list_position")
    @Column(name = "product_sku")
    private List<String> productsListed;

    @Column(name = "total_sales_count")
    private Integer salesCount;

    @Column(name = "total_sales_revenue")
    private BigDecimal salesRevenue;

    @Column(name = "business_name")
    private String businessName;

    public Vendor(Long id, Long userId, List<String> productsListed, Integer salesCount, BigDecimal salesRevenue,
            Map<String, Integer> totalFeedback, String businessName) {
        this.id = id;
        this.userId = userId;
        this.productsListed = productsListed;
        this.salesCount = salesCount;
        this.salesRevenue = salesRevenue;
        this.businessName = businessName;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<String> getProductsListed() {
        return productsListed;
    }

    public void setProductsListed(List<String> productsListed) {
        this.productsListed = productsListed;
    }

    public Integer getSalesCount() {
        return salesCount;
    }

    public void setSalesCount(Integer salesCount) {
        this.salesCount = salesCount;
    }

    public BigDecimal getSalesRevenue() {
        return salesRevenue;
    }

    public void setSalesRevenue(BigDecimal salesRevenue) {
        this.salesRevenue = salesRevenue;
    }

    public Vendor() {

    }
}
