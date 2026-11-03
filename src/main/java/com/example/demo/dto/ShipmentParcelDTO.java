package com.example.demo.dto;

import java.util.Map;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// Dimensions in inches, weight in ounces (EasyPost units)
public class ShipmentParcelDTO {
    @NotNull
    @Positive
    private Double length;
    @NotNull
    @Positive
    private Double width;
    @NotNull
    @Positive
    private Double height;
    @NotNull
    @Positive
    private Double weight;

    public Map<String, Object> toParams() {
        return Map.of("length", length, "width", width, "height", height, "weight", weight);
    }

    public Double getLength() {
        return length;
    }

    public void setLength(Double length) {
        this.length = length;
    }

    public Double getWidth() {
        return width;
    }

    public void setWidth(Double width) {
        this.width = width;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }
}
