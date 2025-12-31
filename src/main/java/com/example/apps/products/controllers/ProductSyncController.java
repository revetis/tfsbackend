package com.example.apps.products.controllers;

import com.example.apps.products.documents.ProductDocument;
import com.example.apps.products.entities.Product;
import com.example.apps.products.mappers.ProductMapper;
import com.example.apps.products.repositories.ProductRepository;
import com.example.apps.products.repositories.search.ProductDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/rest/api/public/products-sync")
@RequiredArgsConstructor
public class ProductSyncController {

    private final ProductRepository productRepository;
    private final ProductDocumentRepository productDocumentRepository;
    private final ProductMapper productMapper;

    @PostMapping
    @Transactional(readOnly = true)
    public ResponseEntity<String> syncAll() {
        List<Product> products = productRepository.findAll();
        List<ProductDocument> documents = products.stream()
                .map(productMapper::toDocument)
                .collect(Collectors.toList());

        productDocumentRepository.saveAll(documents);

        return ResponseEntity.ok("Synced " + documents.size() + " products.");
    }
}
