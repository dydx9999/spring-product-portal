package com.example.demo.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class OrderDetailsDTO {
    private UUID orderId;
    private LocalDateTime creationDate;
    private List<OrderItemDTO> items;
    private BigDecimal total;
    private String status;

    public OrderDetailsDTO(UUID orderId, LocalDateTime creationDate, List<OrderItemDTO> items, BigDecimal total,
            String status) {
        this.orderId = orderId;
        this.creationDate = creationDate;
        this.items = items;
        this.total = total;
        this.status = status;
    }

    public OrderDetailsDTO() {

    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public List<OrderItemDTO> getItems() {
        return items;
    }
}