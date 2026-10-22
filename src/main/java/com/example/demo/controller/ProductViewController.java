package com.example.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.example.demo.dto.ProductReviewPageDTO;
import com.example.demo.dto.ProductSalesHistoryDTO;
import com.example.demo.model.OrderItem;
import com.example.demo.model.PageView;
import com.example.demo.model.Product;
import com.example.demo.model.User;
import com.example.demo.model.Vendor;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.repository.WarehouseRepository;
import com.example.demo.service.CartService;
import com.example.demo.service.ProductService;
import com.example.demo.service.UserService;
import com.example.demo.service.VendorService;
import com.example.demo.service.WishlistService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/products")
public class ProductViewController {
    private final WishlistService wishlistService;
    private final WarehouseRepository warehouseRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final UserService userService;
    private final CartService cartService;
    private final VendorService vendorService;
    private final ReviewRepository reviewRepository;

    public ProductViewController(WishlistService wishlistService, WarehouseRepository warehouseRepository,
            OrderItemRepository orderItemRepository, ProductRepository productRepository,
            ProductService productService, UserService userService, CartService cartService,
            VendorService vendorService, ReviewRepository reviewRepository) {
        this.wishlistService = wishlistService;
        this.warehouseRepository = warehouseRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.productService = productService;
        this.userService = userService;
        this.cartService = cartService;
        this.vendorService = vendorService;
        this.reviewRepository = reviewRepository;
    }

    // ---- Product list ----
    @GetMapping
    public String showProductList(@RequestParam(required = false) String filter,
            @RequestParam(defaultValue = "false") boolean inStockOnly,
            @RequestParam(name = "vendor", required = false) String vendorNameFilter,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String availability,
            @RequestParam(defaultValue = "1") int page, HttpServletRequest request, Model model,
            Authentication authentication) {
        User currentUser = userService.getUserByUsername(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);
        boolean isUser = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"));
        model.addAttribute("isUser", isUser);
        boolean isVendor = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_VENDOR"));
        String vendorBusinessName = null;
        if (isVendor) {
            Vendor vendor = vendorService.getVendorByUserId(currentUser.getId());
            vendorBusinessName = vendor.getBusinessName();
        }
        model.addAttribute("isVendor", isVendor);
        model.addAttribute("businessName", vendorBusinessName);
        model.addAttribute("username", currentUser.getUsername());
        model.addAttribute("productCount", productRepository.countProducts());
        model.addAttribute("outOfStockProducts", productRepository.countOutOfStockProducts());
        Map<String, Integer> quantitySoldByProduct = new HashMap<>();
        for (OrderItem orderItem : orderItemRepository.findAll()) {
            if (orderItem.getProductName() != null) {
                quantitySoldByProduct.merge(orderItem.getProductName(), orderItem.getProductQuantity(), Integer::sum);
            }
        }
        model.addAttribute("quantitySoldByProduct", quantitySoldByProduct);
        List<Product> products = filter == null
                ? productService.getProducts()
                : productService.getFilteredProducts(filter, vendorBusinessName);
        products = products.stream()
                .filter(p -> vendorNameFilter == null || vendorNameFilter.isBlank()
                        || vendorNameFilter.equals(p.getVendorName()))
                .filter(p -> warehouseId == null || warehouseId.equals(p.getWarehouseId()))
                .filter(p -> availability == null || availability.isBlank()
                        || ("inStock".equals(availability) && p.getProductQuantity() > 0)
                        || ("outOfStock".equals(availability) && p.getProductQuantity() <= 0))
                .toList();
        if (inStockOnly) {
            products = products.stream().filter(p -> p.getProductQuantity() > 0).toList();
        }
        PageView<Product> productPager = PageView.of(products, page, PageView.DEFAULT_SIZE)
                .withLinks(request, "page");
        model.addAttribute("products", productPager.getContent());
        model.addAttribute("productPager", productPager);
        model.addAttribute("inStockOnly", inStockOnly);
        model.addAttribute("vendors", productService.getProducts().stream()
                .map(Product::getVendorName)
                .filter(name -> name != null && !name.isBlank())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList());
        model.addAttribute("warehouses", warehouseRepository.findAll());
        model.addAttribute("selectedVendor", vendorNameFilter);
        model.addAttribute("selectedWarehouseId", warehouseId);
        model.addAttribute("selectedAvailability", availability);
        return "products";
    }

    // ---- Add product ----
    @GetMapping("/product-add")
    public String showAddProductForm(Model model, Authentication authentication) {
        model.addAttribute("productForm", new Product());
        boolean isVendor = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_VENDOR"));
        model.addAttribute("isVendor", isVendor);
        model.addAttribute("warehouses", warehouseRepository.findAll());
        return "product-add";
    }

    @PostMapping("/product-add")
    public String handleAddProduct(@ModelAttribute("productForm") Product productForm,
            RedirectAttributes redirectAttributes, Authentication authentication) {
        User currentUser = userService.getUserByUsername(authentication.getName());
        String vendorName = vendorService.getVendorByUserId(currentUser.getId()).getBusinessName();
        boolean exists = productService.checkProductExists(productForm.getProductName());
        if (exists) {
            redirectAttributes.addFlashAttribute("errorMessage", "Product already exists");
        } else {
            productService.createProduct(productForm, vendorName);
            redirectAttributes.addFlashAttribute("successMessage", "Product successfully created");
        }
        return "redirect:/products/product-add";
    }

    // ---- Search products ----
    @GetMapping("/product-search")
    public String showProductSearchForm(
            @RequestParam(required = false) String productName,
            @RequestParam(defaultValue = "1") int page,
            HttpServletRequest request,
            Model model,
            Authentication authentication) {
        Product searchForm = new Product();
        searchForm.setProductName(productName);
        model.addAttribute("productSearchForm", searchForm);
        model.addAttribute("warehouse", warehouseRepository.findAll());
        if (productName != null) {
            PageView<Product> searchPager = PageView
                    .of(productService.searchProductsByName(productName), page, PageView.DEFAULT_SIZE)
                    .withLinks(request, "page");
            model.addAttribute("searchResults", searchPager.getContent());
            model.addAttribute("searchPager", searchPager);
        }
        boolean isVendor = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_VENDOR"));
        model.addAttribute("isVendor", isVendor);
        return "product-search";
    }

    // RENDER UPDATE PRODUCT FORM
    @GetMapping("/update/{id}")
    public String showUpdateProductForm(@PathVariable("id") Long productId, Model model,
            RedirectAttributes redirectAttributes) {
        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND,
                            "Product not found"));
            model.addAttribute("productForm", product);
            model.addAttribute("warehouses", warehouseRepository.findAll());
            return "product-update";
        } catch (ResponseStatusException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getReason());
            return "redirect:/products";
        }
    }

    // HANDLE PRODUCT UPDATE FORM
    @PostMapping("/update/{id}")
    public String handleUpdateProduct(@PathVariable("id") Long productId,
            @ModelAttribute("productForm") Product productForm,
            RedirectAttributes redirectAttributes) {
        productForm.setId(productId);
        try {
            productService.updateProduct(productForm);
            redirectAttributes.addFlashAttribute("successMessage", "Product updated!");
        } catch (ResponseStatusException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getReason());
        }
        return "redirect:/vendor";
    }

    // ---- Delete product ----
    @PostMapping("/delete/{id}")
    public String handleDeleteProduct(@PathVariable("id") Long productId, RedirectAttributes redirectAttributes) {
        productService.deleteProduct(productId);
        redirectAttributes.addFlashAttribute("deleteSuccess", "Product successfully deleted!");
        return "redirect:/products";
    }

    // ---- Cart / purchase ----
    @PostMapping("/purchase/{id}")
    public String handleAddToCart(
            @PathVariable("id") Long productId,
            @RequestParam int quantity,
            @RequestParam(defaultValue = "false") boolean returnToWishlist,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        User currentUser = userService.getUserByUsername(authentication.getName());
        Long userId = currentUser.getId();
        try {
            cartService.addProductToCart(userId, quantity, productId);
            redirectAttributes.addFlashAttribute("successMessage", "Product successfully added to cart");
        } catch (ResponseStatusException exception) {
            redirectAttributes.addFlashAttribute("outOfStockMessage", exception.getReason());
        }
        return returnToWishlist ? "redirect:/products/wishlist/view" : "redirect:/products";
    }

    // ---- Misc ----
    @GetMapping("/home")
    public String showHome() {
        return "home";
    }

    // RENDER: product-view-single
    @GetMapping("/view/{productId}")
    public String showProductPage(@PathVariable("productId") Long productId, Model model) {
        Product requestedProduct = productService.searchProductById(productId);
        model.addAttribute("product", requestedProduct);
        model.addAttribute(
                "averageRating",
                reviewRepository.findAverageRatingByProductId(productId));
        Long unitsSold = productRepository.findUnitsSoldByProductId(productId);
        model.addAttribute("unitsSold", unitsSold);
List<ProductReviewPageDTO> productReviews = productService.getReviewsByProductId(productId);
model.addAttribute("productReviews", productReviews);
        List<ProductSalesHistoryDTO> productSalesHistory = productService.getProductSalesHistory(productId);
        model.addAttribute("productSalesHistory", productSalesHistory);
                return "product-view-single";
    }

    // HANDLE WISHLIST FORM SUBMIT
    @PostMapping("/wishlist/add/{productId}")
    public String handleWishlistUpdate(@PathVariable("productId") Long productId, Model model,
            RedirectAttributes redirectAttributes,
            Authentication authentication) {
        User currentUser = userService.getUserByUsername(authentication.getName());
        Long userId = currentUser.getId();
        try {
            wishlistService.addProductToWishlist(userId, productId);
            redirectAttributes.addFlashAttribute("successMessage", "Product successfully added to wishlist!");
        } catch (ResponseStatusException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getReason());
        }
        return "redirect:/products";
    }

    // RENDER USER WISHLIST PAGE
    @GetMapping("/wishlist/view")
    public String showWishlist(Authentication authentication, Model model) {
        User currentUser = userService.getUserByUsername(authentication.getName());
        model.addAttribute("wishlistItems", wishlistService.getUserWishlist(currentUser.getId()));
        model.addAttribute("currentUser", currentUser);
        return "product-wishlist";
    }

    // HANDLE USER WISHLIST ITEM REMOVAL
    @PostMapping("/wishlist/remove/{productId}")
    public String removeWishlistItem(@PathVariable Long productId, Authentication authentication,
            RedirectAttributes redirectAttributes) {
        User currentUser = userService.getUserByUsername(authentication.getName());
        wishlistService.removeProductFromWishlist(currentUser.getId(), productId);
        redirectAttributes.addFlashAttribute("successMessage", "Product removed from your wishlist.");
        return "redirect:/products/wishlist/view";
    }
}
