package com.example.apps.carts.services.impl;

import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.apps.carts.dtos.CartDTO;
import com.example.apps.carts.dtos.CartItemDTO;
import com.example.apps.carts.dtos.CartItemDTOIU;
import com.example.apps.carts.entities.Cart;
import com.example.apps.carts.entities.CartItem;
import com.example.apps.carts.repositories.CartRepository;
import com.example.apps.carts.services.ICartService;
import com.example.apps.products.entities.Product;
import com.example.apps.products.repositories.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements ICartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    @Override
    public CartDTO getCartByUserId(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCart(userId));
        return mapToDTO(cart);
    }

    @Override
    public CartDTO addItem(Long userId, CartItemDTOIU itemDTO) {
        // Validate product exists
        Product product = productRepository.findById(itemDTO.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + itemDTO.getProductId()));

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCart(userId));

        CartItem cartItem = new CartItem();
        cartItem.setProductId(product.getId());
        cartItem.setProductName(product.getName());
        cartItem.setQuantity(itemDTO.getQuantity());
        cartItem.setPrice(product.getMainPrice());

        cart.addItem(cartItem);
        cartRepository.save(cart);

        log.info("Added item to cart for user: {}, product: {}", userId, product.getId());
        return mapToDTO(cart);
    }

    @Override
    public CartDTO updateItemQuantity(Long userId, Long productId, Integer quantity) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));

        if (quantity <= 0) {
            cart.removeItem(productId);
        } else {
            cart.updateItemQuantity(productId, quantity);
        }

        cartRepository.save(cart);
        log.info("Updated cart item quantity for user: {}, product: {}", userId, productId);
        return mapToDTO(cart);
    }

    @Override
    public CartDTO removeItem(Long userId, Long productId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));

        cart.removeItem(productId);
        cartRepository.save(cart);

        log.info("Removed item from cart for user: {}, product: {}", userId, productId);
        return mapToDTO(cart);
    }

    @Override
    public void clearCart(Long userId) {
        cartRepository.deleteByUserId(userId);
        log.info("Cleared cart for user: {}", userId);
    }

    private Cart createNewCart(Long userId) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        return cart;
    }

    private CartDTO mapToDTO(Cart cart) {
        CartDTO dto = new CartDTO();
        dto.setUserId(cart.getUserId());
        dto.setItems(cart.getItems().stream()
                .map(this::mapItemToDTO)
                .collect(Collectors.toList()));
        dto.setCreatedAt(cart.getCreatedAt());
        dto.setUpdatedAt(cart.getUpdatedAt());
        return dto;
    }

    private CartItemDTO mapItemToDTO(CartItem item) {
        return new CartItemDTO(
                item.getProductId(),
                item.getProductName(),
                item.getQuantity(),
                item.getPrice());
    }
}
