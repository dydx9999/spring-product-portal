package com.example.demo.dto;

import java.math.BigDecimal;

public record ProductDTO(Long id, String name, BigDecimal price, String vendorName) {
}