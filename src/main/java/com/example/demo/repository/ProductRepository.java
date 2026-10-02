package com.example.demo.repository;

import com.example.demo.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByProductNameContainingIgnoreCase(String productName);

    boolean existsByProductNameIgnoreCase(String productName);

    List<Product> findByProductNameIgnoreCase(String productName);

}
