package com.example.demo.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.model.WishlistItem;
import com.example.demo.repository.WishlistRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class WishlistService {
    private final WishlistRepository wishlistRepository;

    public List<WishlistItem> getUserWishlist(Long userId) {
        return wishlistRepository.findByUserId(userId);
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
}
