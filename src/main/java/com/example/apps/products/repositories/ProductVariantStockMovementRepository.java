package com.example.apps.products.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.apps.products.entities.ProductVariantStockMovement;

@Repository
public interface ProductVariantStockMovementRepository extends JpaRepository<ProductVariantStockMovement, Long> {

}
