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
import com.example.settings.maindto.ApiTemplate;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/api/admin/product-materials")
@RequiredArgsConstructor
@Validated
public class ProductMaterialAdminController {

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

    @PostMapping
    public ResponseEntity<ApiTemplate<Void, ProductMaterialDTO>> create(
            @Valid @RequestBody ProductMaterialDTOIU productMaterialDTOIU, HttpServletRequest servletRequest) {
        ProductMaterialDTO createdProductMaterial = productMaterialService.create(productMaterialDTOIU);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null,
                createdProductMaterial));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiTemplate<Void, ProductMaterialDTO>> update(@PathVariable Long id,
            @Valid @RequestBody ProductMaterialDTOIU productMaterialDTOIU, HttpServletRequest servletRequest) {
        ProductMaterialDTO updatedProductMaterial = productMaterialService.update(id, productMaterialDTOIU);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null,
                updatedProductMaterial));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiTemplate<Void, String>> delete(@PathVariable Long id, HttpServletRequest servletRequest) {
        productMaterialService.delete(id);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null,
                "Product material deleted successfully"));
    }
}
