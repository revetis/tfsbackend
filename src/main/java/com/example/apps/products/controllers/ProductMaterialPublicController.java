package com.example.apps.products.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.apps.products.dtos.ProductMaterialDTO;
import com.example.apps.products.services.IProductMaterialService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/api/public/product-materials")
@RequiredArgsConstructor
public class ProductMaterialPublicController {

    private final IProductMaterialService productMaterialService;

    @GetMapping
    public ResponseEntity<List<ProductMaterialDTO>> getAll() {
        return ResponseEntity.ok(productMaterialService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductMaterialDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productMaterialService.getById(id));
    }
}
