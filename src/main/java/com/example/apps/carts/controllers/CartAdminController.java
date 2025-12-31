package com.example.apps.carts.controllers;

import org.apache.hc.core5.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import com.example.apps.carts.services.ICartService;
import com.example.tfs.maindto.ApiTemplate;

@RestController
@RequestMapping("/rest/api/admin/carts")
@Validated
public class CartAdminController {

    @Autowired
    private ICartService cartService;

    @GetMapping("/all")
    public ResponseEntity<?> getAllCarts(int page, int size) {
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, HttpStatus.SC_OK,
                "/rest/api/admin/carts/all", null, cartService.getAllCarts(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCartById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, HttpStatus.SC_OK,
                "/rest/api/admin/carts/" + id, null, cartService.getCartById(id)));
    }
}
