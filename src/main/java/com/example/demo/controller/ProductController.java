package com.example.demo.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Product;
import com.example.demo.model.ProductDTO;
import com.example.demo.model.User;
import com.example.demo.model.Vendor;
import com.example.demo.model.VendorPickList;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.service.ProductService;
import com.example.demo.service.UserService;
import com.example.demo.service.VendorService;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final OrderItemRepository orderItemRepository;
    private final ProductService productService;
    private final ProductRepository productRepository;
    private final UserService userService;
    private final VendorService vendorService;

    public ProductController(OrderItemRepository orderItemRepository, ProductService productService,
            ProductRepository productRepository, UserService userService, VendorService vendorService) {
        this.orderItemRepository = orderItemRepository;
        this.productService = productService;
        this.productRepository = productRepository;
        this.userService = userService;
        this.vendorService = vendorService;
    }

    @GetMapping
    public ResponseEntity<?> getProducts() {
        List<ProductDTO> products = productService.getProductDTO();
        return ResponseEntity.ok(products);
    }

    // @PostMapping
    // public ResponseEntity<?> createProduct(@RequestBody Product newProduct) {
    // try {
    // Product updatedProduct = productService.createProduct(newProduct);
    // return ResponseEntity.ok(productRepository.save(updatedProduct));
    // } catch (Exception e) {
    // return ResponseEntity.badRequest().build();
    // }
    // }
    @PostMapping("/update/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody Product newProduct) {
        newProduct.setId(id);
        Product updatedProduct;
        try {
            updatedProduct = productService.updateProduct(newProduct);
            return ResponseEntity.ok(productRepository.save(updatedProduct));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/filter")
    public List<Product> filterProducts(@RequestParam String filter) {
        if (filter.equals("priceDesc")) {
            return productRepository.findAll(Sort.by(Product::getProductPrice));
        }
        return productRepository.findAll(Sort.by(Product::getProductPrice));
    }

    @GetMapping("/vendor-pick-list/{orderId}")
    public ResponseEntity<?> getVendorPickList(@PathVariable UUID orderId, Authentication authentication) {
        User currentUser = userService.getUserByUsername(authentication.getName());
        Vendor currentVendor = vendorService.getVendorByUserId(currentUser.getId());
        List<VendorPickList> pickList = orderItemRepository.findVendorPickListByOrderId(orderId,
                currentVendor.getId());
        return ResponseEntity.ok(pickList);
    }
}