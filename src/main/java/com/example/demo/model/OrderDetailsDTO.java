package com.example.demo.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class OrderDetailsDTO {
    private UUID orderId;
    private LocalDateTime creationDate;
    private List<OrderItemDTO> items;

    public OrderDetailsDTO(UUID orderId, LocalDateTime creationDate, List<OrderItemDTO> items) {
        this.orderId = orderId;
        this.creationDate = creationDate;
        this.items = items;
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