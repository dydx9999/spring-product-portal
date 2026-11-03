package com.example.demo.dto;

public record PaymentDetails(String paymentIntentId, String paymentMethodType, String cardBrand, String cardLast4,
        Integer cardExpMonth, Integer cardExpYear) {
}
