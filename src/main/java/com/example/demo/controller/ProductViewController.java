package com.example.demo.controller;

import com.example.demo.repository.ProductRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.example.demo.service.ProductService;
import com.example.demo.model.Product;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import com.example.demo.model.User;
import com.example.demo.service.UserService;

@Controller
@RequestMapping("/products")
public class ProductViewController {
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final UserService userService;

    public ProductViewController(ProductService productService, UserService userService,
            ProductRepository productRepository) {
        this.productService = productService;
        this.userService = userService;
        this.productRepository = productRepository;
    }

    // GET - products.html
    @GetMapping
    public String listProducts(Model model, Authentication authentication) {
        User currentUser = userService.getUserByUsername(authentication.getName());

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);
        boolean isUser = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"));
        model.addAttribute("isUser", isUser);

        model.addAttribute("products", productService.getProducts());
        model.addAttribute("username", currentUser.getUsername());
        model.addAttribute("productCount", productRepository.countProducts());

        return "products";
    }

    // Render - add-product.html
    @GetMapping("/add-product")
    public String showProductForm(Model model) {
        model.addAttribute("productForm", new Product());
        return "add-product";
    }

    // Render - product-search.html
    @GetMapping("/product-search")
    public String showProductSearch(Model model) {
        model.addAttribute("productSearchForm", new Product());
        return "product-search";
    }

    // Form handling - add-product.html
    @PostMapping("/add-product")
    public String addProducts(@ModelAttribute("productForm") Product productForm,
            RedirectAttributes redirectAttributes) {
        boolean exists = productService.checkProductExists(productForm.getProductName());
        if (exists) {
            redirectAttributes.addFlashAttribute("errorMessage", "Product already exists");
        } else {
            productService.createProduct(productForm);
            redirectAttributes.addFlashAttribute("successMessage", "Product successfully created");
        }
        return "redirect:/products/add-product";
    }

    // Form handling - product-search.html
    @PostMapping("/product-search")
    public String searchProducts(@ModelAttribute("productSearchForm") Product productSearchForm, Model model) {
        model.addAttribute("searchResults", productService.searchProductsByName(productSearchForm.getProductName()));
        return "product-search";
    }

    // Form Handling - products.html - Purchase
    @PostMapping("/purchase/{id}")
    public String purchaseProduct(@PathVariable("id") Long productId, RedirectAttributes redirectAttributes) {
        boolean exists = productService.checkProductExistsById(productId);
        boolean isInStock = productService.productInStock(productId);
        if (!isInStock) {
            redirectAttributes.addFlashAttribute("outOfStockMessage", "Product is out of stock!");
        }
        if (exists && isInStock) {
            try {
                productService.updateProductQuantity(productId);
                redirectAttributes.addFlashAttribute("successMessage", "Product successfully added to cart");
            } catch (ResponseStatusException exception) {
                redirectAttributes.addFlashAttribute("outOfStockMessage", exception.getReason());
            }
        }
        return "redirect:/products";
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long productId, RedirectAttributes redirectAttributes) {
        productService.deleteProduct(productId);
        redirectAttributes.addFlashAttribute("deleteSuccess", "Product successfully deleted!");
        return "redirect:/products";
    }
}