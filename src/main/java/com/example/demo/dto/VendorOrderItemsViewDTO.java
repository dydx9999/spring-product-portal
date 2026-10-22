package com.example.demo.dto;

import java.math.BigDecimal;

public record VendorOrderItemsViewDTO(BigDecimal productPrice, String productName, int productQuantity,
        BigDecimal total) {

}
