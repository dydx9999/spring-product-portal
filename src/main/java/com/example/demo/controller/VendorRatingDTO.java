package com.example.demo.controller;

import java.math.BigDecimal;

public record VendorRatingDTO(
        String vendorName,
        BigDecimal averageRating,
        Long reviewsCount) {
}