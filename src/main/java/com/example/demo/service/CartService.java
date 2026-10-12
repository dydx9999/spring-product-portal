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

    @Transactional
    public void removeCartItem(Long productId) {

        try {
            cartRepository.deleteByProductId(productId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product does not exist!");
        }
    }

    @Transactional
    public void updateCartQuantity(Long productId, int quantity, Long userId) {
        if (quantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be positive");
        }

        List<Cart> matchingItems = cartRepository.findByProductIdUserId(productId, userId);
        if (matchingItems.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found");
        }
        Cart cart = matchingItems.get(0);
        int currentQuantity = matchingItems.stream().mapToInt(Cart::getQuantity).sum();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        int quantityDifference = quantity - currentQuantity;
        if (quantityDifference > product.getProductQuantity()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient stock");
        }

        product.setProductQuantity(product.getProductQuantity() - quantityDifference);
        cart.setQuantity(quantity);
        productRepository.save(product);
        cartRepository.save(cart);
        if (matchingItems.size() > 1) {
            cartRepository.deleteAll(matchingItems.subList(1, matchingItems.size()));
        }
    }

    @Transactional
    public void processOrder(Long userId, UUID orderId) {
        List<Cart> cart = cartRepository.findByUserId(userId);
        if (cart.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart is empty");
        }

        Map<Long, List<OrderItem>> orderItemsByVendorId = new LinkedHashMap<>();
        for (Cart cartItem : cart) {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
            if (product.getVendorName() == null || product.getVendorName().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "A cart product is not associated with a vendor");
            }

            List<Vendor> matchingVendors = vendorRepository
                    .findAllByBusinessName(product.getVendorName());
            if (matchingVendors.size() != 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Could not identify a unique vendor for a cart product");
            }
            Long vendorId = matchingVendors.get(0).getId();
            OrderItem orderItem = new OrderItem(null, null, product.getProductPrice(),
                    product.getProductName(), cartItem.getQuantity());
            orderItemsByVendorId.computeIfAbsent(vendorId, ignored -> new ArrayList<>())
                    .add(orderItem);
        }

        LocalDateTime orderCreatedAt = LocalDateTime.now();
        List<Order> orders = orderItemsByVendorId.keySet().stream()
                .map(vendorId -> new Order(null, orderId, userId, vendorId, orderCreatedAt))
                .toList();

        List<Order> savedOrders = orderRepository.saveAll(orders);
        List<OrderItem> orderItems = new ArrayList<>();
        for (Order order : savedOrders) {
            List<OrderItem> vendorOrderItems = orderItemsByVendorId.get(order.getVendorId());
            for (OrderItem orderItem : vendorOrderItems) {
                orderItem.setOrderId(order.getId());
            }
            orderItems.addAll(vendorOrderItems);
        }
        orderItemRepository.saveAll(orderItems);
        cartRepository.deleteAll(cart);
    }
}
