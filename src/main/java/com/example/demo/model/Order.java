package com.example.demo.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private UUID orderId;
    private Long buyerId;
    private Long vendorId;
    private LocalDateTime creationDate;
    private String status;
    @Column(name = "tracking_number")
    private String trackingNumber;
    private String carrier;
    private String stripePaymentIntentId;
    private String paymentMethodType;
    private String cardBrand;
    @Column(name = "card_last4")
    private String cardLast4;
    private Integer cardExpMonth;
    private Integer cardExpYear;
    private String shipmentId;
    @Column(name = "tracking_url")
    private String trackingUrl;

    public String getTrackingUrl() {
        return trackingUrl;
    }

    public void setTrackingUrl(String trackingUrl) {
        this.trackingUrl = trackingUrl;
    }

    public String getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(String shipmentId) {
        this.shipmentId = shipmentId;
    }

    public String getStripePaymentIntentId() {
        return stripePaymentIntentId;
    }

    public void setStripePaymentIntentId(String stripePaymentIntentId) {
        this.stripePaymentIntentId = stripePaymentIntentId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public Order(Long id, UUID orderId, Long buyerId, Long vendorId, LocalDateTime creationDate, String status,
            String shipmentId) {
        this.id = id;
        this.orderId = orderId;
        this.buyerId = buyerId;
        this.vendorId = vendorId;
        this.creationDate = creationDate;
        this.status = status;
        this.shipmentId = shipmentId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    public Long getVendorId() {
        return vendorId;
    }

    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public Order() {

    }

}
