package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.model.OrderSummaryDTO;
import com.example.demo.model.ReviewFormDTO;
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

    @GetMapping("/review")
    public String showReviewForm(@RequestParam(required = true) UUID orderId, Model model,
            Authentication authentication) {
        User currentUser = userService.getUserByUsername(authentication.getName());
        model.addAttribute("orderDetails", orderService.getOrderDetails(orderId, currentUser.getId()));
        ReviewFormDTO reviewForm = new ReviewFormDTO();
        reviewForm.setOrderId(orderId);
        model.addAttribute("reviewForm", reviewForm);
        return "order-review-form";
    }

    @PostMapping("/review")
    public String handleReviewForm(ReviewFormDTO reviewForm, Authentication authentication,
            RedirectAttributes redirectAttributes) {
        User currentUser = userService.getUserByUsername(authentication.getName());
        try {
            orderService.saveReview(reviewForm, currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Review has been successfully saved!");
        } catch (ResponseStatusException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getReason());
        }

        return "redirect:/orders/view/" + reviewForm.getOrderId();
    }

}
