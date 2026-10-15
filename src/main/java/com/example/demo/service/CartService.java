package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.example.demo.model.Cart;
import com.example.demo.model.Order;
import com.example.demo.model.OrderItem;
import com.example.demo.model.Product;
import com.example.demo.model.User;
import com.example.demo.model.Vendor;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.VendorRepository;

@Service
public class CartService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final VendorRepository vendorRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserService userService;

    public CartService(CartRepository cartRepository, ProductRepository productRepository,
            OrderRepository orderRepository, VendorRepository vendorRepository,
            OrderItemRepository orderItemRepository, UserService userService) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.vendorRepository = vendorRepository;
        this.orderItemRepository = orderItemRepository;
        this.userService = userService;
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
        int combinedQuantity = existingItems
                .stream()
                .mapToInt(Cart::getQuantity)
                .sum() + quantity;
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
    public void removeCartItem(Long productId, Long userId) {

        try {
            cartRepository.deleteByProductIdAndUserId(productId, userId);
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

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        if (quantity > product.getProductQuantity()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient stock");
        }

        cart.setQuantity(quantity);
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
            OrderItem orderItem = new OrderItem(null,
                    null,
                    product.getId(),
                    product.getProductPrice(),
                    product.getProductName(),
                    cartItem.getQuantity());
            orderItemsByVendorId.computeIfAbsent(vendorId, ignored -> new ArrayList<>())
                    .add(orderItem);
        }

        LocalDateTime orderCreatedAt = LocalDateTime.now();
        List<Order> orders = orderItemsByVendorId.keySet().stream()
                .map(vendorId -> new Order(null, orderId, userId, vendorId, orderCreatedAt, "Pending"))
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
        Map<Long, Integer> productQuantitiesToDecrease = cart.stream()
                .collect(Collectors.toMap(
                        Cart::getProductId,
                        Cart::getQuantity,
                        Integer::sum));
        for (Map.Entry<Long, Integer> entry : productQuantitiesToDecrease.entrySet()) {
            int updatedRows = productRepository.decreaseStockIfAvailable(
                    entry.getKey(), entry.getValue());

            if (updatedRows != 1) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "Insufficient stock");
            }
        }
        orderItemRepository.saveAll(orderItems);
        cartRepository.deleteAll(cart);
    }

    public int getCartSizeForCurrentUser(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return 0;
        }

        User currentUser = userService.getUserByUsername(authentication.getName());

        Long userId = currentUser.getId();

        List<Cart> userCart = cartRepository.findByUserId(userId);

        return userCart.size();

    }
}
