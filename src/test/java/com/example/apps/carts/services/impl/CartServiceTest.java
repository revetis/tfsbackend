package com.example.apps.carts.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.example.apps.carts.dtos.CartDTO;
import com.example.apps.carts.dtos.CartItemDTOIU;
import com.example.apps.carts.entities.Cart;
import com.example.apps.carts.entities.CartItem;
import com.example.apps.carts.repositories.CartItemRepository;
import com.example.apps.carts.repositories.CartRepository;
import com.example.apps.products.entities.ProductVariant;
import com.example.apps.products.repositories.ProductVariantRepository;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private Cart cart;
    private Long userId = 1L;

    @BeforeEach
    void setUp() {
        cart = new Cart();
        cart.setId(1L);
        cart.setUserId(userId);
        cart.setItems(new ArrayList<>());
    }

    @Test
    void getCartByUserId_Success() {
        when(cartRepository.findCartByUserId(userId)).thenReturn(Optional.of(cart));

        CartDTO result = cartService.getCartByUserId(userId, userId);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
    }

    @Test
    void getCartByUserId_AccessDenied() {
        when(cartRepository.findCartByUserId(userId)).thenReturn(Optional.of(cart));

        assertThrows(AccessDeniedException.class, () -> cartService.getCartByUserId(userId, 2L));
    }

    @Test
    void addItemToCart_Success() {
        CartItemDTOIU itemDTOIU = new CartItemDTOIU();
        itemDTOIU.setProductVariantId(1L);
        itemDTOIU.setQuantity(2);

        ProductVariant variant = new ProductVariant();
        variant.setId(1L);
        variant.setPrice(new BigDecimal("100.00"));

        when(cartRepository.findCartByUserId(userId)).thenReturn(Optional.of(cart));
        when(productVariantRepository.findById(1L)).thenReturn(Optional.of(variant));

        CartDTO result = cartService.addItemToCart(userId, itemDTOIU, userId);

        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void removeItemFromCart_Success() {
        CartItem cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setCart(cart);
        cart.getItems().add(cartItem);

        when(cartRepository.findCartByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(cartItem));
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));

        CartDTO result = cartService.removeItemFromCart(userId, 1L, userId);

        assertNotNull(result);
        assertEquals(0, result.getItems().size());
        verify(cartItemRepository).delete(cartItem);
    }
}
