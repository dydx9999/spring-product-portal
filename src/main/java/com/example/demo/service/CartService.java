package com.example.demo.service;

import com.example.demo.repository.OrderRepository;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.ArrayList;
import java.util.Map;
import com.example.demo.model.Cart;
import com.example.demo.model.Order;
import com.example.demo.model.Product;
import com.example.demo.model.OrderItem;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.VendorRepository;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.model.Vendor;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.LinkedHashMap;

@Service
public class CartService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final VendorRepository vendorRepository;
    private final OrderItemRepository orderItemRepository;

    public CartService(CartRepository cartRepository, ProductRepository productRepository,
            OrderRepository orderRepository, VendorRepository vendorRepository,
            OrderItemRepository orderItemRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.vendorRepository = vendorRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional
    public Cart addProductToCart(Long userId, int quantity, Long productId) {

        if (quantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be positive");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        String vendorName = product.getVendorName();
        List<Cart> existingItems = cartRepository.findByProductIdUserId(product.getId(), userId);
        int combinedQuantity = existingItems.stream().mapToInt(Cart::getQuantity).sum() + quantity;
        if (product.getProductQuantity() < combinedQuantity) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient stock");
        }

        if (!existingItems.isEmpty()) {
            Cart existingItem = existingItems.get(0);
            existingItem.setQuantity(combinedQuantity);
            existingItem.setVendorName(vendorName);
            Cart savedItem = cartRepository.save(existingItem);
            if (existingItems.size() > 1) {
                cartRepository.deleteAll(existingItems.subList(1, existingItems.size()));
            }
            return savedItem;
        }

        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setProductId(product.getId());
        cart.setVendorName(vendorName);
        cart.setQuantity(quantity);
        return cartRepository.save(cart);
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
