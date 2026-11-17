package com.example.demo.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.example.demo.model.Product;
import com.example.demo.model.User;
import com.example.demo.model.Vendor;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.service.OrderService;
import com.example.demo.service.UserService;
import com.example.demo.service.VendorService;
import com.example.demo.service.WarehouseService;

class VendorDiscountTest {
    @Test
    void capsNewDiscountsAt99Percent() {
        ProductRepository products = mock(ProductRepository.class);
        UserService users = mock(UserService.class);
        VendorService vendors = mock(VendorService.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("vendor");
        when(users.getUserByUsername("vendor")).thenReturn(new User(1L, "vendor", "", BigDecimal.ZERO));
        when(vendors.getVendorByUserId(1L)).thenReturn(new Vendor(2L, 1L, 0, BigDecimal.ZERO, "Vendor", null));
        Product product = new Product(3L, "Product", new BigDecimal("10.00"), 10, "sku", "Vendor", null, 1L, 0, 0);
        when(products.findById(3L)).thenReturn(Optional.of(product));
        VendorViewController controller = new VendorViewController(vendors, users, products, mock(OrderService.class),
                mock(WarehouseService.class), mock(OrderItemRepository.class));
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        controller.handleDiscountsManagerForm(3L, 100, authentication, redirect, new ExtendedModelMap());
        assertEquals("Discount must be between 0 and 99%.", redirect.getFlashAttributes().get("errorMessage"));
        verify(products, never()).save(product);

        controller.handleDiscountsManagerForm(3L, 99, authentication, new RedirectAttributesModelMap(),
                new ExtendedModelMap());
        assertEquals(99, product.getDiscountPercent());
        verify(products).save(product);

        controller.updateProductPrice(3L, "8.00", authentication, new RedirectAttributesModelMap());
        assertEquals(new BigDecimal("8.00"), product.getProductPrice());
        assertEquals(99, product.getDiscountPercent());
    }
}
