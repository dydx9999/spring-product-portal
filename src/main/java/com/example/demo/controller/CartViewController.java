package com.example.demo.controller;

import com.example.demo.repository.CartRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.model.Cart;
import com.example.demo.model.User;
import com.example.demo.service.CartService;
import com.example.demo.service.OrderService;
import com.example.demo.service.ProductService;
import com.example.demo.service.UserService;
import com.example.demo.model.Product;
import org.springframework.web.bind.annotation.PostMapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.UUID;

@Controller
@RequestMapping("/cart")
public class CartViewController {
    private final CartRepository cartRepository;
    private final CartService cartService;
    private final OrderService orderService;
    private final ProductService productService;
    private final UserService userService;

    public CartViewController(CartService cartService, OrderService orderService, ProductService productService,
            UserService userService, CartRepository cartRepository) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.productService = productService;
        this.userService = userService;
        this.cartRepository = cartRepository;
    }

    @GetMapping("/view")
    public String displayCart(Model model, Authentication authentication) {
        User currentUser = userService.getUserByUsername(authentication.getName());

        List<Cart> cart = cartService.listUserCart(currentUser.getId());
        Map<Long, Product> products = cart.stream()
                .map(cartItem -> productService.searchProductById(cartItem.getProductId()))
                .collect(Collectors.toMap(Product::getId, product -> product, (existing, duplicate) -> existing));

        BigDecimal cartTotal = cart.stream()
                .map(cartItem -> products.get(cartItem.getProductId()).getProductPrice()
                        .multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<Long, Long> distinctCarts = cart.stream()
                .map(Cart::getProductId)
                .distinct()
                .collect(Collectors.toMap(
                        productId -> productId,
                        cartRepository::countDistinctUsersWithProduct));

        model.addAttribute("cart", cart);
        model.addAttribute("products", products);
        model.addAttribute("cartTotal", cartTotal);
        model.addAttribute("distinctCarts", distinctCarts);
        return "cart";

    }

    @PostMapping("/delete/{id}")
    public String deleteCartItem(@PathVariable("id") Long productId, RedirectAttributes redirectAttributes) {
        try {
            cartService.removeCartItem(productId);
        } catch (ResponseStatusException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cart removal failed");
        }
        return "redirect:/cart/view";
    }

    @PostMapping("/update/{id}")
    public String updateCartQuantity(@PathVariable("id") Long productId,
            @ModelAttribute("cartItem") Cart cartItem,
            RedirectAttributes redirectAttributes, Authentication authentication) {
        User currentUser = userService.getUserByUsername(authentication.getName());

        try {
            cartService.updateCartQuantity(productId, cartItem.getQuantity(), currentUser.getId());
        } catch (ResponseStatusException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getReason());
        }

        return "redirect:/cart/view";

    }

    @GetMapping("/checkout")
    public String displayCheckout(Model model, Authentication authentication) {
        User currentUser = userService.getUserByUsername(authentication.getName());

        List<Cart> cart = cartService.listUserCart(currentUser.getId());
        Map<Long, Product> products = cart.stream()
                .map(cartItem -> productService.searchProductById(cartItem.getProductId()))
                .collect(Collectors.toMap(Product::getId, product -> product, (existing, duplicate) -> existing));

        BigDecimal cartTotal = cart.stream()
                .map(cartItem -> products.get(cartItem.getProductId()).getProductPrice()
                        .multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("cart", cart);
        model.addAttribute("products", products);
        model.addAttribute("cartTotal", cartTotal);
        return "checkout";

    }

    @PostMapping("/checkout")
    public String placeOrder(Authentication authentication, RedirectAttributes redirectAttributes) {
        User currentUser = userService.getUserByUsername(authentication.getName());
        UUID orderId = UUID.randomUUID();
        Long userId = currentUser.getId();

        try {
            cartService.processOrder(userId, orderId);
        } catch (ResponseStatusException e) {
            redirectAttributes.addFlashAttribute("checkoutError", e.getReason());
            return "redirect:/cart/checkout";
        }

        return "redirect:/cart/checkout/confirmation/" + orderId;
    }

    @GetMapping("/checkout/confirmation/{orderId}")
    public String displayConfirmation(@PathVariable UUID orderId, Model model, Authentication authentication) {
        User currentUser = userService.getUserByUsername(authentication.getName());
        model.addAttribute("orderDetails", orderService.getOrderDetails(orderId, currentUser.getId()));

        return "order-confirmation";
    }

}
