package com.example.demo.dto;

public record VendorSalesRankingDTO(
        String businessName,
        Long salesCount,
        Long salesRank) {
}