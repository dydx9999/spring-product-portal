package com.example.demo.service;

import org.springframework.stereotype.Service;

import com.example.demo.repository.VendorRepository;
import com.example.demo.model.Vendor;
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
}