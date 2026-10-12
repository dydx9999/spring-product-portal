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

    public OrderDetailsDTO(UUID orderId, LocalDateTime creationDate, List<OrderItemDTO> items, BigDecimal total) {
        this.orderId = orderId;
        this.creationDate = creationDate;
        this.items = items;
        this.total = total;
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