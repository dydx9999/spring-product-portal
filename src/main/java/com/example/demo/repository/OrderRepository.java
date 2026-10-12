package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Order;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByBuyerIdOrderByCreationDateDesc(Long buyerId);

    List<Order> findByVendorId(Long vendorId);

    List<Order> findByOrderIdAndBuyerId(UUID orderId, Long buyerId);
}
