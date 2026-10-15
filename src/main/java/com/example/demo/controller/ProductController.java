package com.example.demo.controller;

import org.springframework.web.bind.annotation.RestController;

import com.example.demo.repository.ProductRepository;
import com.example.demo.service.ProductService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import java.util.List;

import com.example.demo.model.Product;
import com.example.demo.model.ProductDTO;

import org.springframework.web.bind.annotation.RequestBody;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;
    private final ProductRepository productRepository;

    @GetMapping
    public ResponseEntity<?> getProducts() {
        List<ProductDTO> products = productService.getProductDTO();
        return ResponseEntity.ok(products);
    }

    // @PostMapping
    // public ResponseEntity<?> createProduct(@RequestBody Product newProduct) {

    // try {
    // Product updatedProduct = productService.createProduct(newProduct);
    // return ResponseEntity.ok(productRepository.save(updatedProduct));
    // } catch (Exception e) {
    // return ResponseEntity.badRequest().build();
    // }

    // }

    @PostMapping("/update/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody Product newProduct) {
        newProduct.setId(id);
        Product updatedProduct;

        try {
            updatedProduct = productService.updateProduct(newProduct);
            return ResponseEntity.ok(productRepository.save(updatedProduct));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/filter")
    public List<Product> filterProducts(@RequestParam String filter) {
        if (filter.equals("priceDesc")) {
            return productRepository.findAll(Sort.by(Product::getProductPrice));

        }
        return productRepository.findAll(Sort.by(Product::getProductPrice));

    }

}