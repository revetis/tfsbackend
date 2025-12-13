package com.example.apps.products.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.apps.products.entities.Brand;

public interface BrandRepository extends JpaRepository<Brand, Long> {
}
