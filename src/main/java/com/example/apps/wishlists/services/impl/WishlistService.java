package com.example.apps.wishlists.services.impl;

import java.util.List;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.apps.carts.dtos.CartItemDTOIU;
import com.example.apps.carts.services.ICartService;
import com.example.apps.products.entities.Product;
import com.example.apps.products.repositories.ProductRepository;
import com.example.apps.wishlists.dtos.WishlistDTO;
import com.example.apps.wishlists.dtos.WishlistProductDTO;
import com.example.apps.wishlists.entities.Wishlist;
import com.example.apps.wishlists.exceptions.WishlistException;
import com.example.apps.wishlists.repositories.WishlistRepository;
import com.example.apps.wishlists.services.IWishlistService;
import com.example.tfs.utils.SecurityUtils;

@Service
public class WishlistService implements IWishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ICartService cartService;

    @Autowired
    private SecurityUtils securityUtils;

    @Override
    public Page<WishlistProductDTO> getAllWishlists(Pageable pageable) {
        return wishlistRepository.findAll(pageable).map(this::convertToProductDTO);
    }

    @Override
    public List<WishlistProductDTO> getWishlistById(Long userId) {
        return wishlistRepository.findAllByUserId(userId).stream()
                .map(this::convertToProductDTO)
                .toList();
    }

    @Override
    @Transactional
    public List<WishlistProductDTO> createWishlist(WishlistDTO wishlistDTO) {
        Wishlist wishlist = new Wishlist();
        BeanUtils.copyProperties(wishlistDTO, wishlist);
        wishlistRepository.save(wishlist);
        return getWishlistById(wishlistDTO.getUserId());
    }

    @Override
    @Transactional
    public List<WishlistProductDTO> addProductToWishlist(Long userId, Long productId) {
        if (wishlistRepository.findByUserIdAndProductId(userId, productId).isPresent()) {
            return getWishlistById(userId);
        }

        Wishlist wishlist = Wishlist.builder()
                .userId(userId)
                .productId(productId)
                .build();

        wishlistRepository.save(wishlist);
        return getWishlistById(userId);
    }

    @Override
    @Transactional
    public WishlistProductDTO removeProductFromWishlist(Long userId, Long productId) {
        Wishlist wishlist = wishlistRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new WishlistException("Product not found in wishlist"));

        WishlistProductDTO dto = convertToProductDTO(wishlist);
        wishlistRepository.delete(wishlist);
        return dto;
    }

    @Override
    @Transactional
    public Boolean clearWishlist(Long userId) {
        wishlistRepository.deleteByUserId(userId);
        return true;
    }

    private WishlistProductDTO convertToProductDTO(Wishlist wishlist) {
        Product product = productRepository.findById(wishlist.getProductId()).orElse(null);

        return WishlistProductDTO.builder()
                .id(wishlist.getId())
                .productName(product != null ? product.getName() : "Unknown Product")
                .isAvailable(product != null)
                .addedAt(wishlist.getCreatedAt() != null ? wishlist.getCreatedAt().toString() : null)

                .build();
    }

    @Autowired

    @Override
    @Transactional
    public Boolean moveToCart(Long userId, Long productId) {
        boolean exists = wishlistRepository.findByUserIdAndProductId(userId, productId).isPresent();

        if (exists) {
            CartItemDTOIU cartItem = new CartItemDTOIU();
            cartItem.setProductVariantId(productId);
            cartItem.setQuantity(1);

            Long actualId = securityUtils.getCurrentUserId();

            cartService.addItemToCart(userId, cartItem, actualId);

            wishlistRepository.deleteByUserIdAndProductId(userId, productId);

            return true;
        }

        throw new WishlistException("Product not found in wishlist");
    }
}