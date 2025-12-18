package com.example.apps.products.events;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.apps.products.documents.ProductDocument;
import com.example.apps.products.dtos.search.ProductSavedEvent;
import com.example.apps.products.mappers.ProductMapper;
import com.example.apps.products.repositories.search.ProductDocumentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductSyncListener {

    private final ProductDocumentRepository elasticRepository;
    private final ProductMapper productMapper;

    @Async // Ana thread'i bloklamıyoruz efendim, performans bizim için her şeydir
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductSaveEvent(ProductSavedEvent event) {
        log.info("Elasticsearch syncronization is started: Product ID {}", event.getProduct().getId());

        ProductDocument doc = productMapper.toDocument(event.getProduct());
        elasticRepository.save(doc);

        log.info("Elasticsearch syncronization is completed: Product ID {}", event.getProduct().getId());
    }
}