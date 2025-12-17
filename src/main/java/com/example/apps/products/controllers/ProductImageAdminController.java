package com.example.apps.products.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.apps.products.dtos.ProductImageDTO;
import com.example.apps.products.dtos.ProductImageDTOIU;
import com.example.apps.products.services.IProductImageService;
import com.example.tfs.maindto.ApiTemplate;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/api/admin/product-images")
@RequiredArgsConstructor
@Validated
public class ProductImageAdminController {

        private final IProductImageService productImageService;

        @GetMapping
        public ResponseEntity<ApiTemplate<Void, List<ProductImageDTO>>> getAll(HttpServletRequest servletRequest) {
                List<ProductImageDTO> productImages = productImageService.getAll();
                return ResponseEntity
                                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null,
                                                productImages));
        }

        @GetMapping("/{id}")
        public ResponseEntity<ApiTemplate<Void, ProductImageDTO>> getById(@PathVariable Long id,
                        HttpServletRequest servletRequest) {
                ProductImageDTO productImage = productImageService.getById(id);
                return ResponseEntity
                                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null,
                                                productImage));
        }

        @PostMapping
        public ResponseEntity<ApiTemplate<Void, ProductImageDTO>> create(
                        @Valid @ModelAttribute ProductImageDTOIU productImageDTOIU, HttpServletRequest servletRequest) {
                ProductImageDTO createdProductImage = productImageService.create(productImageDTOIU);
                return ResponseEntity.ok(
                                ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null,
                                                createdProductImage));
        }

        @PutMapping("/{id}")
        public ResponseEntity<ApiTemplate<Void, ProductImageDTO>> update(@PathVariable Long id,
                        @Valid @ModelAttribute ProductImageDTOIU productImageDTOIU, HttpServletRequest servletRequest) {
                ProductImageDTO updatedProductImage = productImageService.update(id, productImageDTOIU);
                return ResponseEntity.ok(
                                ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null,
                                                updatedProductImage));
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<ApiTemplate<Void, String>> delete(@PathVariable Long id,
                        HttpServletRequest servletRequest) {
                productImageService.delete(id);
                return ResponseEntity
                                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null,
                                                "Product image deleted successfully"));
        }
}
