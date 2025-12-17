package com.example.apps.products.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.apps.products.dtos.ProductVariantDTO;
import com.example.apps.products.services.IProductVariantService;
import com.example.tfs.maindto.ApiTemplate;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/api/public/product-variants")
@RequiredArgsConstructor
public class ProductVariantPublicController {

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
}
