package com.example.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.model.OrderItem;
import com.example.demo.model.PageView;
import com.example.demo.model.Product;
import com.example.demo.model.User;
import com.example.demo.model.Vendor;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.repository.WarehouseRepository;
import com.example.demo.service.CartService;
import com.example.demo.service.ProductService;
import com.example.demo.service.UserService;
import com.example.demo.service.VendorService;
import com.example.demo.service.WishlistService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Controller
@RequestMapping("/products")
public class ProductViewController {
    private final WishlistService wishlistService;
    private final WarehouseRepository warehouseRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final UserService userService;
    private final CartService cartService;
    private final VendorService vendorService;
    private final ReviewRepository reviewRepository;

    // ---- Product list ----

    @GetMapping
    public String showProductList(@RequestParam(required = false) String filter,
            @RequestParam(defaultValue = "false") boolean inStockOnly, Model model,
            Authentication authentication) {
        User currentUser = userService.getUserByUsername(authentication.getName());

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);
        boolean isUser = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"));
        model.addAttribute("isUser", isUser);
        boolean isVendor = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_VENDOR"));
        String vendorBusinessName = null;
        if (isVendor) {
            Vendor vendor = vendorService.getVendorByUserId(currentUser.getId());
            vendorBusinessName = vendor.getBusinessName();
        }

        model.addAttribute("isVendor", isVendor);
        model.addAttribute("businessName", vendorBusinessName);

        model.addAttribute("username", currentUser.getUsername());
        model.addAttribute("productCount", productRepository.countProducts());
        model.addAttribute("outOfStockProducts", productRepository.countOutOfStockProducts());
        Map<String, Integer> quantitySoldByProduct = new HashMap<>();
        for (OrderItem orderItem : orderItemRepository.findAll()) {
            if (orderItem.getProductName() != null) {
                quantitySoldByProduct.merge(orderItem.getProductName(), orderItem.getProductQuantity(), Integer::sum);
            }
        }
        model.addAttribute("quantitySoldByProduct", quantitySoldByProduct);
        List<Product> products = filter == null
                ? productService.getProducts()
                : productService.getFilteredProducts(filter, vendorBusinessName);
        if (inStockOnly) {
            products = products.stream().filter(p -> p.getProductQuantity() > 0).toList();
        }
        model.addAttribute("products", products);
        model.addAttribute("inStockOnly", inStockOnly);

        return "products";
    }

    // ---- Add product ----

    @GetMapping("/add-product")
    public String showAddProductForm(Model model, Authentication authentication) {
        model.addAttribute("productForm", new Product());
        boolean isVendor = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_VENDOR"));
        model.addAttribute("isVendor", isVendor);
        model.addAttribute("warehouses", warehouseRepository.findAll());
        return "add-product";
    }

    @PostMapping("/add-product")
    public String handleAddProduct(@ModelAttribute("productForm") Product productForm,
            RedirectAttributes redirectAttributes, Authentication authentication) {
        User currentUser = userService.getUserByUsername(authentication.getName());
        String vendorName = vendorService.getVendorByUserId(currentUser.getId()).getBusinessName();
        boolean exists = productService.checkProductExists(productForm.getProductName());
        if (exists) {
            redirectAttributes.addFlashAttribute("errorMessage", "Product already exists");
        } else {
            productService.createProduct(productForm, vendorName);
            redirectAttributes.addFlashAttribute("successMessage", "Product successfully created");
        }
        return "redirect:/products/add-product";
    }

    // ---- Search products ----

    @GetMapping("/product-search")
    public String showProductSearchForm(Model model) {
        model.addAttribute("productSearchForm", new Product());
        model.addAttribute("warehouse", warehouseRepository.findAll());
        return "product-search";
    }

    @PostMapping("/product-search")
    public String handleProductSearch(@ModelAttribute("productSearchForm") Product productSearchForm, Model model) {
        model.addAttribute("searchResults", productService.searchProductsByName(productSearchForm.getProductName()));
        return "product-search";
    }

    // ---- Update product ----

    @GetMapping("/update/{id}")
    public String showUpdateProductForm(@PathVariable("id") Long productId, Model model,
            RedirectAttributes redirectAttributes) {
        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND,
                            "Product not found"));
            model.addAttribute("productForm", product);
            model.addAttribute("warehouses", warehouseRepository.findAll());
            return "update-product";
        } catch (ResponseStatusException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getReason());
            return "redirect:/products";
        }
    }

    @PostMapping("/update/{id}")
    public String handleUpdateProduct(@PathVariable("id") Long productId,
            @ModelAttribute("productForm") Product productForm,
            RedirectAttributes redirectAttributes) {
        productForm.setId(productId);
        try {
            productService.updateProduct(productForm);
            redirectAttributes.addFlashAttribute("successMessage", "Product updated!");
        } catch (ResponseStatusException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getReason());
        }
        return "redirect:/products";
    }

    // ---- Delete product ----

    @PostMapping("/delete/{id}")
    public String handleDeleteProduct(@PathVariable("id") Long productId, RedirectAttributes redirectAttributes) {
        productService.deleteProduct(productId);
        redirectAttributes.addFlashAttribute("deleteSuccess", "Product successfully deleted!");
        return "redirect:/products";
    }

    // ---- Cart / purchase ----

    @PostMapping("/purchase/{id}")
    public String handleAddToCart(
            @PathVariable("id") Long productId,
            @RequestParam int quantity,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        User currentUser = userService.getUserByUsername(authentication.getName());
        Long userId = currentUser.getId();
        try {
            cartService.addProductToCart(userId, quantity, productId);
            redirectAttributes.addFlashAttribute("successMessage", "Product successfully added to cart");
        } catch (ResponseStatusException exception) {
            redirectAttributes.addFlashAttribute("outOfStockMessage", exception.getReason());
        }
        return "redirect:/products";
    }

    // ---- Misc ----

    @GetMapping("/home")
    public String showHome() {
        return "home";
    }

}
