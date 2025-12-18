package com.example.apps.products.documents;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(indexName = "search_history")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchHistoryDocument {
    @Id
    private String id;
    private Long userId; // Misafir kullanıcılar için null olabilir efendim
    private String searchTerm;
    private LocalDateTime searchedAt;
}