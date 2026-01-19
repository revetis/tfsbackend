package com.example.apps.products.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.apps.products.entities.Product;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import com.example.apps.dashboard.dtos.StatusDistributionDTO;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    long countByEnableTrue();

    @Query("SELECT new com.example.apps.dashboard.dtos.StatusDistributionDTO(mc.name, COUNT(p), '') " +
            "FROM Product p JOIN p.subCategory sc JOIN sc.mainCategory mc GROUP BY mc.name")
    List<StatusDistributionDTO> findCategoryDistribution();
}
