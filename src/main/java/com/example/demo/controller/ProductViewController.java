package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.example.demo.service.ProductService;
import com.example.demo.model.Product;

@Controller
@RequestMapping("/products")
public class ProductViewController {
    private final ProductService productService;

    public ProductViewController(ProductService productService) {
        this.productService = productService;
    }

    // Render - Product Catalog Entries
    @GetMapping
    public String listProducts(Model model) {
        model.addAttribute("products", productService.getProducts());
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
        boolean exists = !productService.searchProductsByName(productForm.getProductName()).isEmpty();
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

    // Display search products page
    @GetMapping("/product-search")
    public String showProductSearch(Model model) {
        model.addAttribute("productSearchForm", new Product());
        return "product-search";
    }

}
