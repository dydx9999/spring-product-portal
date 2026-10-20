package com.example.demo.dto;

import java.time.LocalDateTime;

public class BuyerReviewDTO {
    private final Long productId;
    private final String productName;
    private final int rating;
    private final String comment;
    private final LocalDateTime createdAt;

    public BuyerReviewDTO(Long productId, String productName, int rating, String comment, LocalDateTime createdAt) {
        this.productId = productId;
        this.productName = productName;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public String getComment() {
        return comment;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getRating() {
        return rating;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

}
