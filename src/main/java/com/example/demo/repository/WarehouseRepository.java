package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.model.Warehouse;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

}
