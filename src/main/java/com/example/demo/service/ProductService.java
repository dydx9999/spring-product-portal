package com.example.demo.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.model.Product;
import com.example.demo.repository.ProductRepository;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product createProduct(Product newProduct) {
        String requestProductName = newProduct.getProductName();
        BigDecimal requestProductPrice = newProduct.getProductPrice();
        int requestProductQuantity = newProduct.getProductQuantity();

        newProduct.setProductName(requestProductName);
        newProduct.setProductPrice(requestProductPrice);
        newProduct.setProductQuantity(requestProductQuantity);
        productRepository.save(newProduct);
        return newProduct;

    }

    public Product updateProduct(Product newProduct) {
        Long requestId = newProduct.getId();

        Product existingProduct = productRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found"));

        existingProduct.setProductName(newProduct.getProductName());
        existingProduct.setProductPrice(newProduct.getProductPrice());
        existingProduct.setProductQuantity(newProduct.getProductQuantity());

        return productRepository.save(existingProduct);
    }

    public List<Product> getProducts() {
        return productRepository.findAll();
    }

    public List<Product> searchProductsByName(String productName) {
        return productRepository.findByProductNameContainingIgnoreCase(productName == null ? "" : productName.trim());
    }

}
