package com.example.demo.controller;

import java.util.List;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.demo.model.User;
import com.example.demo.model.Vendor;
import com.example.demo.service.CartService;
import com.example.demo.service.UserService;
import com.example.demo.service.VendorService;
import com.example.demo.service.WishlistService;
import com.example.demo.service.WishlistService.WishlistEntry;

@ControllerAdvice(annotations = Controller.class)
public class GlobalModelAttributes {
    private final VendorService vendorService;
    private final CartService cartService;
    private final UserService userService;
    private final WishlistService wishlistService;

    public GlobalModelAttributes(CartService cartService, UserService userService, VendorService vendorService,
            WishlistService wishlistService) {
        this.cartService = cartService;
        this.userService = userService;
        this.vendorService = vendorService;
        this.wishlistService = wishlistService;
    }

    @ModelAttribute("cartSize")
    public int cartSize(Authentication authentication) {
        return cartService.getCartSizeForCurrentUser(authentication);
    }

    @ModelAttribute
    public void checkIfVendor(Model model, Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken
                || authentication.getAuthorities().stream()
                        .noneMatch(authority -> authority.getAuthority().equals("ROLE_VENDOR"))) {
            return;
        }

        User currentUser = userService.getUserByUsername(authentication.getName());
        Vendor currentVendor = vendorService.getVendorByUserId(currentUser.getId());
        model.addAttribute("businessNameVendor", currentVendor.getBusinessName());
    }

    @ModelAttribute
    public void checkIfUser(Model model, Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken
                || authentication.getAuthorities().stream()
                        .noneMatch(authority -> authority.getAuthority().equals("ROLE_USER"))) {
            return;
        }

        User currentUser = userService.getUserByUsername(authentication.getName());
        List<WishlistEntry> userWishlist = wishlistService.getUserWishlist(currentUser.getId());
        model.addAttribute("wishlistSize", userWishlist.size());
        model.addAttribute("userWishlist", userWishlist.stream()
                .map(WishlistEntry::productId)
                .toList());
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