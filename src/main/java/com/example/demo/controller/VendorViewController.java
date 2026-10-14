package com.example.demo.controller;

import com.example.demo.service.OrderService;
import com.example.demo.service.WarehouseService;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.demo.model.OrderSummaryDTO;
import com.example.demo.model.PageView;
import com.example.demo.model.User;
import com.example.demo.model.Vendor;
import com.example.demo.model.WarehouseDTO;
import com.example.demo.repository.ProductRepository;
import com.example.demo.service.UserService;
import com.example.demo.service.VendorService;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/vendor")
public class VendorViewController {
        private final WarehouseService warehouseService;
        private final OrderService orderService;
        private final VendorService vendorService;
        private final UserService userService;
        private final ProductRepository productRepository;

        public VendorViewController(VendorService vendorService, UserService userService,
                        ProductRepository productRepository, OrderService orderService,
                        WarehouseService warehouseService) {
                this.vendorService = vendorService;
                this.userService = userService;
                this.productRepository = productRepository;
                this.orderService = orderService;
                this.warehouseService = warehouseService;
        }

        @GetMapping
        public String displayVendorDashboard(@RequestParam(defaultValue = "1") int productPage,
                        @RequestParam(defaultValue = "1") int salesPage, HttpServletRequest request,
                        Model model, Authentication authentication) {

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
                var products = productRepository.findByVendorNameIgnoreCase(
                                currentVendor.getBusinessName(), Sort.by(Sort.Direction.ASC, "id"));
                Map<Long, String> warehouseNamesById = warehouseService.listWarehouses().stream()
                                .collect(Collectors.toMap(WarehouseDTO::getWarehouseId,
                                                WarehouseDTO::getWarehouseName,
                                                (existing, replacement) -> existing));
                model.addAttribute("warehouseNamesById", warehouseNamesById);
                model.addAttribute("products", products);
                model.addAttribute("productPager",
                                PageView.of(products, productPage, PageView.DEFAULT_SIZE)
                                                .withLinks(request, "productPage"));
                model.addAttribute("productsListedCount", products.size());
                model.addAttribute("businessName", currentVendor.getBusinessName());
                PageView<OrderSummaryDTO> salesPager = orderService
                                .getVendorSales(currentVendor.getId(), salesPage, PageView.DEFAULT_SIZE)
                                .withLinks(request, "salesPage");
                model.addAttribute("orderDetails", salesPager.getContent());
                model.addAttribute("salesPager", salesPager);
                return "vendor-dashboard";
        }

        @PostMapping("/orders/{orderId}/status")
        public String updateOrderStatus(@PathVariable UUID orderId, @RequestParam String status,
                        Authentication authentication, RedirectAttributes redirectAttributes) {
                User currentUser = userService.getUserByUsername(authentication.getName());
                Vendor currentVendor = vendorService.getVendorByUserId(currentUser.getId());
                try {
                        orderService.updateStatus(orderId, currentVendor.getId(), status);
                        redirectAttributes.addFlashAttribute("successMessage", "Order status updated.");
                } catch (ResponseStatusException e) {
                        redirectAttributes.addFlashAttribute("errorMessage", e.getReason());
                }
                return "redirect:/vendor";
        }
}
