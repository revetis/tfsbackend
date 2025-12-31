package com.example.apps.settings.repositories;

import com.example.apps.settings.entities.FAQ;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FAQRepository extends JpaRepository<FAQ, Long> {
    List<FAQ> findByActiveOrderByDisplayOrderAsc(Boolean active);

    List<FAQ> findByCategoryAndActiveOrderByDisplayOrderAsc(String category, Boolean active);

    List<FAQ> findAllByOrderByDisplayOrderAsc();
}
