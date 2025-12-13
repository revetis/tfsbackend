package com.example.apps.products.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.apps.products.entities.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
