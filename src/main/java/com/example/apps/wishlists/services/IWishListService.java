package com.example.apps.wishlists.services;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;

import com.example.apps.products.dtos.ProductDTO;

public interface IWishListService {

    List<ProductDTO> getById(Long id, String actualUserUsername) throws AccessDeniedException;

    void putProductToWishlist(Long userId, Long productId, String actualUserUsername) throws AccessDeniedException;

    void deleteProductFromWishlist(Long userId, Long productId, String actualUserUsername)
            throws AccessDeniedException;

}
