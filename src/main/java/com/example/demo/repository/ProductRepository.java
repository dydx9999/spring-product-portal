package com.example.demo.repository;

import com.example.demo.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
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

}
