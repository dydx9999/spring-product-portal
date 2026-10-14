package com.example.demo.repository;

import com.example.demo.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CartRepository extends JpaRepository<Cart, Long> {
    @Query("SELECT c FROM Cart c WHERE c.userId = :userId")
    List<Cart> findByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM Cart c WHERE c.productId = :productId AND c.userId = :userId")
    void deleteByProductIdAndUserId(@Param("productId") Long productId, @Param("userId") Long userId);

    @Query("SELECT c FROM Cart c WHERE c.productId = :productId AND c.userId = :userId ORDER BY c.id")
    List<Cart> findByProductIdUserId(@Param("productId") Long productId, @Param("userId") Long userId);

    @Query("SELECT COUNT(c) FROM Cart c WHERE c.productId = :productId")
    int countProductsInCarts(@Param("productId") Long productId);

    @Query("SELECT COUNT(DISTINCT c.userId) FROM Cart c WHERE c.productId = :productId")
    long countDistinctUsersWithProduct(@Param("productId") Long productId);

}
