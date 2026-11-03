package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.easypost.exception.EasyPostException;
import com.easypost.model.Shipment;
import com.easypost.service.EasyPostClient;

@RestController
@RequestMapping("/api/shipping")
public class ShippingController {

    private final EasyPostClient easyPostClient;

    public ShippingController(EasyPostClient easyPostClient) {
        this.easyPostClient = easyPostClient;
    }

    @PostMapping("/shipments")
    public ResponseEntity<?> createShipment(@RequestBody Map<String, Object> requestPayload) {
        try {
            Map<String, Object> shipmentParams = new HashMap<>();
            shipmentParams.put("to_address", requestPayload.get("to_address"));
            shipmentParams.put("from_address", requestPayload.get("from_address"));
            shipmentParams.put("parcel", requestPayload.get("parcel"));
            Shipment shipment = easyPostClient.shipment.create(shipmentParams);
            return ResponseEntity.ok(shipment);

        } catch (EasyPostException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping("/shipments/{shipmentId}/buy")
    public ResponseEntity<?> buyShipment(@PathVariable String shipmentId,
            @RequestBody Map<String, Object> requestPayload) {
        try {
            Map<String, Object> buyParams = new HashMap<>();
            buyParams.put("rate", Map.of("id", requestPayload.get("rate_id")));

            Shipment shipment = easyPostClient.shipment.buy(shipmentId, buyParams);
            return ResponseEntity.ok(shipment);

        } catch (EasyPostException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}
