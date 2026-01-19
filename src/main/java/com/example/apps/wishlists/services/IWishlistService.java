package com.example.apps.wishlists.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.apps.wishlists.dtos.WishlistDTO;
import com.example.apps.wishlists.dtos.WishlistProductDTO;

public interface IWishlistService {
    public Page<WishlistProductDTO> getAllWishlists(Pageable pageable);

    public List<WishlistProductDTO> getWishlistById(Long userId);

    public List<WishlistProductDTO> createWishlist(WishlistDTO wishlistDTO);

    public List<WishlistProductDTO> addProductToWishlist(Long userId, Long productId);

    public WishlistProductDTO removeProductFromWishlist(Long userId, Long productId);

    public Boolean clearWishlist(Long userId);

    Boolean moveToCart(Long userId, Long productId);

    public WishlistProductDTO getWishlistItemById(Long id);

    // Paginated version with filtering
    WishlistPageResult getAllWishlists(int start, int end, String sortField, String sortOrder, String search,
            Long userId, Long productId);

    // Result record for paginated wishlists
    record WishlistPageResult(List<WishlistProductDTO> data, long totalCount) {
    }
}
