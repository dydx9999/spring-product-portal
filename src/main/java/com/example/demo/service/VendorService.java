package com.example.demo.service;

import org.springframework.stereotype.Service;

import com.example.demo.repository.VendorRepository;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.model.Vendor;
import com.example.demo.model.Order;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class VendorService {
    private final VendorRepository vendorRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public VendorService(VendorRepository vendorRepository, OrderRepository orderRepository,
            OrderItemRepository orderItemRepository) {
        this.vendorRepository = vendorRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public List<Vendor> listVendors() {
        return vendorRepository.findAll();
    }

    public Vendor getVendorByUserId(Long userId) {
        List<Vendor> matchingVendors = vendorRepository.findAllByUserId(userId);
        if (matchingVendors.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vendor profile not found");
        }
        if (matchingVendors.size() > 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Multiple vendor profiles found for user");
        }
        return matchingVendors.get(0);
    }

    public Vendor createVendor(Long userId, String businessName) {
        Vendor vendor = new Vendor(null, userId, List.of(), 0, BigDecimal.ZERO,
                new HashMap<>(), businessName);
        return vendorRepository.save(vendor);
    }

    public Vendor updateBusinessName(Long userId, String businessName) {
        if (businessName == null || businessName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Business name is required");
        }
        Vendor vendor = getVendorByUserId(userId);
        vendor.setBusinessName(businessName);
        return vendorRepository.save(vendor);
    }

    public BigDecimal calculateTotalSalesRevenue(Long vendorId) {
        List<Order> vendorOrders = orderRepository.findByVendorId(vendorId);
        if (vendorOrders.isEmpty()) {
            return BigDecimal.ZERO;
        }

        List<Long> orderIds = vendorOrders.stream().map(Order::getId).toList();
        return orderItemRepository.findByOrderIdIn(orderIds).stream()
                .map(item -> item.getProductPrice().multiply(BigDecimal.valueOf(item.getProductQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int calculateTotalSales(Long vendorId) {

        List<Order> vendorOrders = orderRepository.findByVendorId(vendorId);
        if (vendorOrders.isEmpty()) {
            return 0;
        }

        return vendorOrders.size();
    }

}