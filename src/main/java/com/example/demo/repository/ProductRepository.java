package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.model.Product;
import com.example.demo.model.ProductDTO;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("""
            select new com.example.demo.model.ProductDTO(
                p.id, p.productName, p.productPrice, p.vendorName
            )
            from Product p
            """)
    List<ProductDTO> findAllProducts();

    List<Product> findByProductNameContainingIgnoreCase(String productName, Sort sort);

    List<Product> findByVendorNameIgnoreCase(String vendorName, Sort sort);

    boolean existsByProductNameIgnoreCase(String productName);

    List<Product> findByProductNameIgnoreCase(String productName);

    Optional<Product> findById(Long id);

    @Query("SELECT COUNT(p) FROM Product p")
    long countProducts();

    @Query("SELECT COUNT(p) FROM Product p WHERE(p.productQuantity) = 0")
    long countOutOfStockProducts();

    @Query("SELECT p FROM Product p WHERE p.productQuantity <= 0")
    List<Product> findOutOfStockProducts(Sort sort);

    @Query("SELECT p FROM Product p WHERE p.productQuantity <= 5 AND p.productQuantity > 0")
    List<Product> lowStockAlert();

    @Query("SELECT p FROM Product p WHERE p.productQuantity <= 0 AND p.vendorName = :businessName")
    List<Product> outOfStockProductsCurrentVendor(@Param("businessName") String businessName, Sort sort);

    @Query("SELECT p FROM Product p WHERE p.productQuantity > 0")
    List<Product> findInStockProducts();

    @Query("SELECT p FROM Product p WHERE p.productQuantity <= p.lowStockThreshold AND p.vendorName = :businessName")
    List<Product> lowStockCurrentVendor(@Param("businessName") String businessName, Sort sort);

    @Modifying
    @Query("UPDATE Product p SET p.productQuantity = p.productQuantity - :quantity WHERE p.id= :productId AND p.productQuantity >= :quantity")

    int decreaseStockIfAvailable(
            @Param("productId") Long productId,
            @Param("quantity") int quantity);

    @Query(value = """
            SELECT SUM(oi.product_quantity) AS units_sold
            FROM order_items oi
            JOIN products p ON p.product_name = oi.product_name
            WHERE p.id = :productId
            GROUP BY oi.product_name
                """, nativeQuery = true)
    Long findUnitsSoldByProductId(Long productId);

    @Query(value = """
            SELECT ROUND(p.product_price * (100 - COALESCE(p.discount_percent, 0)) / 100.0, 2)
            AS discounted_price
            FROM products p
            WHERE p.id = :productId
            """, nativeQuery = true)
    BigDecimal calculateDiscountedProductPrice(Long productId);

}
