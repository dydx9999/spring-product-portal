package com.example.demo.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.model.Order;
import com.example.demo.model.OrderDetailsDTO;
import com.example.demo.model.OrderItem;
import com.example.demo.model.OrderItemDTO;
import com.example.demo.model.OrderSummaryDTO;
import com.example.demo.model.Review;
import com.example.demo.model.ReviewFormDTO;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.repository.VendorRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final VendorRepository vendorRepository;
    private final ReviewRepository reviewRepository;

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
            VendorRepository vendorRepository, ReviewRepository reviewRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.vendorRepository = vendorRepository;
        this.reviewRepository = reviewRepository;
    }

    public List<OrderSummaryDTO> displayOrders(Long userId) {
        List<Order> orders = orderRepository.findByBuyerIdOrderByCreationDateDesc(userId);
        Map<Long, BigDecimal> totalsByOrderId = new HashMap<>();
        if (!orders.isEmpty()) {
            List<Long> orderRowIds = orders.stream().map(Order::getId).toList();
            for (OrderItem item : orderItemRepository.findByOrderIdIn(orderRowIds)) {
                BigDecimal itemTotal = item.getProductPrice()
                        .multiply(BigDecimal.valueOf(item.getProductQuantity()));
                totalsByOrderId.merge(item.getOrderId(), itemTotal, BigDecimal::add);
            }
        }

        return orders.stream()
                .map(order -> {
                    OrderSummaryDTO dto = new OrderSummaryDTO();
                    dto.setOrderId(order.getOrderId());
                    dto.setBuyerId(order.getBuyerId());
                    dto.setVendorId(order.getVendorId());
                    dto.setCreationDate(order.getCreationDate());
                    dto.setTotal(totalsByOrderId.getOrDefault(order.getId(), BigDecimal.ZERO));
                    return dto;
                })
                .toList();
    }

    public List<OrderSummaryDTO> getVendorSales(Long vendorId) {
        List<Order> orders = orderRepository.findByVendorId(vendorId);
        if (orders.isEmpty()) {
            return List.of();
        }
        Map<Long, List<OrderItem>> itemsByOrderId = new HashMap<>();
        for (OrderItem item : orderItemRepository.findByOrderIdIn(orders.stream().map(Order::getId).toList())) {
            itemsByOrderId.computeIfAbsent(item.getOrderId(), k -> new ArrayList<>()).add(item);
        }

        return orders.stream().map(order -> {
            List<OrderItem> orderItems = itemsByOrderId.getOrDefault(order.getId(), List.of());
            OrderSummaryDTO dto = new OrderSummaryDTO(order.getOrderId(), order.getBuyerId(),
                    order.getVendorId(), order.getCreationDate());
            dto.setItems(orderItems.stream()
                    .map(i -> new OrderItemDTO(i.getProductName(), i.getProductQuantity(), i.getProductPrice(), null))
                    .toList());
            dto.setTotal(orderItems.stream()
                    .map(i -> i.getProductPrice().multiply(BigDecimal.valueOf(i.getProductQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            return dto;
        }).toList();
    }

    public OrderDetailsDTO getOrderDetails(UUID orderId, Long buyerId) {
        List<Order> orders = orderRepository.findByOrderIdAndBuyerId(orderId, buyerId);
        if (orders.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
        }

        List<Long> orderRowIds = orders.stream().map(Order::getId).toList();
        Map<Long, String> vendorBusinessNamesById = new HashMap<>();
        vendorRepository.findAllById(orders.stream()
                .map(Order::getVendorId)
                .filter(vendorId -> vendorId != null)
                .distinct()
                .toList())
                .forEach(vendor -> vendorBusinessNamesById.put(vendor.getId(), vendor.getBusinessName()));
        Map<Long, String> vendorBusinessNamesByOrderRowId = new HashMap<>();
        for (Order order : orders) {
            vendorBusinessNamesByOrderRowId.put(order.getId(), vendorBusinessNamesById.get(order.getVendorId()));
        }
        List<OrderItemDTO> items = orderItemRepository.findByOrderIdIn(orderRowIds).stream()
                .map(item -> {
                    OrderItemDTO itemDTO = new OrderItemDTO(item.getProductName(), item.getProductQuantity(),
                            item.getProductPrice(), vendorBusinessNamesByOrderRowId.get(item.getOrderId()));
                    itemDTO.setId(item.getId());
                    return itemDTO;
                })
                .toList();
        LocalDateTime creationDate = orders.get(0).getCreationDate();
        BigDecimal total = items.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new OrderDetailsDTO(orderId, creationDate, items, total);
    }

    @Transactional
    public void saveReview(ReviewFormDTO reviewForm, Long buyerId) {
        if (reviewForm == null || reviewForm.getOrderId() == null || reviewForm.getOrderItemId() == null
                || reviewForm.getRating() < 1 || reviewForm.getRating() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A valid order item and rating are required");
        }

        List<Order> buyerOrders = orderRepository.findByOrderIdAndBuyerId(reviewForm.getOrderId(), buyerId);
        if (buyerOrders.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
        }

        OrderItem orderItem = orderItemRepository.findById(reviewForm.getOrderItemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order item not found"));
        boolean itemBelongsToOrder = buyerOrders.stream()
                .anyMatch(order -> order.getId().equals(orderItem.getOrderId()));
        if (!itemBelongsToOrder) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order item not found");
        }
        if (reviewRepository.existsByOrderItemIdAndBuyerId(orderItem.getId(), buyerId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You have already reviewed this item");
        }

        reviewRepository.save(new Review(orderItem.getId(), buyerId, reviewForm.getRating()));
    }

}
