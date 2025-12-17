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

import com.example.apps.products.dtos.ProductDTO;
import com.example.apps.products.dtos.ProductDTOIU;
import com.example.apps.products.services.IProductService;
import com.example.tfs.maindto.ApiTemplate;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/api/admin/products")
@RequiredArgsConstructor
@Validated
public class ProductAdminController {

        private final IProductService productService;

        @GetMapping
        public ResponseEntity<ApiTemplate<Void, List<ProductDTO>>> getAll(HttpServletRequest servletRequest) {
                List<ProductDTO> products = productService.getAll();
                return ResponseEntity
                                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null,
                                                products));
        }

        @GetMapping("/{id}")
        public ResponseEntity<ApiTemplate<Void, ProductDTO>> getById(@PathVariable Long id,
                        HttpServletRequest servletRequest) {
                ProductDTO product = productService.getById(id);
                return ResponseEntity
                                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null,
                                                product));
        }

        @PostMapping
        public ResponseEntity<ApiTemplate<Void, ProductDTO>> create(@Valid @RequestBody ProductDTOIU productDTOIU,
                        HttpServletRequest servletRequest) {
                ProductDTO product = productService.create(productDTOIU);
                return ResponseEntity
                                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null,
                                                product));
        }

        @PutMapping("/{id}")
        public ResponseEntity<ApiTemplate<Void, ProductDTO>> update(@PathVariable Long id,
                        @Valid @RequestBody ProductDTOIU productDTOIU, HttpServletRequest servletRequest) {
                ProductDTO product = productService.update(id, productDTOIU);
                return ResponseEntity
                                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null,
                                                product));
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<ApiTemplate<Void, String>> delete(@PathVariable Long id,
                        HttpServletRequest servletRequest) {
                productService.delete(id);
                return ResponseEntity
                                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null,
                                                "Product deleted successfully"));
        }
}
