package com.example.demo.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.easypost.exception.EasyPostException;
import com.easypost.model.Rate;
import com.easypost.model.Shipment;
import com.easypost.model.Tracker;
import com.easypost.service.EasyPostClient;
import com.example.demo.dto.ShipmentCreateFormDTO;
import com.example.demo.dto.ShipmentLabelDTO;
import com.example.demo.dto.ShipmentRateDTO;
import com.example.demo.dto.ShipmentRatesDTO;
import com.example.demo.dto.ShippingDetailsFormDTO;
import com.example.demo.model.Order;
import com.example.demo.repository.OrderRepository;

@Service
public class ShippingService {

    private static final Logger log = LoggerFactory.getLogger(ShippingService.class);

    // Prefix match: EasyPost names vary by account type (e.g. FedExDefault, UPSDAP)
    private static final List<String> ALLOWED_CARRIERS = List.of("USPS", "UPS", "FedEx");

    private final EasyPostClient easyPostClient;
    private final OrderService orderService;
    private final OrderRepository orderRepository;

    public ShippingService(EasyPostClient easyPostClient, OrderService orderService, OrderRepository orderRepository) {
        this.easyPostClient = easyPostClient;
        this.orderService = orderService;
        this.orderRepository = orderRepository;
    }

    public ShipmentRatesDTO createShipment(ShipmentCreateFormDTO form) throws EasyPostException {
        Map<String, Object> params = new HashMap<>();
        params.put("from_address", form.getFromAddress().toParams());
        params.put("to_address", form.getToAddress().toParams());
        params.put("parcel", form.getParcel().toParams());
        params.put("reference", form.getReference());

        Shipment shipment = easyPostClient.shipment.create(params);

        List<ShipmentRateDTO> rates = shipment.getRates().stream()
                .filter(rate -> isAllowedCarrier(rate.getCarrier()))
                .map(ShippingService::toRateDTO)
                .sorted(Comparator.comparing(ShipmentRateDTO::rate))
                .toList();

        return new ShipmentRatesDTO(shipment.getId(), rates, shipment.getReference());
    }

    public ShipmentLabelDTO buyShipment(Long vendorId, String shipmentId, String rateId) throws EasyPostException {
        // Check the order before paying for a label so a bad order can't leave a
        // purchased, unsaved label
        UUID orderId = orderIdFrom(easyPostClient.shipment.retrieve(shipmentId));
        Order order = orderRepository.findByOrderIdAndVendorId(orderId, vendorId);
        if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
        }
        if (!"Accepted".equalsIgnoreCase(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Labels can only be purchased for accepted orders");
        }

        Map<String, Object> params = new HashMap<>();
        params.put("rate", Map.of("id", rateId));

        Shipment shipment = easyPostClient.shipment.buy(shipmentId, params);
        Rate selected = shipment.getSelectedRate();

        ShippingDetailsFormDTO shippingDetails = new ShippingDetailsFormDTO();
        shippingDetails.setOrderId(orderId);
        shippingDetails.setShipmentId(shipmentId);
        shippingDetails.setTrackingNumber(shipment.getTrackingCode());
        shippingDetails.setCarrier(carrierGroup(selected.getCarrier()));
        orderService.updateTracking(vendorId, shippingDetails);

        return toLabelDTO(shipment);
    }

    public ShipmentLabelDTO displayShipmentDetails(Long vendorId, String shipmentId) throws EasyPostException {
        Shipment shipment = easyPostClient.shipment.retrieve(shipmentId);
        Order order = orderRepository.findByOrderIdAndVendorId(orderIdFrom(shipment), vendorId);
        if (order == null || !shipmentId.equals(order.getShipmentId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No shipment found for provided ID!");
        }
        if (shipment.getSelectedRate() == null || shipment.getPostageLabel() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No label has been purchased for this shipment");
        }
        return toLabelDTO(shipment);
    }

    public void syncDeliveredShipments(UUID orderId, Long buyerId) {
        for (Order order : orderRepository.findByOrderIdAndBuyerId(orderId, buyerId)) {
            if (!"Shipped".equals(order.getStatus()) || order.getShipmentId() == null) {
                continue;
            }
            try {
                Tracker tracker = easyPostClient.shipment.retrieve(order.getShipmentId()).getTracker();
                if (tracker != null && "delivered".equals(tracker.getStatus())) {
                    orderService.updateStatus(orderId, order.getVendorId(), "Delivered");
                }
            } catch (EasyPostException e) {
                // An EasyPost outage shouldn't stop the buyer viewing their order
                log.warn("Tracker lookup failed for shipment {}: {}", order.getShipmentId(), e.getMessage());
            }
        }
    }

    public LocalDateTime calculateDeliveryDate(String shipmentId) {
        if (shipmentId == null || shipmentId.isBlank()) {
            return null;
        }
        Shipment shipment;
        try {
            shipment = easyPostClient.shipment.retrieve(shipmentId);
        } catch (EasyPostException e) {
            log.warn("Delivery estimate lookup failed for shipment {}: {}", shipmentId, e.getMessage());
            return null;
        }
        if (shipment == null || shipment.getPostageLabel() == null
                || shipment.getPostageLabel().getCreatedAt() == null
                || shipment.getSelectedRate() == null
                || shipment.getSelectedRate().getEstDeliveryDays() == null) {
            return null;
        }
        Instant instant = (shipment.getPostageLabel().getCreatedAt()).toInstant();
        LocalDateTime labelPurchaseDate = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

        int estimatedDays = (shipment.getSelectedRate().getEstDeliveryDays()).intValue();
        LocalDateTime estimatedDeliveryDate = labelPurchaseDate.plusDays(estimatedDays);
        return estimatedDeliveryDate;

    }

    private static UUID orderIdFrom(Shipment shipment) {
        try {
            return UUID.fromString(shipment.getReference());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Shipment is not linked to an order");
        }
    }

    private static ShipmentLabelDTO toLabelDTO(Shipment shipment) {
        Rate selected = shipment.getSelectedRate();
        return new ShipmentLabelDTO(
                shipment.getId(),
                shipment.getReference(),
                selected.getCarrier(),
                selected.getService(),
                shipment.getTrackingCode(),
                shipment.getPostageLabel().getLabelUrl(),
                shipment.getTracker() != null ? shipment.getTracker().getPublicUrl() : null);
    }

    private static boolean isAllowedCarrier(String carrier) {
        return carrier != null && ALLOWED_CARRIERS.stream().anyMatch(carrier::startsWith);
    }

    // Maps EasyPost names like FedExDefault to the USPS / UPS / FedEx values used
    // on orders
    private static String carrierGroup(String carrier) {
        if (carrier == null) {
            return null;
        }
        return ALLOWED_CARRIERS.stream().filter(carrier::startsWith).findFirst().orElse(carrier);
    }

    private static ShipmentRateDTO toRateDTO(Rate rate) {
        return new ShipmentRateDTO(
                rate.getId(),
                rate.getCarrier(),
                rate.getService(),
                new BigDecimal(String.valueOf(rate.getRate())),
                rate.getCurrency(),
                rate.getEstDeliveryDays() != null ? rate.getEstDeliveryDays().intValue() : null);
    }

}
