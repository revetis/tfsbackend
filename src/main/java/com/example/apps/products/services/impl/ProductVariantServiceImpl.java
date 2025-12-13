package com.example.apps.products.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.apps.products.dtos.ProductVariantDTO;
import com.example.apps.products.dtos.ProductVariantDTOIU;
import com.example.apps.products.entities.Color;
import com.example.apps.products.entities.Product;
import com.example.apps.products.entities.ProductVariant;
import com.example.apps.products.repositories.ColorRepository;
import com.example.apps.products.repositories.ProductRepository;
import com.example.apps.products.repositories.ProductVariantRepository;
import com.example.apps.products.services.IProductVariantService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductVariantServiceImpl implements IProductVariantService {

    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;
    private final ColorRepository colorRepository;

    @Override
    public List<ProductVariantDTO> getAll() {
        return productVariantRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ProductVariantDTO getById(Long id) {
        ProductVariant variant = productVariantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProductVariant not found with id: " + id));
        return mapToDTO(variant);
    }

    @Transactional
    @Override
    public ProductVariantDTO create(ProductVariantDTOIU dto) {
        ProductVariant variant = new ProductVariant();
        mapToEntity(variant, dto);
        ProductVariant savedVariant = productVariantRepository.save(variant);
        return mapToDTO(savedVariant);
    }

    @Transactional
    @Override
    public ProductVariantDTO update(Long id, ProductVariantDTOIU dto) {
        ProductVariant variant = productVariantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProductVariant not found with id: " + id));
        mapToEntity(variant, dto);
        ProductVariant savedVariant = productVariantRepository.save(variant);
        return mapToDTO(savedVariant);
    }

    @Override
    public void delete(Long id) {
        if (!productVariantRepository.existsById(id)) {
            throw new RuntimeException("ProductVariant not found with id: " + id);
        }
        productVariantRepository.deleteById(id);
    }

    private ProductVariantDTO mapToDTO(ProductVariant variant) {
        if (variant == null)
            return null;
        return new ProductVariantDTO(
                variant.getId(),
                variant.getVariantPrice(),
                variant.getStock(),
                variant.getSize(),
                variant.getSku(),
                variant.getProduct() != null ? variant.getProduct().getId() : null,
                variant.getColor() != null ? variant.getColor().getId() : null,
                variant.getIsActive());
    }

    private void mapToEntity(ProductVariant variant, ProductVariantDTOIU dto) {
        variant.setVariantPrice(dto.getVariantPrice());
        variant.setStock(dto.getStock());
        variant.setSize(dto.getSize());
        // Variant doesn't have a direct name for slug usually, maybe SKU?
        // But the previous request asked for slug on variants too if applicable.
        // Checking entity: Variant DOES NOT have a slug field based on previous
        // context, but let's check view_file.
        // Wait, ProductVariant entity was NOT viewed recently, let me check strict
        // requirement.
        // The user said "once slug olusturmayi coz".
        // Product, Brand, Category, ProductImage, Color have slugs.
        // ProductVariant has 'sku', not slug.
        variant.setIsActive(dto.getIsActive());

        if (dto.getProductId() != null) {
            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + dto.getProductId()));
            variant.setProduct(product);
        }

        if (dto.getColorId() != null) {
            Color color = colorRepository.findById(dto.getColorId())
                    .orElseThrow(() -> new RuntimeException("Color not found with id: " + dto.getColorId()));
            variant.setColor(color);
        }
    }
}
