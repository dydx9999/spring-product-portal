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

    @Column(name = "vendor_id")
    private Long vendorId;

    @ElementCollection
    @CollectionTable(name = "vendor_products_listed", joinColumns = @JoinColumn(name = "vendor_pk"))
    @OrderColumn(name = "list_position")
    @Column(name = "product_sku")
    private List<String> productsListed;

    @Column(name = "total_sales_count")
    private Integer salesCount;

    @Column(name = "total_sales_revenue")
    private BigDecimal salesRevenue;

    @ElementCollection
    @CollectionTable(name = "vendor_feedback", joinColumns = @JoinColumn(name = "vendor_pk"))
    @MapKeyColumn(name = "feedback_type")
    @Column(name = "feedback_count")
    private Map<String, Integer> totalFeedback = new HashMap<>(Map.of(
            "negative", 0,
            "positive", 0,
            "neutral", 0));

    public Vendor(Long id, Long vendorId, List<String> productsListed, Integer salesCount,
            BigDecimal salesRevenue, Map<String, Integer> totalFeedback) {
        this.id = id;
        this.vendorId = vendorId;
        this.productsListed = productsListed;
        this.salesCount = salesCount;
        this.salesRevenue = salesRevenue;
        this.totalFeedback = totalFeedback;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVendorId() {
        return vendorId;
    }

    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
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

    public Map<String, Integer> getTotalFeedback() {
        return totalFeedback;
    }

    public void setTotalFeedback(Map<String, Integer> totalFeedback) {
        this.totalFeedback = totalFeedback;
    }

    public Vendor() {

    }
}
