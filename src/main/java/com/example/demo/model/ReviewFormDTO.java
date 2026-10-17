package com.example.demo.model;

import java.util.UUID;

public class ReviewFormDTO {
    private UUID orderId;
    private Long orderItemId;
    private int rating;

    public ReviewFormDTO() {
    }

    public ReviewFormDTO(UUID orderId, Long orderItemId, int rating) {
        this.orderId = orderId;
        this.orderItemId = orderItemId;
        this.rating = rating;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public Long getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(Long orderItemId) {
        this.orderItemId = orderItemId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}
