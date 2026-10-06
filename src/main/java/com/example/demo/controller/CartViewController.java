package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.example.demo.service.CartService;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import com.example.demo.service.ProductService;
import com.example.demo.service.UserService;
import com.example.demo.model.User;
import org.springframework.security.core.Authentication;

@Controller
@RequestMapping("/cart/view")
public class CartViewController {
    private final CartService cartService;
    private final ProductService productService;
    private final UserService userService;

    public CartViewController(CartService cartService, ProductService productService, UserService userService) {
        this.cartService = cartService;
        this.productService = productService;
        this.userService = userService;
    }

    @GetMapping
    public String displayCart(Model model, Authentication authentication) {
        User currentUser = userService.getUserByUsername(authentication.getName());

        model.addAttribute("currentUser", currentUser.getUsername());
        model.addAttribute("cart", cartService.listUserCart(currentUser.getId()));
        return "cart";
    }

}
