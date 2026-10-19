package com.example.demo.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "vendors")
public class Vendor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "total_sales_count")
    private Integer salesCount;

    @Column(name = "total_sales_revenue")
    private BigDecimal salesRevenue;

    @Column(name = "business_name")
    private String businessName;

    public Vendor(Long id, Long userId, Integer salesCount, BigDecimal salesRevenue, String businessName) {
        this.id = id;
        this.userId = userId;
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
