package com.example.demo.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.dto.OrderDetailsDTO;
import com.example.demo.dto.OrderSummaryDTO;
import com.example.demo.dto.ReviewFormDTO;
import com.example.demo.model.PageView;
import com.example.demo.model.User;
import com.example.demo.service.OrderService;
import com.example.demo.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

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
    public String showOrders(@RequestParam(defaultValue = "1") int page, HttpServletRequest request,
            Model model, Authentication authentication) {
        User currentUser = userService.getUserByUsername(authentication.getName());
        model.addAttribute("username", currentUser.getUsername());
        PageView<OrderSummaryDTO> orderPager = orderService
                .displayOrders(currentUser.getId(), page, PageView.DEFAULT_SIZE)
                .withLinks(request, "page");
        model.addAttribute("orderSummary", orderPager.getContent());
        model.addAttribute("orderPager", orderPager);
        return "orders";
    }

    @GetMapping("/view/{orderId}")
    public String showOrderDetails(
            @PathVariable("orderId") UUID orderId,
            Model model,
            Authentication authentication) {
        User currentUser = userService.getUserByUsername(authentication.getName());

        OrderDetailsDTO orderDetails = orderService.getOrderDetails(orderId, currentUser.getId());
        model.addAttribute("orderDetails", orderDetails);
        List<Long> orderItemIds = orderDetails.getItems().stream()
                .map(item -> item.getId())
                .toList();
        model.addAttribute("reviewedItemIds",
                orderService.getReviewedItemIds(currentUser.getId(), orderItemIds));

        return "order-view-single";
    }

    @GetMapping("/review")
    public String showReviewForm(@RequestParam(required = true) UUID orderId, Model model,
            Authentication authentication) {
        User currentUser = userService.getUserByUsername(authentication.getName());
        OrderDetailsDTO orderDetails = orderService.getOrderDetails(orderId, currentUser.getId());
        model.addAttribute("orderDetails", orderDetails);
        List<Long> orderItemIds = orderDetails.getItems().stream()
                .map(item -> item.getId())
                .toList();
        model.addAttribute("reviewedItemIds",
                orderService.getReviewedItemIds(currentUser.getId(), orderItemIds));
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
