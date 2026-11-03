package com.example.demo.dto;

import java.math.BigDecimal;

public record VendorRatingDTO(
                String vendorName,
                BigDecimal averageRating,
                Long reviewsCount) {
}