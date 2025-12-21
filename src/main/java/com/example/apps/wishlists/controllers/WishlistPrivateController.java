package com.example.apps.wishlists.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.apps.wishlists.services.IWishlistService;
import com.example.tfs.maindto.ApiTemplate;

@RestController
@RequestMapping("/rest/api/private/wishlists")
@Validated
public class WishlistPrivateController {

    @Autowired
    private IWishlistService wishlistService;

    @GetMapping("/user/{userId}")
    @PreAuthorize("#userId == authentication.principal.id")
    public ResponseEntity<?> getWishlistByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpResponseStatus.ACCEPTED.code(),
                "/rest/api/private/wishlists/user/" + userId,
                null,
                wishlistService.getWishlistById(userId)));
    }

    @PostMapping("/user/{userId}/add-product/{productId}")
    @PreAuthorize("#userId == authentication.principal.id")
    public ResponseEntity<?> addProductToWishlist(@PathVariable Long userId, @PathVariable Long productId) {
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.ACCEPTED.value(),
                "/rest/api/private/wishlists/user/" + userId + "/add-product/" + productId,
                null,
                wishlistService.addProductToWishlist(userId, productId)));
    }

    @DeleteMapping("/user/{userId}/remove-product/{productId}")
    @PreAuthorize("#userId == authentication.principal.id")
    public ResponseEntity<?> removeProductFromWishlist(@PathVariable Long userId, @PathVariable Long productId) {
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpResponseStatus.ACCEPTED.code(),
                "/rest/api/private/wishlists/user/" + userId + "/remove-product/" + productId,
                null,
                wishlistService.removeProductFromWishlist(userId, productId)));
    }

    @DeleteMapping("/user/{userId}/clear")
    @PreAuthorize("#userId == authentication.principal.id")
    public ResponseEntity<?> clearWishlist(@PathVariable Long userId) {
        wishlistService.clearWishlist(userId);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpResponseStatus.ACCEPTED.code(),
                "/rest/api/private/wishlists/user/" + userId + "/clear",
                null,
                "Wishlist cleared successfully."));
    }

    @PostMapping("/user/{userId}/move-to-cart/{productId}")
    @PreAuthorize("#userId == authentication.principal.id")
    public ResponseEntity<?> moveToCart(@PathVariable Long userId, @PathVariable Long productId) {
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpResponseStatus.ACCEPTED.code(),
                "/rest/api/private/wishlists/user/" + userId + "/move-to-cart/" + productId,
                null,
                wishlistService.moveToCart(userId, productId)
                        ? "Product moved to cart successfully."
                        : "Failed to move product to cart."));
    }
}