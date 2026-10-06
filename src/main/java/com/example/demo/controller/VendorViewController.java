package com.example.demo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import java.util.List;
import com.example.demo.model.Product;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.ui.Model;
import com.example.demo.service.VendorService;
import com.example.demo.model.*;

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
        // model.addAttribute("vendorSales", vendorService.findById(vendorId)); // TODO:
        // implement vendor lookup
        return "vendor-dashboard";
    }
}
