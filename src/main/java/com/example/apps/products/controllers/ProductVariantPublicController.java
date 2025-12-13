package com.example.apps.products.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.apps.products.dtos.ProductVariantDTO;
import com.example.apps.products.services.IProductVariantService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/api/public/product-variants")
@RequiredArgsConstructor
public class ProductVariantPublicController {

    private final IProductVariantService productVariantService;

    @GetMapping
    public ResponseEntity<List<ProductVariantDTO>> getAll() {
        return ResponseEntity.ok(productVariantService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductVariantDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productVariantService.getById(id));
    }
}
