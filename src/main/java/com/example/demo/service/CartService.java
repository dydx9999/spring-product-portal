package com.example.demo.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.repository.CartRepository;
import com.example.demo.model.Cart;
import com.example.demo.repository.ProductRepository;
import com.example.demo.model.Product;
import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    public Cart addProductToCart(Cart cartProduct) {

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
        return !cartRepository
                .findByUserId(userId).isEmpty();
    }

    public List<Cart> listUserCart(Long userId) {

        try {
            return cartRepository.findByUserId(userId);
        } catch (Exception e) {
            throw new ResponseStatusException((HttpStatus.BAD_REQUEST), "User does not exist!");
        }

    }

    public List<Cart> listCarts() {
        return cartRepository.findAll();
    }
}
