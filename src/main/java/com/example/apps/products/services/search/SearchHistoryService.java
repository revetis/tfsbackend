package com.example.apps.products.services.search;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.apps.products.documents.SearchHistoryDocument;

import co.elastic.clients.elasticsearch._types.SortOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchHistoryService {

    private final ElasticsearchOperations elasticsearchOperations;

    @Async("taskExecutor")
    public void saveSearchHistory(Long userId, String term) {
        if (term == null || term.isBlank()) {
            return;
        }

        try {
            SearchHistoryDocument history = SearchHistoryDocument.builder()
                    .userId(userId)
                    .searchTerm(term.trim().toLowerCase())
                    .searchedAt(LocalDateTime.now())
                    .build();

            elasticsearchOperations.save(history);
            log.debug("Search history saved successfully: {}", term);
        } catch (Exception e) {
            log.error("Failed to save search history for user: {}", userId, e);
        }
    }

    public List<String> getUserSearchHistory(Long userId) {
        if (userId == null) {
            return List.of();
        }

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.term(t -> t.field("userId").value(userId)))
                .withSort(s -> s.field(f -> f.field("searchedAt").order(SortOrder.Desc)))
                .withPageable(PageRequest.of(0, 10))
                .build();

        SearchHits<SearchHistoryDocument> hits = elasticsearchOperations.search(query, SearchHistoryDocument.class);

        return hits.stream()
                .map(SearchHit::getContent)
                .map(SearchHistoryDocument::getSearchTerm)
                .distinct()
                .limit(5)
                .collect(Collectors.toList());
    }
}