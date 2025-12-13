package com.example.apps.orders.services;

import com.example.apps.orders.dtos.CartDTO;
import com.example.apps.orders.dtos.CartItemDTOIU;

public interface ICartService {

    CartDTO getCartByUserId(Long userId);

    CartDTO addItem(Long userId, CartItemDTOIU item);

    CartDTO updateItemQuantity(Long userId, Long productId, Integer quantity);

    CartDTO removeItem(Long userId, Long productId);

    void clearCart(Long userId);
}
