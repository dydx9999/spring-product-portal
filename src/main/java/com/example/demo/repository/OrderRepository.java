package com.example.demo.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.dto.VendorOrderListDTO;
import com.example.demo.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByBuyerId(Long buyerId, Pageable pageable);

    List<Order> findByVendorId(Long vendorId);

    Page<Order> findByVendorId(Long vendorId, Pageable pageable);

    Page<Order> findByVendorIdAndStatusIgnoreCase(Long vendorId, String status, Pageable pageable);

    List<Order> findByOrderIdAndBuyerId(UUID orderId, Long buyerId);

    Order findByOrderIdAndVendorId(UUID orderId, Long vendorId);

    @Query(value = """
            SELECT o.order_id, o.buyer_id, o.creation_date, o.status
            FROM orders o
            WHERE o.vendor_id = :vendorId
            ORDER BY o.creation_date DESC;
            """, nativeQuery = true)
    List<VendorOrderListDTO> findOrdersByVendorId(Long vendorId);

}
