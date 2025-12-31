package com.example.apps.products.services.search;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import com.example.apps.products.documents.ProductDocument;
import com.example.apps.products.enums.ProductSize;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.FieldValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductSearchService {

    private final ElasticsearchOperations elasticsearchOperations;

    private final SearchHistoryService searchHistoryService;

    public List<ProductDocument> fuzzySearch(String searchTerm) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q
                        .multiMatch(m -> m
                                .fields("name^3", "description")
                                .query(searchTerm)
                                .fuzziness("AUTO")))
                .build();
        return fetchResults(query);
    }

    public com.example.apps.products.dtos.ProductSearchResponse searchWithFullFeatures(
            Long userId,
            String searchTerm,
            Double minPrice,
            Double maxPrice,
            Long mainCategoryId,
            Long subCategoryId,
            String material,
            List<String> genders, // Changed from String gender
            List<String> colors,
            List<ProductSize> sizes,
            Boolean hasDiscount,
            Pageable pageable) {

        log.info("=== ELASTICSEARCH SEARCH DEBUG ===");
        log.info(
                "Search params - term: {}, subCategoryId: {}, minPrice: {}, maxPrice: {}, material: {}, genders: {}, colors: {}, sizes: {}",
                searchTerm, subCategoryId, minPrice, maxPrice, material, genders, colors, sizes);

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> {
                            boolean hasCriteria = (searchTerm != null && !searchTerm.isBlank()) ||
                                    mainCategoryId != null ||
                                    subCategoryId != null ||
                                    (minPrice != null && minPrice >= 0) || (maxPrice != null && maxPrice >= 0) ||
                                    (material != null && !material.isBlank()) ||
                                    (genders != null && !genders.isEmpty()) ||
                                    (colors != null && !colors.isEmpty()) ||
                                    (sizes != null && !sizes.isEmpty());

                            if (!hasCriteria) {
                                b.must(m -> m.matchAll(ma -> ma));
                                b.filter(f -> f.term(t -> t.field("enable").value(true)));
                                return b;
                            }

                            // 1. MUST: Arama terimi ve Boosting
                            if (searchTerm != null && !searchTerm.isBlank()) {
                                b.must(m -> m
                                        .multiMatch(mm -> mm
                                                .fields("name^3", "description")
                                                .query(searchTerm)
                                                .fuzziness("AUTO")));
                            }

                            // 2. FILTER: Aktiflik
                            b.filter(f -> f.term(t -> t.field("enable").value(true)));

                            // 2.5 FILTER: Main Category
                            if (mainCategoryId != null) {
                                b.filter(f -> f.term(t -> t.field("mainCategoryId").value(mainCategoryId)));
                            }

                            // 3. FILTER: Kategori
                            if (subCategoryId != null) {
                                b.filter(f -> f.term(t -> t.field("subCategory.id").value(subCategoryId)));
                            }

                            // 4. FILTER: Fiyat Aralığı
                            if (minPrice != null || maxPrice != null) {
                                b.filter(f -> f.nested(n -> n
                                        .path("variants")
                                        .query(nq -> nq.range(r -> r
                                                .number(num -> {
                                                    num.field("variants.price");
                                                    if (minPrice != null)
                                                        num.gte(minPrice);
                                                    if (maxPrice != null)
                                                        num.lte(maxPrice);
                                                    return num;
                                                })))));
                            }

                            // 5. FILTER: Material
                            if (material != null && !material.isBlank()) {
                                b.filter(f -> f.term(t -> t.field("material").value(material)));
                            }

                            // 5.1. FILTER: Gender
                            if (genders != null && !genders.isEmpty()) {
                                List<FieldValue> genderValues = genders.stream()
                                        .map(FieldValue::of)
                                        .collect(Collectors.toList());
                                b.filter(f -> f
                                        .terms(t -> t.field("gender").terms(tt -> tt.value(genderValues))));
                            }

                            // 6. FILTER: Colors
                            if (colors != null && !colors.isEmpty()) {
                                List<FieldValue> colorValues = colors.stream().map(FieldValue::of)
                                        .collect(Collectors.toList());
                                b.filter(f -> f
                                        .terms(t -> t.field("colors").terms(tt -> tt.value(colorValues))));
                            }

                            // 7. FILTER: Sizes
                            if (sizes != null && !sizes.isEmpty()) {
                                List<FieldValue> sizeValues = sizes.stream().map(FieldValue::of)
                                        .collect(Collectors.toList());
                                b.filter(f -> f.terms(t -> t.field("sizes").terms(tt -> tt.value(sizeValues))));
                            }

                            // 8. FILTER: Discount
                            if (java.lang.Boolean.TRUE.equals(hasDiscount)) {
                                b.filter(f -> f.nested(n -> n
                                        .path("variants")
                                        .query(nq -> nq.range(r -> r
                                                .number(num -> num.field("variants.discountRatio").gt(0.0))))));
                            }

                            return b;
                        }))
                .withAggregation("colors_agg",
                        co.elastic.clients.elasticsearch._types.aggregations.Aggregation
                                .of(a -> a.terms(t -> t.field("colors").size(50))))
                .withAggregation("sizes_agg",
                        co.elastic.clients.elasticsearch._types.aggregations.Aggregation
                                .of(a -> a.terms(t -> t.field("sizes").size(50))))
                .withAggregation("material_agg",
                        co.elastic.clients.elasticsearch._types.aggregations.Aggregation
                                .of(a -> a.terms(t -> t.field("material").size(50))))
                .withAggregation("gender_agg",
                        co.elastic.clients.elasticsearch._types.aggregations.Aggregation
                                .of(a -> a.terms(t -> t.field("gender").size(10))))
                .withPageable(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()))
                .withSort(s -> {
                    if (pageable.getSort().isSorted()) {
                        pageable.getSort().forEach(order -> {
                            if ("price".equals(order.getProperty())) {
                                s.field(f -> f
                                        .field("variants.price")
                                        .order(order.getDirection().isAscending() ? SortOrder.Asc : SortOrder.Desc)
                                        .nested(n -> n.path("variants"))
                                        .mode(co.elastic.clients.elasticsearch._types.SortMode.Min));
                            } else {
                                s.field(f -> f
                                        .field(mapSortField(order.getProperty()))
                                        .order(order.getDirection().isAscending() ? SortOrder.Asc : SortOrder.Desc));
                            }
                        });
                    } else {
                        s.score(sc -> sc.order(SortOrder.Desc));
                    }
                    return s;
                })
                .build();

        if (searchTerm != null && !searchTerm.isBlank()) {
            searchHistoryService.saveSearchHistory(userId, searchTerm);
        }

        SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(query, ProductDocument.class);

        List<ProductDocument> products = searchHits.get().map(SearchHit::getContent).collect(Collectors.toList());

        // Process Aggregations
        java.util.Map<String, java.util.Map<String, Long>> facets = new java.util.HashMap<>();

        if (searchHits.getAggregations() != null) {
            processAggregation(searchHits, "colors_agg", facets, "colors");
            processAggregation(searchHits, "sizes_agg", facets, "sizes");
            processAggregation(searchHits, "material_agg", facets, "materials");
            processAggregation(searchHits, "gender_agg", facets, "gender");
        }

        return com.example.apps.products.dtos.ProductSearchResponse.builder()
                .products(products)
                .totalElements(searchHits.getTotalHits())
                .totalPages((int) Math.ceil((double) searchHits.getTotalHits() / pageable.getPageSize()))
                .facets(facets)
                .build();
    }

    private void processAggregation(SearchHits<ProductDocument> searchHits, String aggName,
            java.util.Map<String, java.util.Map<String, Long>> facets, String facetName) {
        if (searchHits.getAggregations() == null)
            return;

        var container = (org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations) searchHits
                .getAggregations();
        var esAggWrapper = container.get(aggName);

        if (esAggWrapper != null) {
            Object rawAggObj = esAggWrapper.aggregation();

            if (rawAggObj instanceof org.springframework.data.elasticsearch.client.elc.Aggregation) {
                var springAgg = (org.springframework.data.elasticsearch.client.elc.Aggregation) rawAggObj;
                var esAgg = springAgg.getAggregate();

                if (esAgg != null && esAgg.isSterms()) {
                    java.util.Map<String, Long> buckets = new java.util.HashMap<>();
                    esAgg.sterms().buckets().array().forEach(b -> buckets.put(b.key().stringValue(), b.docCount()));
                    facets.put(facetName, buckets);
                }
            } else if (rawAggObj instanceof co.elastic.clients.elasticsearch._types.aggregations.Aggregate) {
                var esAgg = (co.elastic.clients.elasticsearch._types.aggregations.Aggregate) rawAggObj;
                if (esAgg.isSterms()) {
                    java.util.Map<String, Long> buckets = new java.util.HashMap<>();
                    esAgg.sterms().buckets().array().forEach(b -> buckets.put(b.key().stringValue(), b.docCount()));
                    facets.put(facetName, buckets);
                }
            }
        }
    }

    private String mapSortField(String property) {
        return switch (property) {
            case "price" -> "variants.price";
            case "name" -> "name.keyword"; // ProductDocument has @MultiField with keyword suffix
            case "createdAt" -> "createdAt";
            case "id" -> "_id";
            default -> "_score";
        };
    }

    private List<ProductDocument> fetchResults(NativeQuery query) {
        SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(query, ProductDocument.class);
        return searchHits.get()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }
}