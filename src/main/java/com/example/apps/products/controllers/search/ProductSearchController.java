package com.example.apps.products.controllers.search;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.apps.products.documents.ProductDocument;
import com.example.apps.products.services.search.ProductSearchService;
import com.example.apps.products.services.search.SearchHistoryService;
import com.example.tfs.maindto.ApiTemplate;

import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("rest/api/public/products/search")
@RequiredArgsConstructor
public class ProductSearchController {

        private final ProductSearchService productSearchService;
        private final SearchHistoryService searchHistoryService;

        @GetMapping
        public ResponseEntity<ApiTemplate<Object, List<ProductDocument>>> search(
                        @RequestParam(required = false) Long userId,
                        @RequestParam(required = false) String term,
                        @RequestParam(required = false) Double minPrice,
                        @RequestParam(required = false) Double maxPrice,
                        @RequestParam(required = false) Long subCategoryId,
                        @RequestParam(required = false) String material,
                        @RequestParam(required = false) List<String> colors,
                        @RequestParam(required = false) List<String> sizes,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "name") String sortBy,
                        @RequestParam(defaultValue = "ASC") String sortDir) {

                Sort.Direction direction = sortDir.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
                Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

                List<ProductDocument> results = productSearchService.searchWithFullFeatures(
                                userId, term, minPrice, maxPrice, subCategoryId, material, colors, sizes, pageable);

                return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                                true,
                                HttpResponseStatus.OK.code(),
                                "rest/api/public/products/search",
                                null,
                                results));
        }

        @GetMapping("/history")
        public ResponseEntity<ApiTemplate<Object, List<String>>> getHistory(@RequestParam Long userId) {
                List<String> history = searchHistoryService.getUserSearchHistory(userId);

                return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                                true,
                                HttpResponseStatus.OK.code(),
                                "rest/api/public/products/search/history",
                                null,
                                history));
        }
}