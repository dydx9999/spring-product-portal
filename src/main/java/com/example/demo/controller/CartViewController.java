package com.example.demo.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.demo.dto.OrderDetailsDTO;
import com.example.demo.model.Cart;
import com.example.demo.model.Product;
import com.example.demo.model.User;
import com.example.demo.repository.CartRepository;
import com.example.demo.service.CartService;
import com.example.demo.service.CheckoutFulfillmentService;
import com.example.demo.service.OrderService;
import com.example.demo.service.ProductService;
import com.example.demo.service.StripeCheckoutService;
import com.example.demo.service.UserService;
import com.stripe.exception.StripeException;

@Controller
@RequestMapping("/cart")
public class CartViewController {
    private final CartRepository cartRepository;
    private final CartService cartService;
    private final OrderService orderService;
    private final ProductService productService;
    private final UserService userService;
    private final BigDecimal salesTaxRate;
    private final StripeCheckoutService stripeCheckoutService;
    private final CheckoutFulfillmentService checkoutFulfillmentService;
    private static final Logger log = LoggerFactory.getLogger(CartViewController.class);

    public CartViewController(CartService cartService, OrderService orderService, ProductService productService,
            UserService userService, CartRepository cartRepository,
            @Value("${app.sales-tax-rate}") BigDecimal salesTaxRate, StripeCheckoutService stripeCheckoutService,
            CheckoutFulfillmentService checkoutFulfillmentService) {
        this.stripeCheckoutService = stripeCheckoutService;
        this.checkoutFulfillmentService = checkoutFulfillmentService;
        this.cartService = cartService;
        this.orderService = orderService;
        this.productService = productService;
        this.userService = userService;
        this.cartRepository = cartRepository;
        this.salesTaxRate = salesTaxRate;
    }

    private void addCartTotals(Model model, BigDecimal subtotal) {
        BigDecimal salesTax = subtotal.multiply(salesTaxRate).setScale(2, RoundingMode.HALF_UP);
        model.addAttribute("cartSubtotal", subtotal);
        model.addAttribute("salesTax", salesTax);
        model.addAttribute("cartTotal", subtotal.add(salesTax));
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
        addCartTotals(model, cartTotal);
        model.addAttribute("distinctCarts", distinctCarts);
        model.addAttribute("cartSize", cart.size());
        return "cart";

    }

    @PostMapping("/delete/{id}")
    public String deleteCartItem(@PathVariable("id") Long productId, RedirectAttributes redirectAttributes,
            Authentication authentication) {
        User currentUser = userService.getUserByUsername(authentication.getName());
        Long userId = currentUser.getId();
        try {
            cartService.removeCartItem(productId, userId);
            redirectAttributes.addFlashAttribute("successMessage", "Cart item successfully removed!");
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
            redirectAttributes.addFlashAttribute("successMessage", "Item quantity successfully updated!");
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
        addCartTotals(model, cartTotal);
        return "cart-checkout";

    }

    @PostMapping("/checkout")
    public String placeOrder(Authentication authentication, RedirectAttributes redirectAttributes) {
        User currentUser = userService.getUserByUsername(authentication.getName());
        UUID orderId = UUID.randomUUID();
        Long userId = currentUser.getId();

        try {
            cartService.processOrder(userId, orderId);
            OrderDetailsDTO order = orderService.getOrderDetails(orderId, userId);
            BigDecimal salesTax = order.getTotal().multiply(salesTaxRate).setScale(2, RoundingMode.HALF_UP);
            String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString();
            return "redirect:" + stripeCheckoutService.createCheckoutSessionUrl(order, salesTax, baseUrl);
        } catch (ResponseStatusException e) {
            redirectAttributes.addFlashAttribute("checkoutError", e.getReason());
        } catch (StripeException e) {
            log.error("Stripe session creation failed for order {}", orderId, e);
            redirectAttributes.addFlashAttribute("checkoutError", "Unable to start payment. Please try again.");
        }
        return "redirect:/cart/checkout";
    }

    @GetMapping("/checkout/confirmation/{orderId}")
    public String displayConfirmation(@PathVariable UUID orderId,
            @RequestParam(name = "session_id", required = false) String sessionId,
            Model model, Authentication authentication) {
        User currentUser = userService.getUserByUsername(authentication.getName());
        // Ownership check first so users can't trigger fulfillment for someone else's
        // order
        orderService.getOrderDetails(orderId, currentUser.getId());
        if (sessionId != null && !sessionId.isBlank()) {
            checkoutFulfillmentService.fulfillSession(orderId, sessionId);
        }
        model.addAttribute("orderDetails", orderService.getOrderDetails(orderId, currentUser.getId()));

        return "order-confirmation";
    }

}
