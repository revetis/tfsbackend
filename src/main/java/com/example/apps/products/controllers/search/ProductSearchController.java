package com.example.apps.products.controllers.search;

import java.util.List;

import org.apache.hc.core5.http.HttpStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.apps.products.documents.ProductDocument;
import com.example.apps.products.enums.ProductSize;
import com.example.apps.products.services.search.ProductSearchService;
import com.example.apps.products.services.search.SearchHistoryService;
import com.example.tfs.maindto.ApiTemplate;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("rest/api/public/products/search")
@RequiredArgsConstructor
public class ProductSearchController {

        private final ProductSearchService productSearchService;
        private final SearchHistoryService searchHistoryService;

        @GetMapping
        public ResponseEntity<ApiTemplate<Object, com.example.apps.products.dtos.ProductSearchResponse>> search(
                        @RequestParam(required = false) Long userId,
                        @RequestParam(required = false) String term,
                        @RequestParam(required = false) Double minPrice,
                        @RequestParam(required = false) Double maxPrice,
                        @RequestParam(required = false) Long mainCategoryId,
                        @RequestParam(required = false) Long subCategoryId,
                        @RequestParam(required = false) String material,
                        @RequestParam(required = false) List<String> gender, // List param
                        @RequestParam(required = false) List<String> colors,
                        @RequestParam(required = false) List<ProductSize> sizes,
                        @RequestParam(required = false) Boolean hasDiscount,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "id") String sortBy,
                        @RequestParam(defaultValue = "ASC") String sortDir) {

                Sort.Direction direction = sortDir.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
                Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

                com.example.apps.products.dtos.ProductSearchResponse results = productSearchService
                                .searchWithFullFeatures(
                                                userId, term, minPrice, maxPrice, mainCategoryId, subCategoryId,
                                                material, gender,
                                                colors,
                                                sizes, hasDiscount, pageable);

                return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                                true,
                                HttpStatus.SC_OK,
                                "rest/api/public/products/search",
                                null,
                                results));
        }

        @GetMapping("/history")
        public ResponseEntity<ApiTemplate<Object, List<String>>> getHistory(@RequestParam Long userId) {
                List<String> history = searchHistoryService.getUserSearchHistory(userId);

                return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                                true,
                                HttpStatus.SC_OK,
                                "rest/api/public/products/search/history",
                                null,
                                history));
        }
}