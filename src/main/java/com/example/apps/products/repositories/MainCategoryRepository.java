package com.example.apps.products.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.apps.products.entities.MainCategory;

@Repository
public interface MainCategoryRepository extends JpaRepository<MainCategory, Long> {

    Optional<MainCategory> findByName(String name);

}
