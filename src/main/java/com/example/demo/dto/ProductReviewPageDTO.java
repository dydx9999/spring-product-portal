package com.example.demo.dto;

import java.time.LocalDateTime;

public record ProductReviewPageDTO(String comment, String username, Integer rating, LocalDateTime createdAt) {

}
