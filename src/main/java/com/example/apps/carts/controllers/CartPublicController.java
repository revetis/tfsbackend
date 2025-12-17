package com.example.apps.carts.controllers;

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
import com.example.apps.carts.dtos.CartDTO;
import com.example.apps.carts.dtos.CartItemDTOIU;
import com.example.apps.carts.services.ICartService;
import com.example.tfs.maindto.ApiTemplate;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/api/public/cart")
@RequiredArgsConstructor
public class CartPublicController {

        private final ICartService cartService;
        private final IUserRepository userRepository;

        @GetMapping
        public ResponseEntity<ApiTemplate<Void, CartDTO>> getCart(@AuthenticationPrincipal UserDetails userDetails,
                        HttpServletRequest servletRequest) {
                User user = userRepository.findByUsername(userDetails.getUsername())
                                .orElseThrow(() -> new RuntimeException("User not found"));
                CartDTO cart = cartService.getCartByUserId(user.getId());
                return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(),
                                null, cart));
        }

        @PostMapping("/items")
        public ResponseEntity<ApiTemplate<Void, CartDTO>> addItem(
                        @AuthenticationPrincipal UserDetails userDetails,
                        @Valid @RequestBody CartItemDTOIU item, HttpServletRequest servletRequest) {
                User user = userRepository.findByUsername(userDetails.getUsername())
                                .orElseThrow(() -> new RuntimeException("User not found"));
                CartDTO cart = cartService.addItem(user.getId(), item);
                return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(),
                                null, cart));
        }

        @PutMapping("/items/{productId}")
        public ResponseEntity<ApiTemplate<Void, CartDTO>> updateItemQuantity(
                        @AuthenticationPrincipal UserDetails userDetails,
                        @PathVariable Long productId,
                        @RequestParam Integer quantity, HttpServletRequest servletRequest) {
                User user = userRepository.findByUsername(userDetails.getUsername())
                                .orElseThrow(() -> new RuntimeException("User not found"));
                CartDTO cart = cartService.updateItemQuantity(user.getId(), productId, quantity);
                return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(),
                                null, cart));
        }

        @DeleteMapping("/items/{productId}")
        public ResponseEntity<ApiTemplate<Void, CartDTO>> removeItem(
                        @AuthenticationPrincipal UserDetails userDetails,
                        @PathVariable Long productId, HttpServletRequest servletRequest) {
                User user = userRepository.findByUsername(userDetails.getUsername())
                                .orElseThrow(() -> new RuntimeException("User not found"));
                CartDTO cart = cartService.removeItem(user.getId(), productId);
                return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(),
                                null, cart));
        }

        @DeleteMapping
        public ResponseEntity<ApiTemplate<Void, String>> clearCart(@AuthenticationPrincipal UserDetails userDetails,
                        HttpServletRequest servletRequest) {
                User user = userRepository.findByUsername(userDetails.getUsername())
                                .orElseThrow(() -> new RuntimeException("User not found"));
                cartService.clearCart(user.getId());
                return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(),
                                null, "Cart cleared successfully"));
        }
}
