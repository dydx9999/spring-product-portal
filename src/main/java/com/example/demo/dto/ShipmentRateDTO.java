package com.example.demo.dto;

import java.math.BigDecimal;

public record ShipmentRateDTO(
        String id,
        String carrier,
        String service,
        BigDecimal rate,
        String currency,
        Integer estDeliveryDays) {
}
