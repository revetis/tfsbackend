package com.example.apps.products.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.apps.products.dtos.ProductVariantDTO;
import com.example.apps.products.dtos.ProductVariantDTOIU;
import com.example.apps.products.services.IProductVariantService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/api/admin/product-variants")
@RequiredArgsConstructor
@Validated
public class ProductVariantAdminController {

    private final IProductVariantService productVariantService;

    @GetMapping
    public ResponseEntity<List<ProductVariantDTO>> getAll() {
        return ResponseEntity.ok(productVariantService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductVariantDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productVariantService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ProductVariantDTO> create(@Valid @RequestBody ProductVariantDTOIU productVariantDTOIU) {
        return ResponseEntity.ok(productVariantService.create(productVariantDTOIU));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductVariantDTO> update(@PathVariable Long id,
            @Valid @RequestBody ProductVariantDTOIU productVariantDTOIU) {
        return ResponseEntity.ok(productVariantService.update(id, productVariantDTOIU));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productVariantService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
