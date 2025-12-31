package com.example.apps.settings.repositories;

import com.example.apps.settings.entities.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageRepository extends JpaRepository<Page, Long> {
    Optional<Page> findBySlugAndActive(String slug, Boolean active);

    List<Page> findByActiveOrderByDisplayOrderAsc(Boolean active);

    List<Page> findByShowInFooterTrueAndActiveTrueOrderByDisplayOrderAsc();

    boolean existsBySlug(String slug);
}
