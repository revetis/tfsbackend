package com.example.apps.products.services.search;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import com.example.apps.products.documents.ProductDocument;

import co.elastic.clients.elasticsearch._types.SortOrder;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductSearchService {

    private final ElasticsearchOperations elasticsearchOperations;

    private final SearchHistoryService searchHistoryService;

    /**
     * Efendim, en basit haliyle hızlıca arama yapmak isteyenler için
     * sizin o meşhur fuzzy search metodunuz.
     */
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

    /**
     * İşte projenizin amiral gemisi!
     * Filtreleme, Sıralama ve Sayfalama özelliklerinin muazzam uyumu efendim.
     */
    public List<ProductDocument> searchWithFullFeatures(
            Long userId,
            String searchTerm,
            Double minPrice,
            Double maxPrice,
            Long subCategoryId,
            String material,
            List<String> colors,
            List<String> sizes,
            Pageable pageable) {

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> {
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

                            // 3. FILTER: Kategori
                            if (subCategoryId != null) {
                                b.filter(f -> f.term(t -> t.field("subCategory.id").value(subCategoryId)));
                            }

                            // 4. FILTER: Fiyat Aralığı
                            if (minPrice != null || maxPrice != null) {
                                b.filter(f -> f.range(r -> r
                                        .number(n -> {
                                            n.field("variants.price");
                                            if (minPrice != null)
                                                n.gte(minPrice);
                                            if (maxPrice != null)
                                                n.lte(maxPrice);
                                            return n;
                                        })));
                            }

                            return b;
                        }))
                // Sayfalama (Pagination) desteği efendim
                .withPageable(pageable)
                // Sıralama (Sorting) desteği efendim
                .withSort(s -> {
                    if (pageable.getSort().isSorted()) {
                        pageable.getSort().forEach(order -> {
                            s.field(f -> f
                                    .field(mapSortField(order.getProperty()))
                                    .order(order.getDirection().isAscending() ? SortOrder.Asc : SortOrder.Desc));
                        });
                    } else {
                        s.score(sc -> sc.order(SortOrder.Desc)); // Varsayılan: En alakalı olan efendim
                    }
                    return s;
                })
                .build();
        if (searchTerm != null) {
            searchHistoryService.saveSearchHistory(userId, searchTerm);
        }

        return fetchResults(query);
    }

    /**
     * Sıralama yapılacak alanları Elasticsearch formatına çeviren
     * usta işi bir yardımcı metod efendim.
     */
    private String mapSortField(String property) {
        return switch (property) {
            case "price" -> "variants.price";
            case "name" -> "name.keyword";
            case "createdAt" -> "createdAt";
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