package com.example.demo.dto;

public class WarehouseDTO {
    private Long warehouseId;
    private String warehouseName;

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public WarehouseDTO(Long warehouseId, String warehouseName) {
        this.warehouseId = warehouseId;
        this.warehouseName = warehouseName;
    }

    public WarehouseDTO() {

    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

}
