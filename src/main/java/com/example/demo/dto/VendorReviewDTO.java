package com.example.demo.dto;

import java.time.LocalDateTime;

public record VendorReviewDTO(String comment, String username, int rating, LocalDateTime createdAt) {

}
