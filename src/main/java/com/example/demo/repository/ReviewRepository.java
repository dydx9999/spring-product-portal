package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.dto.ProductReviewPageDTO;
import com.example.demo.dto.VendorReviewDTO;
import com.example.demo.model.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByOrderItemIdAndBuyerId(Long orderItemId, Long buyerId);

    List<Review> findByBuyerIdAndOrderItemIdIn(Long buyerId, Collection<Long> orderItemIds);

    Page<Review> findByBuyerId(Long buyerId, Pageable pageable);

    @Query(value = """
            SELECT AVG(r.rating)
            FROM reviews r
            JOIN order_items oi ON oi.id = r.order_item_id
            WHERE oi.product_id = :productId
            """, nativeQuery = true)
    Double findAverageRatingByProductId(@Param("productId") Long productId);

    @Query(value = """
            SELECT
                R.COMMENT AS comment,
                U.USERNAME AS username,
                R.RATING AS rating,
                R.CREATED_AT as createdAt
            FROM REVIEWS R
            JOIN ORDER_ITEMS OI ON R.ORDER_ITEM_ID = OI.ID
            JOIN USERS U ON R.BUYER_ID = U.ID
            WHERE OI.PRODUCT_ID = :productId
            ORDER BY R.CREATED_AT DESC, R.ID DESC
            """, nativeQuery = true)
    List<ProductReviewPageDTO> findReviewsByProductId(@Param("productId") Long productId);

    @Query(value = """
            SELECT
                R.COMMENT AS comment,
                U.USERNAME AS username,
                R.RATING AS rating,
                R.CREATED_AT AS createdAt
            FROM REVIEWS R
            JOIN ORDER_ITEMS OI ON R.ORDER_ITEM_ID = OI.ID
            JOIN PRODUCTS P ON OI.PRODUCT_ID = P.ID
            JOIN VENDORS V ON V.BUSINESS_NAME = P.VENDOR_NAME
            JOIN USERS U ON R.BUYER_ID = U.ID
            WHERE V.ID = :vendorId
            ORDER BY R.CREATED_AT DESC, R.ID DESC
            """, nativeQuery = true)
    List<VendorReviewDTO> findReviewsByVendorId(@Param("vendorId") Long vendorId);

    interface VendorReviewView {
        String getComment();

        String getUsername();

        Integer getRating();

        LocalDateTime getCreatedAt();
    }

}