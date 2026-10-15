package com.example.demo.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    private String productName;
    private Long id;
    private int quantity;
    private BigDecimal unitPrice;
    private String vendorBusinessName;

    public OrderItemDTO(String productName, int quantity, BigDecimal unitPrice, String vendorBusinessName) {
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.vendorBusinessName = vendorBusinessName;
    }

}