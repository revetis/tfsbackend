package com.example.apps.carts.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.apps.carts.services.ICartService;
import com.example.tfs.maindto.ApiTemplate;

import io.netty.handler.codec.http.HttpResponseStatus;

@RestController
@RequestMapping("/rest/api/admin/carts")
@Validated
public class CartAdminController {

    @Autowired
    private ICartService cartService;

    @GetMapping("/all")
    public ResponseEntity<?> getAllCarts(int page, int size) {
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, HttpResponseStatus.ACCEPTED.code(),
                "/rest/api/admin/carts/all", null, cartService.getAllCarts(page, size)));
    }

}
