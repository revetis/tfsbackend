package com.example.apps.products.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.apps.products.entities.ProductVariantStock;

@Repository
public interface ProductVariantStockRepository extends JpaRepository<ProductVariantStock, Long> {

}
