package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.example.demo.service.CartService;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/cart")
public class CartViewController {
    private final CartService cartService;

    public CartViewController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public String showCart(Model model) {
        return "cart";

    }

}
