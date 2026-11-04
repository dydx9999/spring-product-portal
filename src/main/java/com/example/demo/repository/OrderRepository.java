package com.example.demo.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.dto.ShipmentTrackingDTO;
import com.example.demo.dto.VendorOrderListDTO;
import com.example.demo.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByBuyerId(Long buyerId, Pageable pageable);

    List<Order> findByVendorId(Long vendorId);

    Page<Order> findByVendorId(Long vendorId, Pageable pageable);

    Page<Order> findByVendorIdAndStatusIgnoreCase(Long vendorId, String status, Pageable pageable);

    List<Order> findByOrderIdAndBuyerId(UUID orderId, Long buyerId);

    List<Order> findByOrderId(UUID orderId);

    Order findByOrderIdAndVendorId(UUID orderId, Long vendorId);

    @Query(value = """
            SELECT o.order_id, o.buyer_id, o.creation_date, o.status
            FROM orders o
            WHERE o.vendor_id = :vendorId
            ORDER BY o.creation_date DESC;
            """, nativeQuery = true)
    List<VendorOrderListDTO> findOrdersByVendorId(Long vendorId);

    @Query(value = """
            SELECT v.business_name, o.status, o.carrier, o.tracking_number, o.tracking_url
            FROM orders o
            LEFT JOIN vendors v ON v.id = o.vendor_id
            WHERE o.order_id = :orderId
              AND o.buyer_id = :buyerId
              AND o.tracking_number IS NOT NULL
              AND o.tracking_number <> ''
            ORDER BY v.business_name
            """, nativeQuery = true)
    List<ShipmentTrackingDTO> findShipmentTrackingByOrderIdAndBuyerId(@Param("orderId") UUID orderId,
            @Param("buyerId") Long buyerId);

}
