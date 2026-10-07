package com.example.demo.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class OrderSummaryDTO {

    private UUID orderId;
    private Long buyerId;
    private Long vendorId;
    private LocalDateTime creationDate;

    public OrderSummaryDTO(UUID orderId, Long buyerId, Long vendorId, LocalDateTime creationDate) {
        this.orderId = orderId;
        this.buyerId = buyerId;
        this.vendorId = vendorId;
        this.creationDate = creationDate;
    }

    public OrderSummaryDTO() {

    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    public Long getVendorId() {
        return vendorId;
    }

    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

}
