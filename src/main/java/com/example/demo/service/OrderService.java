package com.example.demo.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.model.BuyerReviewDTO;
import com.example.demo.model.Order;
import com.example.demo.model.OrderDetailsDTO;
import com.example.demo.model.OrderItem;
import com.example.demo.model.OrderItemDTO;
import com.example.demo.model.OrderSummaryDTO;
import com.example.demo.model.PageView;
import com.example.demo.model.Review;
import com.example.demo.model.ReviewFormDTO;
import com.example.demo.model.ShippingDetailsForm;
import com.example.demo.model.VendorPickListDTO;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.repository.VendorRepository;

@Service
public class OrderService {
    private static final Sort NEWEST_ORDERS_FIRST = Sort.by(Sort.Order.desc("creationDate"), Sort.Order.desc("id"));
    private static final Sort NEWEST_REVIEWS_FIRST = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
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

    public PageView<OrderSummaryDTO> displayOrders(Long userId, int page, int size) {
        PageView<Order> orders = PageView.load(page, size, NEWEST_ORDERS_FIRST,
                pageable -> orderRepository.findByBuyerId(userId, pageable));
        Map<Long, BigDecimal> totalsByOrderId = new HashMap<>();
        if (!orders.getContent().isEmpty()) {
            List<Long> orderRowIds = orders.getContent().stream().map(Order::getId).toList();
            for (OrderItem item : orderItemRepository.findByOrderIdIn(orderRowIds)) {
                BigDecimal itemTotal = item.getProductPrice()
                        .multiply(BigDecimal.valueOf(item.getProductQuantity()));
                totalsByOrderId.merge(item.getOrderId(), itemTotal, BigDecimal::add);
            }
        }
        return orders.map(order -> {
            OrderSummaryDTO dto = new OrderSummaryDTO();
            dto.setOrderId(order.getOrderId());
            dto.setBuyerId(order.getBuyerId());
            dto.setVendorId(order.getVendorId());
            dto.setCreationDate(order.getCreationDate());
            dto.setStatus(order.getStatus());
            dto.setTotal(totalsByOrderId.getOrDefault(order.getId(), BigDecimal.ZERO));
            return dto;
        });
    }

    public PageView<OrderSummaryDTO> getVendorSales(Long vendorId, int page, int size) {
        return getVendorSales(vendorId, null, page, size);
    }

    public PageView<OrderSummaryDTO> getVendorSales(Long vendorId, String status, int page, int size) {
        String statusFilter = status == null || status.isBlank() ? null : status.trim();
        PageView<Order> orders = PageView.load(page, size, NEWEST_ORDERS_FIRST,
                pageable -> statusFilter == null
                        ? orderRepository.findByVendorId(vendorId, pageable)
                        : orderRepository.findByVendorIdAndStatusIgnoreCase(vendorId, statusFilter, pageable));
        Map<Long, List<OrderItem>> itemsByOrderId = new HashMap<>();
        if (!orders.getContent().isEmpty()) {
            for (OrderItem item : orderItemRepository
                    .findByOrderIdIn(orders.getContent().stream().map(Order::getId).toList())) {
                itemsByOrderId.computeIfAbsent(item.getOrderId(), k -> new ArrayList<>()).add(item);
            }
        }
        return orders.map(order -> {
            List<OrderItem> orderItems = itemsByOrderId.getOrDefault(order.getId(), List.of());
            OrderSummaryDTO dto = new OrderSummaryDTO(order.getOrderId(), order.getBuyerId(),
                    order.getVendorId(), order.getCreationDate(), order.getStatus());
            dto.setTrackingNumber(order.getTrackingNumber());
            dto.setCarrier(order.getCarrier());
            dto.setItems(orderItems.stream()
                    .map(i -> new OrderItemDTO(i.getProductName(), i.getProductQuantity(), i.getProductPrice(), null))
                    .toList());
            dto.setTotal(orderItems.stream()
                    .map(i -> i.getProductPrice().multiply(BigDecimal.valueOf(i.getProductQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            return dto;
        });
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
        String status = orders.get(0).getStatus();
        String carrier = orders.get(0).getCarrier();
        String trackingNumber = orders.get(0).getTrackingNumber();
        BigDecimal total = items.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new OrderDetailsDTO(orderId, creationDate, items, total, status, trackingNumber, carrier);
    }

    public Set<Long> getReviewedItemIds(Long buyerId, List<Long> orderItemIds) {
        if (orderItemIds.isEmpty()) {
            return Set.of();
        }
        return reviewRepository.findByBuyerIdAndOrderItemIdIn(buyerId, orderItemIds).stream()
                .map(Review::getOrderItemId)
                .collect(Collectors.toSet());
    }

    public PageView<BuyerReviewDTO> getReviewedItemsByBuyer(Long buyerId, int page, int size) {
        PageView<Review> reviews = PageView.load(page, size, NEWEST_REVIEWS_FIRST,
                pageable -> reviewRepository.findByBuyerId(buyerId, pageable));
        Map<Long, OrderItem> orderItemsById = orderItemRepository.findAllById(reviews.getContent().stream()
                .map(Review::getOrderItemId)
                .distinct()
                .toList()).stream()
                .collect(Collectors.toMap(OrderItem::getId, Function.identity()));
        return reviews.map(review -> {
            OrderItem item = orderItemsById.get(review.getOrderItemId());
            String productName = item == null ? "Product unavailable" : item.getProductName();
            Long productId = item == null ? null : item.getProductId();
            return new BuyerReviewDTO(productId, productName, review.getRating(), review.getCreatedAt());
        });
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
        reviewRepository.save(new Review(orderItem.getId(), buyerId, reviewForm.getRating(), reviewForm.getComment()));
    }

    @Transactional
    public void updateStatus(UUID orderId, Long vendorId, String newStatus) {
        List<String> validStatusList = List.of("Processing", "Delivered", "Shipped", "Delivered", "Accepted");
        if (!validStatusList.contains(newStatus)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid order status");
        }
        Order buyerOrder = orderRepository.findByOrderIdAndVendorId(orderId, vendorId);
        if (buyerOrder == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to retrieve orders for specified buyer.");
        }
        String currentStatus = buyerOrder.getStatus();
        if (currentStatus.equals(newStatus)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No change.");
        }
        buyerOrder.setStatus(newStatus);
    }

    @Transactional
    public void updateTracking(Long vendorId, ShippingDetailsForm shippingDetailsForm) {
        if (shippingDetailsForm == null
                || shippingDetailsForm.getOrderId() == null
                || shippingDetailsForm.getTrackingNumber() == null
                || shippingDetailsForm.getTrackingNumber().isBlank()
                || shippingDetailsForm.getCarrier() == null
                || shippingDetailsForm.getCarrier().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A valid order and tracking details are required");
        }
        Order buyerOrder = orderRepository.findByOrderIdAndVendorId(shippingDetailsForm.getOrderId(), vendorId);
        if (buyerOrder == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
        }
        if (!"Accepted".equalsIgnoreCase(buyerOrder.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Shipping details can only be added to accepted orders");
        }
        buyerOrder.setTrackingNumber(shippingDetailsForm.getTrackingNumber().trim());
        buyerOrder.setCarrier(shippingDetailsForm.getCarrier().trim());
        buyerOrder.setStatus("Shipped");
    }

    public List<VendorPickListDTO> getVendorPickList(UUID orderId, Long vendorId) {
        List<VendorPickListDTO> pickList = orderItemRepository.findVendorPickListByOrderId(orderId, vendorId);
        if (pickList == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No matching item pick list for provided order!");
        }
        return pickList;

    }
}
