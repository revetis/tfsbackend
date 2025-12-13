package com.example.apps.products.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.apps.products.dtos.ProductMaterialDTO;
import com.example.apps.products.dtos.ProductMaterialDTOIU;
import com.example.apps.products.entities.Product;
import com.example.apps.products.entities.ProductMaterial;
import com.example.apps.products.repositories.ProductMaterialRepository;
import com.example.apps.products.repositories.ProductRepository;
import com.example.apps.products.services.IProductMaterialService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductMaterialServiceImpl implements IProductMaterialService {

    private final ProductMaterialRepository productMaterialRepository;
    private final ProductRepository productRepository;

    @Override
    public List<ProductMaterialDTO> getAll() {
        return productMaterialRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ProductMaterialDTO getById(Long id) {
        ProductMaterial material = productMaterialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProductMaterial not found with id: " + id));
        return mapToDTO(material);
    }

    @Transactional
    @Override
    public ProductMaterialDTO create(ProductMaterialDTOIU dto) {
        ProductMaterial material = new ProductMaterial();
        mapToEntity(material, dto);
        ProductMaterial savedMaterial = productMaterialRepository.save(material);
        return mapToDTO(savedMaterial);
    }

    @Transactional
    @Override
    public ProductMaterialDTO update(Long id, ProductMaterialDTOIU dto) {
        ProductMaterial material = productMaterialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProductMaterial not found with id: " + id));
        mapToEntity(material, dto);
        ProductMaterial savedMaterial = productMaterialRepository.save(material);
        return mapToDTO(savedMaterial);
    }

    @Override
    public void delete(Long id) {
        if (!productMaterialRepository.existsById(id)) {
            throw new RuntimeException("ProductMaterial not found with id: " + id);
        }
        productMaterialRepository.deleteById(id);
    }

    private ProductMaterialDTO mapToDTO(ProductMaterial material) {
        if (material == null)
            return null;
        return new ProductMaterialDTO(
                material.getId(),
                material.getName(),
                material.getDescription(),
                material.getIsActive());
    }

    private void mapToEntity(ProductMaterial material, ProductMaterialDTOIU dto) {
        material.setName(dto.getName());
        material.setDescription(dto.getDescription());
        material.setIsActive(dto.getIsActive());

        if (dto.getProductId() != null) {
            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + dto.getProductId()));
            material.setProduct(product);
        }
    }
}
