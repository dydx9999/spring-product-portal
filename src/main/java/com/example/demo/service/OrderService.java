package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.model.Order;
import com.example.demo.model.OrderDetailsDTO;
import com.example.demo.model.OrderItemDTO;
import com.example.demo.model.OrderSummaryDTO;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.repository.OrderRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public List<OrderSummaryDTO> displayOrders(Long userId) {
        return orderRepository.findByBuyerIdOrderByCreationDateDesc(userId)
                .stream()
                .map(order -> {
                    OrderSummaryDTO dto = new OrderSummaryDTO();
                    dto.setOrderId(order.getOrderId());
                    dto.setBuyerId(order.getBuyerId());
                    dto.setVendorId(order.getVendorId());
                    dto.setCreationDate(order.getCreationDate());
                    return dto;
                })
                .toList();
    }

    public OrderDetailsDTO getOrderDetails(UUID orderId, Long buyerId) {
        List<Order> orders = orderRepository.findByOrderIdAndBuyerId(orderId, buyerId);
        if (orders.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
        }

        List<Long> orderRowIds = orders.stream().map(Order::getId).toList();
        List<OrderItemDTO> items = orderItemRepository.findByOrderIdIn(orderRowIds).stream()
                .map(item -> new OrderItemDTO(item.getProductName(), item.getProductQuantity(), item.getProductPrice()))
                .toList();
        LocalDateTime creationDate = orders.get(0).getCreationDate();
        return new OrderDetailsDTO(orderId, creationDate, items);
    }

}
