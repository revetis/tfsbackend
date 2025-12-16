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
import com.example.settings.maindto.ApiTemplate;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/api/admin/product-variants")
@RequiredArgsConstructor
@Validated
public class ProductVariantAdminController {

    private final IProductVariantService productVariantService;

    @GetMapping
    public ResponseEntity<ApiTemplate<Void, List<ProductVariantDTO>>> getAll(HttpServletRequest servletRequest) {
        List<ProductVariantDTO> productVariants = productVariantService.getAll();
        return ResponseEntity
                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null, productVariants));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiTemplate<Void, ProductVariantDTO>> getById(@PathVariable Long id,
            HttpServletRequest servletRequest) {
        ProductVariantDTO productVariant = productVariantService.getById(id);
        return ResponseEntity
                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null, productVariant));
    }

    @PostMapping
    public ResponseEntity<ApiTemplate<Void, ProductVariantDTO>> create(
            @Valid @RequestBody ProductVariantDTOIU productVariantDTOIU, HttpServletRequest servletRequest) {
        ProductVariantDTO createdProductVariant = productVariantService.create(productVariantDTOIU);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null,
                createdProductVariant));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiTemplate<Void, ProductVariantDTO>> update(@PathVariable Long id,
            @Valid @RequestBody ProductVariantDTOIU productVariantDTOIU, HttpServletRequest servletRequest) {
        ProductVariantDTO updatedProductVariant = productVariantService.update(id, productVariantDTOIU);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null,
                updatedProductVariant));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiTemplate<Void, String>> delete(@PathVariable Long id, HttpServletRequest servletRequest) {
        productVariantService.delete(id);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null,
                "Product variant deleted successfully"));
    }
}
