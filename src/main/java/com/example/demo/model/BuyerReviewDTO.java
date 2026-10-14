package com.example.demo.model;

import java.time.LocalDateTime;

public class BuyerReviewDTO {
    private final String productName;
    private final int rating;
    private final LocalDateTime createdAt;

    public BuyerReviewDTO(String productName, int rating, LocalDateTime createdAt) {
        this.productName = productName;
        this.rating = rating;
        this.createdAt = createdAt;
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
