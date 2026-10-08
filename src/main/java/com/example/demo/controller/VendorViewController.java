package com.example.demo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.service.VendorService;

@Controller
@RequestMapping("/vendor")
public class VendorViewController {
    private final VendorService vendorService;

    public VendorViewController(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    @GetMapping
    public String displayVendorDashboard(Model model, Authentication authentication) {
        String currentVendor = authentication.getName();
        model.addAttribute("vendors", vendorService.listVendors());
        model.addAttribute("vendorUsername", currentVendor);
      
        return "vendor-dashboard";
    }
}
