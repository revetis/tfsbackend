package com.example.apps.products.services.search;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

import com.example.apps.products.documents.ProductDocument;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductSearchService {

    private final ElasticsearchOperations elasticsearchOperations;

    public List<ProductDocument> searchProducts(String query, String categorySlug, String brandSlug,
            BigDecimal minPrice, BigDecimal maxPrice,
            List<String> colors) {

        Criteria criteria = new Criteria();

        if (query != null && !query.isBlank()) {
            criteria = criteria.and(new Criteria("name").contains(query)
                    .or(new Criteria("description").contains(query)));
        }

        if (categorySlug != null && !categorySlug.isBlank()) {
            criteria = criteria.and(new Criteria("categorySlug").is(categorySlug));
        }

        if (brandSlug != null && !brandSlug.isBlank()) {
            criteria = criteria.and(new Criteria("brandSlug").is(brandSlug));
        }

        if (minPrice != null) {
            criteria = criteria.and(new Criteria("mainPrice").greaterThanEqual(minPrice));
        }

        if (maxPrice != null) {
            criteria = criteria.and(new Criteria("mainPrice").lessThanEqual(maxPrice));
        }

        if (colors != null && !colors.isEmpty()) {
            criteria = criteria.and(new Criteria("colorSlugs").in(colors));
        }

        // If no criteria, find all (or handle as needed)
        // Spring Data ES CriteriaQuery requires at least one criteria or match all?
        // CriteriaQuery q = new CriteriaQuery(criteria);
        // If criteria is empty, we might want matchAll.

        org.springframework.data.elasticsearch.core.query.Query searchQuery = new CriteriaQuery(criteria);

        SearchHits<ProductDocument> hits = elasticsearchOperations.search(searchQuery, ProductDocument.class);
        return hits.stream().map(SearchHit::getContent).collect(Collectors.toList());
    }
}
