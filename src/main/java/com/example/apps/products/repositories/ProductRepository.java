package com.example.apps.products.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.apps.products.entities.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
