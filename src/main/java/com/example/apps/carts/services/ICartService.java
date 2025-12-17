package com.example.apps.carts.services;

import com.example.apps.carts.dtos.CartDTO;
import com.example.apps.carts.dtos.CartItemDTOIU;

public interface ICartService {

    CartDTO getCartByUserId(Long userId);

    CartDTO addItem(Long userId, CartItemDTOIU item);

    CartDTO updateItemQuantity(Long userId, Long productId, Integer quantity);

    CartDTO removeItem(Long userId, Long productId);

    void clearCart(Long userId);
}
