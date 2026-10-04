package com.example.demo.service;

import org.springframework.stereotype.Service;

import com.example.demo.repository.CartRepository;
import com.example.demo.model.Cart;
import com.example.demo.repository.ProductRepository;
import com.example.demo.model.Product;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    public Cart addCartProduct(Cart cartProduct) {

        Long productId = cartProduct.getProductId();
        int productQuantity = cartProduct.getQuantity();

        Product updatedProduct = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found for id: " + productId));

        boolean productAvailable = updatedProduct.getProductQuantity() >= productQuantity;

        if (productAvailable) {
            updatedProduct.setProductQuantity(updatedProduct.getProductQuantity() - productQuantity);
            productRepository.save(updatedProduct);
        }

        return cartProduct;
    }

    public boolean cartExists(Long userId) {
        return (cartRepository
                .findByUserId(userId) != null);

    }
}
