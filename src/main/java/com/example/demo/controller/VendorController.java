package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.repository.VendorRepository;

@RestController
@RequestMapping("/api/vendor")
public class VendorController {
    private final VendorRepository vendorRepository;

    public VendorController(VendorRepository vendorRepository) {
        this.vendorRepository = vendorRepository;
    }

    @GetMapping("/ratings")
    public ResponseEntity<?> getVendorRatings(@RequestParam String vendorName) {

        VendorRatingDTO vendorRating = vendorRepository.findVendorStats(vendorName);
        return ResponseEntity.ok(vendorRating);

    }
}
