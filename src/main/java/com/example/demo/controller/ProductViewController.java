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

    @GetMapping
    public String listProducts(Model model) {
        model.addAttribute("products", productService.getProducts());
        return "products";
    }

    // Form handling - add-product.html
    @PostMapping("/product-form")
    public String addProducts(@ModelAttribute("productForm") Product productForm) {
        productService.createProduct(productForm);
        return "redirect:/products";
    }

    // Render add-product page
    @GetMapping("/product-form")
    public String showProductForm(Model model) {
        model.addAttribute("productForm", new Product());
        return "add-product";
    }

    // Product search form results product-search.html
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
