package com.example.apps.products.controllers;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.apps.products.documents.ProductDocument;
import com.example.apps.products.dtos.ProductDTO;
import com.example.apps.products.services.IProductService;
import com.example.apps.products.services.search.ProductSearchService;
import com.example.tfs.maindto.ApiTemplate;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/api/public/products")
@RequiredArgsConstructor
public class ProductPublicController {

        private final IProductService productService;
        private final ProductSearchService productSearchService;

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

        @GetMapping("/search")
        public ResponseEntity<ApiTemplate<Void, List<ProductDocument>>> search(
                        @RequestParam(required = false) String q,
                        @RequestParam(required = false) String category,
                        @RequestParam(required = false) String brand,
                        @RequestParam(required = false) BigDecimal minPrice,
                        @RequestParam(required = false) BigDecimal maxPrice,
                        @RequestParam(required = false) List<String> colors,
                        HttpServletRequest servletRequest) {
                List<ProductDocument> results = productSearchService.searchProducts(q, category, brand, minPrice,
                                maxPrice,
                                colors);
                return ResponseEntity
                                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null,
                                                results));
        }
}
