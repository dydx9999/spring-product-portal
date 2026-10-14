package com.example.demo.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.model.Warehouse;
import com.example.demo.model.WarehouseDTO;
import com.example.demo.repository.WarehouseRepository;

@Service
public class WarehouseService {
    private final WarehouseRepository warehouseRepository;

    public WarehouseService(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    public List<WarehouseDTO> listWarehouses() {
        Map<Long, String> warehouseNamesById = warehouseRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Warehouse::getId, Warehouse::getWarehouseName));

        return warehouseNamesById.entrySet()
                .stream()
                .map(entry -> new WarehouseDTO(entry.getKey(), entry.getValue()))
                .toList();
    }
}
