package com.example.demo.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.demo.service.CartService;

@ControllerAdvice(annotations = Controller.class)
public class GlobalModelAttributes {
    private final CartService cartService;

    public GlobalModelAttributes(CartService cartService) {
        this.cartService = cartService;
    }

    @ModelAttribute("cartSize")
    public int cartSize(Authentication authentication) {
        return cartService.getCartSizeForCurrentUser(authentication);
    }

    @ModelAttribute
    public void addSignedInUser(Model model, Authentication authentication) {
        boolean signedIn = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);

        model.addAttribute("signedIn", signedIn);
        if (signedIn) {
            model.addAttribute("signedInUsername", authentication.getName());
            model.addAttribute("signedInRole", authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(authority -> authority.startsWith("ROLE_"))
                    .map(authority -> authority.substring("ROLE_".length()))
                    .findFirst()
                    .orElse("USER"));
        }
    }

}