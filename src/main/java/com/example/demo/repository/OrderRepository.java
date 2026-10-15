package com.example.demo.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Order;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByBuyerId(Long buyerId, Pageable pageable);

    List<Order> findByVendorId(Long vendorId);

    Page<Order> findByVendorId(Long vendorId, Pageable pageable);

    List<Order> findByOrderIdAndBuyerId(UUID orderId, Long buyerId);

    Order findByOrderIdAndVendorId(UUID orderId, Long vendorId);

}
