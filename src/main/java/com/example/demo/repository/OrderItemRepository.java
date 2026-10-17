package com.example.demo.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.model.OrderItem;
import com.example.demo.model.VendorPickList;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
	List<OrderItem> findByOrderIdIn(List<Long> orderIds);

	List<OrderItem> findByOrderId(Long orderId);

	@Query("""
		SELECT NEW com.example.demo.model.VendorPickList(oi.id, oi.orderId, oi.productId, oi.productQuantity)
		FROM OrderItem oi, Order o
		WHERE o.id = oi.orderId AND o.orderId = :orderId AND o.vendorId = :vendorId
		""")
	List<VendorPickList> findVendorPickListByOrderId(@Param("orderId") UUID orderId,
			@Param("vendorId") Long vendorId);
}
