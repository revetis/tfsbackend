package com.example.apps.wishlists.controllers;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.apps.products.dtos.ProductDTO;
import com.example.apps.products.entities.Product;
import com.example.apps.wishlists.services.IWishListService;
import com.example.settings.maindto.ApiTemplate;

@RestController
@RequestMapping("/rest/api/private/wishlists")
@Validated
public class WishlistController {

    @Autowired
    private IWishListService wishlistService;

    @GetMapping("/{userId}")
    public ResponseEntity<ApiTemplate<?, List<ProductDTO>>> getWishlist(@PathVariable("userId") Long userId,
            Principal principal) throws AccessDeniedException {
        List<ProductDTO> products = wishlistService.getById(userId, principal.getName());
        return ResponseEntity.ok(
                ApiTemplate.apiTemplateGenerator(true, HttpStatus.OK.value(), "/rest/api/private/wishlists/" + userId,
                        null,
                        products));
    }

    @PostMapping("/{userId}/products/{productId}")
    public ResponseEntity<?> putProductToWishlist(@PathVariable("userId") Long userId,
            @PathVariable("productId") Long productId, Principal principal) throws AccessDeniedException {
        wishlistService.putProductToWishlist(userId, productId, principal.getName());
        return ResponseEntity.ok(
                ApiTemplate.apiTemplateGenerator(true, HttpStatus.OK.value(),
                        "/rest/api/private/wishlists/" + userId + "/products/" + productId, null,
                        "Product added to wishlist successfully"));
    }

    @DeleteMapping("/{userId}/products/{productId}")
    public ResponseEntity<?> deleteProductFromWishlist(@PathVariable("userId") Long userId,
            @PathVariable("productId") Long productId, Principal principal) throws AccessDeniedException {
        wishlistService.deleteProductFromWishlist(userId, productId, principal.getName());
        return ResponseEntity.ok(
                ApiTemplate.apiTemplateGenerator(true, HttpStatus.OK.value(),
                        "/rest/api/private/wishlists/" + userId + "/products/" + productId,
                        null,
                        "Product removed from wishlist successfully"));
    }

}
