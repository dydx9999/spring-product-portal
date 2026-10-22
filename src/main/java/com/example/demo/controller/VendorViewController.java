package com.example.demo.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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

import com.example.demo.dto.OrderSummaryDTO;
import com.example.demo.dto.ShippingDetailsFormDTO;
import com.example.demo.dto.VendorOrderItemsViewDTO;
import com.example.demo.dto.VendorOrderListDTO;
import com.example.demo.dto.VendorPickListDTO;
import com.example.demo.dto.VendorSalesRankingDTO;
import com.example.demo.dto.WarehouseDTO;
import com.example.demo.model.PageView;
import com.example.demo.model.Product;
import com.example.demo.model.User;
import com.example.demo.model.Vendor;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.service.OrderService;
import com.example.demo.service.UserService;
import com.example.demo.service.VendorService;
import com.example.demo.service.WarehouseService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/vendor")
public class VendorViewController {
        private final WarehouseService warehouseService;
        private final OrderService orderService;
        private final VendorService vendorService;
        private final UserService userService;
        private final ProductRepository productRepository;
        private final OrderItemRepository orderItemRepository;

        public VendorViewController(VendorService vendorService, UserService userService,
                        ProductRepository productRepository, OrderService orderService,
                        WarehouseService warehouseService, OrderItemRepository orderItemRepository) {
                this.vendorService = vendorService;
                this.userService = userService;
                this.productRepository = productRepository;
                this.orderService = orderService;
                this.warehouseService = warehouseService;
                this.orderItemRepository = orderItemRepository;
        }

        @GetMapping
        public String displayVendorDashboard(@RequestParam(defaultValue = "1") int productPage,
                        @RequestParam(defaultValue = "1") int salesPage, @RequestParam(required = false) String status,
                        HttpServletRequest request,
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
                VendorRatingDTO vendorRatingStats = vendorService.getVendorStats(currentVendor.getBusinessName());
                VendorSalesRankingDTO vendorRank = vendorService
                                .getVendorSalesRankings(currentVendor.getId());
                model.addAttribute("vendorRank", vendorRank);
                model.addAttribute("vendorRatingStats", vendorRatingStats);
                Long vendorUnitsSold = orderItemRepository
                                .findUnitsSoldByVendorId(currentVendor.getId());
                model.addAttribute("vendorUnitsSold", vendorUnitsSold);
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
                model.addAttribute("productPage", productPage);
                model.addAttribute("status", status);
                PageView<OrderSummaryDTO> salesPager = orderService
                                .getVendorSales(currentVendor.getId(), status, salesPage, PageView.DEFAULT_SIZE)
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
                        if (status.equals("Accepted")) {
                                redirectAttributes.addFlashAttribute("successMessage", "Order successfully accepted!");
                        }
                } catch (ResponseStatusException e) {
                        redirectAttributes.addFlashAttribute("errorMessage", e.getReason());
                }
                return "redirect:/vendor";
        }

        @GetMapping("/discounts")
        public String showDiscountsManager(
                        Model model,
                        Authentication authentication,
                        RedirectAttributes redirectAttributes) {
                User currentUser = userService.getUserByUsername(authentication.getName());
                Vendor currentVendor = vendorService.getVendorByUserId(currentUser.getId());
                var products = productRepository.findByVendorNameIgnoreCase(
                                currentVendor.getBusinessName(), Sort.by(Sort.Direction.ASC, "id"));
                model.addAttribute("products", products);
                return "vendor-discounts-manager";
        }

        @PostMapping("/orders/{orderId}/shipping")
        public String addShippingDetails(
                        @PathVariable("orderId") UUID orderId,
                        Model model,
                        Authentication authentication,
                        RedirectAttributes redirectAttributes,
                        ShippingDetailsFormDTO shippingDetailsForm) {
                User currentUser = userService.getUserByUsername(authentication.getName());
                Vendor currentVendor = vendorService.getVendorByUserId(currentUser.getId());
                model.addAttribute("orderId", orderId);
                model.addAttribute("shippingDetailsForm", shippingDetailsForm);
                model.addAttribute("currentVendor", currentVendor);
                try {
                        orderService.updateTracking(currentVendor.getId(), shippingDetailsForm);
                        redirectAttributes.addFlashAttribute("successMessage", "Shipping details updated successfully");
                } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
                }
                return "redirect:/vendor";
        }

        @GetMapping("/orders/{orderId}/shipping")
        public String showShippingDetailsForm(
                        @PathVariable("orderId") UUID orderId,
                        Model model,
                        Authentication authentication,
                        RedirectAttributes redirectAttributes,
                        ShippingDetailsFormDTO shippingDetailsForm) {
                User currentUser = userService.getUserByUsername(authentication.getName());
                Vendor currentVendor = vendorService.getVendorByUserId(currentUser.getId());
                model.addAttribute("currentVendor", currentVendor);

                model.addAttribute("shippingDetailsForm", shippingDetailsForm);
                return "vendor-shipment-details-form";
        }

        @PostMapping("/discounts/{productId}")
        public String handleDiscountsManagerForm(
                        @PathVariable("productId") Long productId,
                        @RequestParam int discountPercent,
                        Authentication authentication,
                        RedirectAttributes redirectAttributes,
                        Model model) {
                User currentUser = userService.getUserByUsername(authentication.getName());
                Vendor currentVendor = vendorService.getVendorByUserId(currentUser.getId());
                if (discountPercent < 0 || discountPercent > 100) {
                        redirectAttributes.addFlashAttribute("errorMessage", "Discount must be between 0 and 100%.");
                        return "redirect:/vendor/discounts";
                }
                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                org.springframework.http.HttpStatus.NOT_FOUND, "Product not found"));
                if (!currentVendor.getBusinessName().equals(product.getVendorName())) {
                        throw new ResponseStatusException(
                                        org.springframework.http.HttpStatus.FORBIDDEN,
                                        "You can only discount your own products");
                }
                product.setDiscountPercent(discountPercent);
                productRepository.save(product);
                model.addAttribute("newPrice", productRepository.calculateDiscountedProductPrice(productId));
                redirectAttributes.addFlashAttribute("savedProductId", productId);
                redirectAttributes.addFlashAttribute("successMessage", "Discount successfully applied!");
                return "redirect:/vendor/discounts";
        }

        @GetMapping("/order-pick-list/{orderId}")
        public String getVendorPickList(
                        @PathVariable UUID orderId,
                        Authentication authentication,
                        Model model) {
                User currentUser = userService.getUserByUsername(authentication.getName());
                Vendor currentVendor = vendorService.getVendorByUserId(currentUser.getId());
                List<VendorPickListDTO> pickList = orderItemRepository.findVendorPickListByOrderId(orderId,
                                currentVendor.getId());
                model.addAttribute("pickList", pickList);
                model.addAttribute("orderId", orderId);
                Map<Long, String> productNamesById = pickList.stream()
                                .collect(Collectors.toMap(
                                                VendorPickListDTO::productId,
                                                VendorPickListDTO::productName,
                                                (existingName, duplicateName) -> existingName));
                model.addAttribute("productNamesById", productNamesById);
                return "order-pick-list";
        }

        @GetMapping("/orders/view")
        public String getVendorOrders(
                        Model model,
                        Authentication authentication,
                        RedirectAttributes redirectAttributes) {
                User currentUser = userService.getUserByUsername(authentication.getName());
                Vendor currentVendor = vendorService.getVendorByUserId(currentUser.getId());
                try {
                        List<VendorOrderListDTO> vendorOrders = orderService.findVendorOrderList(currentVendor.getId());
                        model.addAttribute("vendorOrders", vendorOrders);
                        return "vendor-orders-view";
                } catch (ResponseStatusException e) {
                        redirectAttributes.addFlashAttribute("errorMessage", e.getReason());
                        return "vendor-dashboard";
                }
        }

        @GetMapping("/orders/view/{orderId}")
        public String showVendorOrderItems(@PathVariable("orderId") UUID orderId, Model model,
                        Authentication authentication, RedirectAttributes redirectAttributes) {
                User currentUser = userService.getUserByUsername(authentication.getName());
                Vendor currentVendor = vendorService.getVendorByUserId(currentUser.getId());
                Long vendorId = currentVendor.getId();
                try {
                        List<VendorOrderItemsViewDTO> orderItems = orderService.findOrderItemsByVendor(orderId,
                                        vendorId);
                        model.addAttribute("orderItems", orderItems);
                        model.addAttribute("orderId", orderId);
                        return "vendor-order-view-single";

                } catch (ResponseStatusException e) {
                        redirectAttributes.addFlashAttribute("errorMessage", e.getReason());
                        return "vendor-dashboard";
                }
        }

        @GetMapping("/profile/{vendorId}")
        public String getVendorProfile(@PathVariable("vendorId") Long vendorId, Authentication authentication,
                        Model model) {
                User currentUser = userService.getUserByUsername(authentication.getName());
                Vendor currentVendor = vendorService.getVendorByUserId(currentUser.getId());
                if (vendorId == currentVendor.getId()) {
                        return "redirect:/user/profile";
                }

                return "vendor-profile";
        }

}
