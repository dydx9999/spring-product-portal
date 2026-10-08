package com.example.demo.repository;

import com.example.demo.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByProductNameContainingIgnoreCase(String productName);

    boolean existsByProductNameIgnoreCase(String productName);

    List<Product> findByProductNameIgnoreCase(String productName);

    Optional<Product> findById(Long id);

    @Query("SELECT COUNT(p) FROM Product p")
    long countProducts();

    @Query("SELECT COUNT(p) FROM Product p WHERE(p.productQuantity) = 0")
    long countOutOfStockProducts();

    @Query("SELECT p FROM Product p WHERE p.productQuantity <= 0")
    List<Product> findOutOfStockProducts();

}
