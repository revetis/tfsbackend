package com.example.apps.products.repositories.search;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.example.apps.products.documents.ProductDocument;
import java.util.List;

public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, Long> {

    List<ProductDocument> findByNameContaining(String name);

    List<ProductDocument> findByCategorySlug(String categorySlug);

    List<ProductDocument> findByBrandSlug(String brandSlug);
}
