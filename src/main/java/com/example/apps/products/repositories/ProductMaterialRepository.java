package com.example.apps.products.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.apps.products.entities.ProductMaterial;

public interface ProductMaterialRepository extends JpaRepository<ProductMaterial, Long> {
}
