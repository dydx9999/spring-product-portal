package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import com.example.demo.model.OrderSummaryDTO;
import com.example.demo.model.User;
import com.example.demo.service.OrderService;
import com.example.demo.service.UserService;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/orders")
public class OrderViewController {
    private final UserService userService;
    private final OrderService orderService;

    public OrderViewController(UserService userService, OrderService orderService) {
        this.userService = userService;
        this.orderService = orderService;
    }

    @GetMapping
    public String showOrders(Model model, Authentication authentication) {
        User currentUser = userService.getUserByUsername(authentication.getName());
        model.addAttribute("username", currentUser.getUsername());
        List<OrderSummaryDTO> orderSummary = orderService.displayOrders(currentUser.getId());
        model.addAttribute("orderSummary", orderSummary);
        return "orders";
    }

    @GetMapping("/view/{orderId}")
    public String showOrderDetails(@PathVariable("orderId") UUID orderId, Model model,
            Authentication authentication) {
        User currentUser = userService.getUserByUsername(authentication.getName());
        model.addAttribute("orderDetails", orderService.getOrderDetails(orderId, currentUser.getId()));

        return "order-view-single";
    }

}
