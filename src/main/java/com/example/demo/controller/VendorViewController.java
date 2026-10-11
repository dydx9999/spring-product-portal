package com.example.demo.controller;

import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.example.demo.model.User;
import com.example.demo.model.Vendor;
import com.example.demo.repository.ProductRepository;
import com.example.demo.service.UserService;
import com.example.demo.service.VendorService;

@Controller
@RequestMapping("/vendor")
public class VendorViewController {
        private final VendorService vendorService;
        private final UserService userService;
        private final ProductRepository productRepository;

        public VendorViewController(VendorService vendorService, UserService userService,
                        ProductRepository productRepository) {
                this.vendorService = vendorService;
                this.userService = userService;
                this.productRepository = productRepository;
        }

        @GetMapping
        public String displayVendorDashboard(Model model, Authentication authentication) {

                User currentUser = userService.getUserByUsername(authentication.getName());

                boolean isAdmin = authentication.getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                model.addAttribute("isAdmin", isAdmin);
                boolean isUser = authentication.getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"));
                model.addAttribute("isUser", isUser);
                boolean isVendor = authentication.getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("ROLE_VENDOR"));
                Vendor currentVendor = vendorService.getVendorByUserId(currentUser.getId());
                currentVendor.setSalesRevenue(vendorService.calculateTotalSalesRevenue(currentVendor.getId()));
                currentVendor.setSalesCount(vendorService.calculateTotalSales(currentVendor.getId()));
                model.addAttribute("isVendor", isVendor);
                model.addAttribute("vendorUsername", currentUser.getUsername());
                model.addAttribute("currentVendor", currentVendor);
                model.addAttribute("products", productRepository.findByVendorNameIgnoreCase(
                                currentVendor.getBusinessName(), Sort.by(Sort.Direction.ASC, "id")));
                model.addAttribute("businessName", currentVendor.getBusinessName());

                return "vendor-dashboard";
        }
}
