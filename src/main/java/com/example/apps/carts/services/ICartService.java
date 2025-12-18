package com.example.apps.carts.services;

import com.example.apps.carts.dtos.CartCheckoutDTO;
import com.example.apps.carts.dtos.CartDTO;
import com.example.apps.carts.dtos.CartItemDTOIU;

public interface ICartService {
    public CartDTO getCartByUserId(Long userId, Long actualUserId);

    public CartDTO addItemToCart(Long userId, CartItemDTOIU cartItemDTOIU, Long actualUserId);

    public CartDTO removeItemFromCart(Long userId, Long cartItemId, Long actualUserId);

    public Boolean clearCart(Long userId, Long actualUserId);

    public CartDTO updateItemQuantity(Long userId, Long cartItemId, Integer quantity, Long actualUserId);

    public CartCheckoutDTO validateCartForCheckout(Long userId, Long actualUserId, Double shippingCost);

}
