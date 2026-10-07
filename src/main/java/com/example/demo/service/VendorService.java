package com.example.demo.service;

import org.springframework.stereotype.Service;

import com.example.demo.repository.VendorRepository;
import com.example.demo.model.Vendor;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

@Service
public class VendorService {
    private final VendorRepository vendorRepository;

    public VendorService(VendorRepository vendorRepository) {
        this.vendorRepository = vendorRepository;
    }

    public List<Vendor> listVendors() {
        return vendorRepository.findAll();
    }

    public Vendor createVendor(Long userId, String businessName) {
        Vendor vendor = new Vendor(null, userId, List.of(), 0, BigDecimal.ZERO,
                new HashMap<>(), businessName);
        return vendorRepository.save(vendor);
    }
}