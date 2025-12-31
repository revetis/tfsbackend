package com.example.apps.products.dtos;

import java.util.List;
import java.util.Map;

import com.example.apps.products.documents.ProductDocument;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductSearchResponse {
    private List<ProductDocument> products;
    private long totalElements;
    private int totalPages;
    private Map<String, Map<String, Long>> facets;
}
