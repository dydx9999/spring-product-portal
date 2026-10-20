package com.example.demo.dto;

import java.time.LocalDateTime;

public record VendorPickListDTO(Long id, Long orderId, Long productId, String productName, int quantity,
                LocalDateTime creationDate, String warehouseName) {

}