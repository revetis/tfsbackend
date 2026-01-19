package com.example.apps.products.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.apps.products.entities.ProductMaterial;

@Repository
public interface ProductMaterialRepository
        extends JpaRepository<ProductMaterial, Long>, JpaSpecificationExecutor<ProductMaterial> {

}
