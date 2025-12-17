package com.example.apps.products.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.apps.products.dtos.ProductImageDTO;
import com.example.apps.products.services.IProductImageService;
import com.example.tfs.maindto.ApiTemplate;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/api/public/product-images")
@RequiredArgsConstructor
public class ProductImagePublicController {

    private final IProductImageService productImageService;

    @GetMapping
    public ResponseEntity<ApiTemplate<Void, List<ProductImageDTO>>> getAll(HttpServletRequest servletRequest) {
        List<ProductImageDTO> productImages = productImageService.getAll();
        return ResponseEntity
                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null, productImages));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiTemplate<Void, ProductImageDTO>> getById(@PathVariable Long id,
            HttpServletRequest servletRequest) {
        ProductImageDTO productImage = productImageService.getById(id);
        return ResponseEntity
                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null, productImage));
    }
}
