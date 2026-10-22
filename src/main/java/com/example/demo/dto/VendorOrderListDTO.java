package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record VendorOrderListDTO(UUID orderId, Long buyerId, LocalDateTime creationDate, String status) {

}
