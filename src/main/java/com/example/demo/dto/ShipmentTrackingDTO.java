package com.example.demo.dto;

public record ShipmentTrackingDTO(
        String vendorBusinessName,
        String status,
        String carrier,
        String trackingNumber,
        String trackingUrl) {
}
