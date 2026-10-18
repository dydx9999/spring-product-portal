package com.example.demo.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.model.Product;
import com.example.demo.model.WishlistItem;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.WishlistRepository;

@Service
public class WishlistService {
    public record WishlistEntry(Long wishlistId, Long productId, Product product) {
    }

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;

    public WishlistService(WishlistRepository wishlistRepository, ProductRepository productRepository) {
        this.wishlistRepository = wishlistRepository;
        this.productRepository = productRepository;
    }

    public List<WishlistEntry> getUserWishlist(Long userId) {
        List<WishlistItem> items = wishlistRepository.findByUserIdOrderByWishlistIdAsc(userId);
        Map<Long, Product> productsById = productRepository.findAllById(items.stream()
                .map(WishlistItem::getProductId)
                .toList()).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        return items.stream()
                .map(item -> new WishlistEntry(item.getWishlistId(), item.getProductId(),
                        productsById.get(item.getProductId())))
                .toList();
    }

    public WishlistItem addProductToWishlist(Long userId, Long productId) {

        if (wishlistRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product is already present in wishlist!");
        }

        WishlistItem requestedWishlistItem = new WishlistItem();
        requestedWishlistItem.setUserId(userId);
        requestedWishlistItem.setProductId(productId);

        return wishlistRepository.save(requestedWishlistItem);

    }

    @Transactional
    public void removeProductFromWishlist(Long userId, Long productId) {
        wishlistRepository.deleteByUserIdAndProductId(userId, productId);
    }

    public boolean checkProductInUserWishlist(Long userId, Long productId) {
        return wishlistRepository.existsByUserIdAndProductId(userId, productId);
    }
}
