package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class OrderDetailsDTO {
    private UUID orderId;
    private LocalDateTime creationDate;
    private List<OrderItemDTO> items;
    private BigDecimal total;
    private String status;
    private String trackingNumber;
    private String carrier;
    private String paymentMethodType;
    private String cardBrand;
    private String cardLast4;
    private Integer cardExpMonth;
    private Integer cardExpYear;
    private String shipmentId;

    public String getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(String shipmentId) {
        this.shipmentId = shipmentId;
    }

    public String getPaymentMethodType() {
        return paymentMethodType;
    }

    public void setPaymentMethodType(String paymentMethodType) {
        this.paymentMethodType = paymentMethodType;
    }

    public String getCardBrand() {
        return cardBrand;
    }

    public void setCardBrand(String cardBrand) {
        this.cardBrand = cardBrand;
    }

    public String getCardLast4() {
        return cardLast4;
    }

    public void setCardLast4(String cardLast4) {
        this.cardLast4 = cardLast4;
    }

    public Integer getCardExpMonth() {
        return cardExpMonth;
    }

    public void setCardExpMonth(Integer cardExpMonth) {
        this.cardExpMonth = cardExpMonth;
    }

    public Integer getCardExpYear() {
        return cardExpYear;
    }

    public void setCardExpYear(Integer cardExpYear) {
        this.cardExpYear = cardExpYear;
    }

    public OrderDetailsDTO(UUID orderId, LocalDateTime creationDate, List<OrderItemDTO> items, BigDecimal total,
            String status, String trackingNumber, String carrier, String shipmentId) {
        this.orderId = orderId;
        this.creationDate = creationDate;
        this.items = items;
        this.total = total;
        this.status = status;
        this.trackingNumber = trackingNumber;
        this.carrier = carrier;
        this.shipmentId = shipmentId;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public OrderDetailsDTO() {

    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public List<OrderItemDTO> getItems() {
        return items;
    }
}