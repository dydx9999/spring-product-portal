package com.example.demo.dto;

public record ShipmentLabelDTO(
        String shipmentId,
        String reference,
        String carrier,
        String service,
        String trackingCode,
        String labelUrl,
        String trackingUrl) {
}
