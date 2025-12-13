package com.example.apps.orders.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.apps.auth.entities.User;
import com.example.apps.auth.repositories.IUserRepository;
import com.example.apps.orders.dtos.CartDTO;
import com.example.apps.orders.dtos.CartItemDTOIU;
import com.example.apps.orders.services.ICartService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/api/public/cart")
@RequiredArgsConstructor
public class CartPublicController {

        private final ICartService cartService;
        private final IUserRepository userRepository;

        @GetMapping
        public ResponseEntity<CartDTO> getCart(@AuthenticationPrincipal UserDetails userDetails) {
                User user = userRepository.findByUsername(userDetails.getUsername())
                                .orElseThrow(() -> new RuntimeException("User not found"));
                return ResponseEntity.ok(cartService.getCartByUserId(user.getId()));
        }

        @PostMapping("/items")
        public ResponseEntity<CartDTO> addItem(
                        @AuthenticationPrincipal UserDetails userDetails,
                        @Valid @RequestBody CartItemDTOIU item) {
                User user = userRepository.findByUsername(userDetails.getUsername())
                                .orElseThrow(() -> new RuntimeException("User not found"));
                return ResponseEntity.ok(cartService.addItem(user.getId(), item));
        }

        @PutMapping("/items/{productId}")
        public ResponseEntity<CartDTO> updateItemQuantity(
                        @AuthenticationPrincipal UserDetails userDetails,
                        @PathVariable Long productId,
                        @RequestParam Integer quantity) {
                User user = userRepository.findByUsername(userDetails.getUsername())
                                .orElseThrow(() -> new RuntimeException("User not found"));
                return ResponseEntity.ok(cartService.updateItemQuantity(user.getId(), productId, quantity));
        }

        @DeleteMapping("/items/{productId}")
        public ResponseEntity<CartDTO> removeItem(
                        @AuthenticationPrincipal UserDetails userDetails,
                        @PathVariable Long productId) {
                User user = userRepository.findByUsername(userDetails.getUsername())
                                .orElseThrow(() -> new RuntimeException("User not found"));
                return ResponseEntity.ok(cartService.removeItem(user.getId(), productId));
        }

        @DeleteMapping
        public ResponseEntity<Void> clearCart(@AuthenticationPrincipal UserDetails userDetails) {
                User user = userRepository.findByUsername(userDetails.getUsername())
                                .orElseThrow(() -> new RuntimeException("User not found"));
                cartService.clearCart(user.getId());
                return ResponseEntity.noContent().build();
        }
}
