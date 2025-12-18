package com.example.apps.products.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.apps.products.entities.SubCategory;

public interface SubCategoryRepository extends JpaRepository<SubCategory, Long> {

    Optional<SubCategory> findByName(String name);

    Page<SubCategory> findAllByMainCategoryId(Long mainCategoryId, PageRequest of);

}
