package com.example.apps.orders.services.search;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

import com.example.apps.orders.documents.OrderDocument;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderSearchService {

    private final ElasticsearchOperations elasticsearchOperations;

    public List<OrderDocument> searchOrders(String orderNumber, Long userId, String status,
            String paymentStatus, LocalDateTime startDate,
            LocalDateTime endDate) {

        Criteria criteria = new Criteria();

        if (orderNumber != null && !orderNumber.isBlank()) {
            criteria = criteria.and(new Criteria("orderNumber").is(orderNumber));
        }

        if (userId != null) {
            criteria = criteria.and(new Criteria("userId").is(userId));
        }

        if (status != null && !status.isBlank()) {
            criteria = criteria.and(new Criteria("status").is(status));
        }

        if (paymentStatus != null && !paymentStatus.isBlank()) {
            criteria = criteria.and(new Criteria("paymentStatus").is(paymentStatus));
        }

        if (startDate != null) {
            criteria = criteria.and(new Criteria("createdAt").greaterThanEqual(startDate));
        }

        if (endDate != null) {
            criteria = criteria.and(new Criteria("createdAt").lessThanEqual(endDate));
        }

        CriteriaQuery query = new CriteriaQuery(criteria);
        SearchHits<OrderDocument> hits = elasticsearchOperations.search(query, OrderDocument.class);

        return hits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }
}
