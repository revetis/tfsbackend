package com.example.apps.products.repositories.search;

import com.example.apps.products.documents.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductDocumentRepository extends ElasticsearchRepository<ProductDocument, String> {
    // Efendim, buraya özel arama metotlarınızı (findByName gibi) daha sonra
    // ekleyeceğiz.
}