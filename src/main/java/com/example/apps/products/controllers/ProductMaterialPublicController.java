package com.example.apps.products.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.apps.products.dtos.ProductMaterialDTO;
import com.example.apps.products.services.IProductMaterialService;
import com.example.settings.maindto.ApiTemplate;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/api/public/product-materials")
@RequiredArgsConstructor
public class ProductMaterialPublicController {

    private final IProductMaterialService productMaterialService;

    @GetMapping
    public ResponseEntity<ApiTemplate<Void, List<ProductMaterialDTO>>> getAll(HttpServletRequest servletRequest) {
        List<ProductMaterialDTO> productMaterials = productMaterialService.getAll();
        return ResponseEntity.ok(
                ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null, productMaterials));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiTemplate<Void, ProductMaterialDTO>> getById(@PathVariable Long id,
            HttpServletRequest servletRequest) {
        ProductMaterialDTO productMaterial = productMaterialService.getById(id);
        return ResponseEntity
                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null, productMaterial));
    }
}
