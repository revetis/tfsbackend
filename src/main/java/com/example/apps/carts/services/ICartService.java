package com.example.apps.carts.services;

import org.springframework.data.domain.Page;

import com.example.apps.carts.dtos.CartCheckoutDTO;
import com.example.apps.carts.dtos.CartDTO;
import com.example.apps.carts.dtos.CartItemDTOIU;

public interface ICartService {
        public CartDTO getCartByUserId(Long userId, Long actualUserId);

        public Page<CartDTO> getAllCarts(int page, int size);

        // Paginated version with filtering
        CartPageResult getAllCarts(int start, int end, String sortField, String sortOrder, String search, Long userId);

        public CartDTO addItemToCart(Long userId, CartItemDTOIU cartItemDTOIU, Long actualUserId);

        public CartDTO removeItemFromCart(Long userId, Long cartItemId, Long actualUserId);

        public Boolean clearCart(Long userId, Long actualUserId);

        public CartDTO updateItemQuantity(Long userId, Long cartItemId, Integer quantity, Long actualUserId);

        public CartCheckoutDTO validateCartForCheckout(Long userId, Long actualUserId, Double shippingCost,
                        String couponCode);

        public CartDTO getCartById(Long id);

        public CartCheckoutDTO validateGuestCart(java.util.List<CartItemDTOIU> items, Double shippingCost,
                        String couponCode);

        void deleteCartById(Long id);

        // Result record for paginated carts
        record CartPageResult(java.util.List<CartDTO> data, long totalCount) {
        }
}
