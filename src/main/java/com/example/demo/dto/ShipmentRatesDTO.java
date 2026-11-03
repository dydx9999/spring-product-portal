package com.example.demo.dto;

import java.util.List;

public record ShipmentRatesDTO(String shipmentId, List<ShipmentRateDTO> rates, String reference) {
}
