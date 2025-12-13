package com.example.apps.products.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.apps.products.dtos.ProductImageDTO;
import com.example.apps.products.services.IProductImageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/api/public/product-images")
@RequiredArgsConstructor
public class ProductImagePublicController {

    private final IProductImageService productImageService;

    @GetMapping
    public ResponseEntity<List<ProductImageDTO>> getAll() {
        return ResponseEntity.ok(productImageService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductImageDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productImageService.getById(id));
    }
}
