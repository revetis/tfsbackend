package com.example.apps.products.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.apps.products.dtos.ProductImageDTO;
import com.example.apps.products.dtos.ProductImageDTOIU;
import com.example.apps.products.services.IProductImageService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/api/admin/product-images")
@RequiredArgsConstructor
@Validated
public class ProductImageAdminController {

    private final IProductImageService productImageService;

    @GetMapping
    public ResponseEntity<List<ProductImageDTO>> getAll() {
        return ResponseEntity.ok(productImageService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductImageDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productImageService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ProductImageDTO> create(@Valid @ModelAttribute ProductImageDTOIU productImageDTOIU) {
        return ResponseEntity.ok(productImageService.create(productImageDTOIU));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductImageDTO> update(@PathVariable Long id,
            @Valid @ModelAttribute ProductImageDTOIU productImageDTOIU) {
        return ResponseEntity.ok(productImageService.update(id, productImageDTOIU));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productImageService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
