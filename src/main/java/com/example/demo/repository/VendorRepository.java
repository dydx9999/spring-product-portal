package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.controller.VendorRatingDTO;
import com.example.demo.model.Vendor;

public interface VendorRepository extends JpaRepository<Vendor, Long> {
    List<Vendor> findAllByBusinessName(String businessName);

    List<Vendor> findAllByUserId(Long userId);

    @Query(value = """
            SELECT p.vendor_name,
            ROUND(AVG(r.rating),2)AS average_rating,
            COUNT(r.rating) AS reviews_count
            FROM reviews r
            JOIN order_items oi ON oi.id = r.order_item_id
            JOIN products p ON p.id = oi.product_id
            WHERE p.vendor_name = :vendorName
            GROUP BY p.vendor_name
            ORDER BY REVIEWS_COUNT DESC;
            """, nativeQuery = true)
    <Optional> VendorRatingDTO findVendorStats(@Param("vendorName") String vendorName);

}
