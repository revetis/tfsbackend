package com.example.apps.products.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.apps.products.entities.Color;

public interface ColorRepository extends JpaRepository<Color, Long> {
}
