package com.example.apps.orders.repositories.search;

import java.util.List;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.example.apps.orders.documents.OrderDocument;

public interface OrderSearchRepository extends ElasticsearchRepository<OrderDocument, Long> {

    List<OrderDocument> findByOrderNumber(String orderNumber);

    List<OrderDocument> findByUserId(Long userId);

    List<OrderDocument> findByStatus(String status);

    List<OrderDocument> findByPaymentStatus(String paymentStatus);
}
