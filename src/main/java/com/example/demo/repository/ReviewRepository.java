package com.example.demo.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByOrderItemIdAndBuyerId(Long orderItemId, Long buyerId);

    List<Review> findByBuyerIdAndOrderItemIdIn(Long buyerId, Collection<Long> orderItemIds);

    Page<Review> findByBuyerId(Long buyerId, Pageable pageable);
}