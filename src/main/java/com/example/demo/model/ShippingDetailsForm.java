package com.example.demo.model;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter 
public class ShippingDetailsForm {
    private UUID orderId;
    private String trackingNumber;
    private String carrier; 
    
    
}
