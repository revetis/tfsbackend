package com.example.apps.carts.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.apps.carts.dtos.CartItemDTOIU;
import com.example.apps.carts.services.ICartService;
import com.example.tfs.maindto.ApiTemplate;
import com.example.tfs.utils.SecurityUtils;

import io.netty.handler.codec.http.HttpResponseStatus;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/rest/api/private/carts")
@Validated
public class CartPrivateController {

    @Autowired
    private ICartService cartService;

    @Autowired
    private SecurityUtils securityUtils;

    @GetMapping("/user/{userId}")
    @PreAuthorize("#userId == authentication.principal.id")
    public ResponseEntity<?> getCartByUserId(@PathVariable Long userId) {
        Long actualUserId = securityUtils.getCurrentUserId();

        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, HttpResponseStatus.ACCEPTED.code(),
                "/rest/api/admin/carts/user/" + userId, null, cartService.getCartByUserId(userId, actualUserId)));
    }

    @PostMapping("/user/{userId}/add-item")
    @PreAuthorize("#userId == authentication.principal.id")
    public ResponseEntity<?> addItemToCart(@PathVariable Long userId, @RequestBody @Valid CartItemDTOIU cartItemDTOIU) {
        Long actualUserId = securityUtils.getCurrentUserId();

        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, HttpResponseStatus.ACCEPTED.code(),
                "/rest/api/private/carts/user/" + userId + "/add-item", null,
                cartService.addItemToCart(userId, cartItemDTOIU, actualUserId)));
    }

    @DeleteMapping("/user/{userId}/remove-item/{cartItemId}")
    @PreAuthorize("#userId == authentication.principal.id")
    public ResponseEntity<?> removeItemFromCart(@PathVariable Long userId, @PathVariable Long cartItemId) {
        Long actualUserId = securityUtils.getCurrentUserId();

        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, HttpResponseStatus.ACCEPTED.code(),
                "/rest/api/private/carts/user/" + userId + "/remove-item/" + cartItemId, null,
                cartService.removeItemFromCart(userId, cartItemId, actualUserId)));
    }

    @DeleteMapping("/user/{userId}/clear")
    @PreAuthorize("#userId == authentication.principal.id")
    public ResponseEntity<?> clearCart(@PathVariable Long userId) {
        Long actualUserId = securityUtils.getCurrentUserId();

        cartService.clearCart(userId, actualUserId);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, HttpResponseStatus.ACCEPTED.code(),
                "/rest/api/private/carts/user/" + userId + "/clear", null, "Cart cleared successfully"));
    }

    @PostMapping("/user/{userId}/update-item-quantity/{cartItemId}/{quantity}")
    @PreAuthorize("#userId == authentication.principal.id")
    public ResponseEntity<?> updateItemQuantity(@PathVariable Long userId, @PathVariable Long cartItemId,
            @PathVariable Integer quantity) {
        Long actualUserId = securityUtils.getCurrentUserId();

        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, HttpResponseStatus.ACCEPTED.code(),
                "/rest/api/private/carts/user/" + userId + "/update-item-quantity/" + cartItemId + "/" + quantity,
                null,
                cartService.updateItemQuantity(userId, cartItemId, quantity, actualUserId)));
    }

    @GetMapping("/user/{userId}/checkout-validation")
    @PreAuthorize("#userId == authentication.principal.id")
    public ResponseEntity<?> validateCartForCheckout(@PathVariable Long userId,
            @RequestParam(defaultValue = "90.0") Double shippingCost) {
        Long actualUserId = securityUtils.getCurrentUserId();

        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, HttpResponseStatus.ACCEPTED.code(),
                "/rest/api/private/carts/user/" + userId + "/checkout-validation", null,
                cartService.validateCartForCheckout(userId, actualUserId, shippingCost)));
    }

}
