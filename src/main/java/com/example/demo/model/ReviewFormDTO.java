package com.example.demo.model;

import java.util.UUID;

public class ReviewFormDTO {
    private UUID orderId;
    private Long orderItemId;
    private int rating;
    private String comment;

    public ReviewFormDTO(UUID orderId, Long orderItemId, int rating, String comment) {
        this.orderId = orderId;
        this.orderItemId = orderItemId;
        this.rating = rating;
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public ReviewFormDTO() {
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
