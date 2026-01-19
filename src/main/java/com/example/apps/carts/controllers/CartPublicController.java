package com.example.apps.carts.controllers;

import org.apache.hc.core5.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.apps.carts.dtos.CartValidationRequest;
import com.example.apps.carts.services.ICartService;
import com.example.tfs.maindto.ApiTemplate;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/rest/api/public/carts")
@Validated
public class CartPublicController {

    @Autowired
    private ICartService cartService;

    @PostMapping("/validate")
    public ResponseEntity<?> validateCart(@RequestBody @Valid CartValidationRequest request) {
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, HttpStatus.SC_OK,
                "/rest/api/public/carts/validate", null,
                cartService.validateGuestCart(request.getItems(), request.getShippingCost(), request.getCouponCode())));
    }
}
