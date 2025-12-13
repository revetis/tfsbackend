package com.example.apps.products.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.apps.products.entities.ProductImage;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
}
