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

import com.example.apps.products.dtos.ProductMaterialDTO;
import com.example.apps.products.dtos.ProductMaterialDTOIU;
import com.example.apps.products.services.IProductMaterialService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/api/admin/product-materials")
@RequiredArgsConstructor
@Validated
public class ProductMaterialAdminController {

    private final IProductMaterialService productMaterialService;

    @GetMapping
    public ResponseEntity<List<ProductMaterialDTO>> getAll() {
        return ResponseEntity.ok(productMaterialService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductMaterialDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productMaterialService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ProductMaterialDTO> create(@Valid @RequestBody ProductMaterialDTOIU productMaterialDTOIU) {
        return ResponseEntity.ok(productMaterialService.create(productMaterialDTOIU));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductMaterialDTO> update(@PathVariable Long id,
            @Valid @RequestBody ProductMaterialDTOIU productMaterialDTOIU) {
        return ResponseEntity.ok(productMaterialService.update(id, productMaterialDTOIU));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productMaterialService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
