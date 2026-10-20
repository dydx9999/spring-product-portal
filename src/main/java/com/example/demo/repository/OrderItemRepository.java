package com.example.demo.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.dto.VendorPickListDTO;
import com.example.demo.model.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
	List<OrderItem> findByOrderIdIn(List<Long> orderIds);

	List<OrderItem> findByOrderId(Long orderId);

	@Query("""
			SELECT NEW com.example.demo.dto.VendorPickListDTO(oi.id, oi.orderId, oi.productId, oi.productName, oi.productQuantity, o.creationDate, w.warehouseName)
				FROM OrderItem oi
				JOIN Order o ON o.id = oi.orderId
				LEFT JOIN Product p ON p.id = oi.productId
				LEFT JOIN Warehouse w ON w.id = p.warehouseId
				WHERE o.orderId = :orderId AND o.vendorId = :vendorId
				""")
	List<VendorPickListDTO> findVendorPickListByOrderId(@Param("orderId") UUID orderId,
			@Param("vendorId") Long vendorId);

	@Query(value = """
			SELECT COALESCE(SUM(oi.product_quantity), 0)
			FROM orders o
			JOIN order_items oi ON oi.order_id = o.id
			WHERE o.vendor_id = :vendorId
			""", nativeQuery = true)

	Long findUnitsSoldByVendorId(@Param("vendorId") Long vendorId);

}
