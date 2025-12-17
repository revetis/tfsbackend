package com.example.apps.products.services.impl;

import java.util.List;

import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.apps.products.dtos.ProductDTO;
import com.example.apps.products.dtos.ProductDTOIU;
import com.example.apps.products.entities.Brand;
import com.example.apps.products.entities.Category;
import com.example.apps.products.entities.Product;
import com.example.apps.products.events.ProductEvent;
import com.example.apps.products.messaging.ProductEventProducer;
import com.example.apps.products.repositories.BrandRepository;
import com.example.apps.products.repositories.CategoryRepository;
import com.example.apps.products.repositories.ProductRepository;
import com.example.apps.products.services.IProductService;
import com.example.tfs.utils.SlugUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements IProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductEventProducer productEventProducer;

    @Override
    public List<ProductDTO> getAll() {
        return productRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ProductDTO getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        return mapToDTO(product);
    }

    @Transactional
    @Override
    public ProductDTO create(ProductDTOIU productDTOIU) {
        Product product = new Product();
        mapToEntity(product, productDTOIU);
        Product savedProduct = productRepository.save(product);
        productEventProducer.sendEvent(new ProductEvent(savedProduct.getId(), ProductEvent.EventType.CREATE));
        return mapToDTO(savedProduct);
    }

    @Transactional
    @Override
    public ProductDTO update(Long id, ProductDTOIU productDTOIU) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        mapToEntity(product, productDTOIU);
        Product savedProduct = productRepository.save(product);
        productEventProducer.sendEvent(new ProductEvent(savedProduct.getId(), ProductEvent.EventType.UPDATE));
        return mapToDTO(savedProduct);
    }

    @Override
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
        productEventProducer.sendEvent(new ProductEvent(id, ProductEvent.EventType.DELETE));
    }

    private ProductDTO mapToDTO(Product product) {
        if (product == null)
            return null;
        return new ProductDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getSlug(),
                product.getMainPrice(),
                product.getDiscountRatio(),
                product.getCategory() != null ? product.getCategory().getId() : null,
                product.getBrand() != null ? product.getBrand().getId() : null,
                product.getIsActive());
    }

    private void mapToEntity(Product product, ProductDTOIU dto) {
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setSlug(SlugUtils.toSlug(dto.getName()));
        product.setMainPrice(dto.getMainPrice());
        product.setDiscountRatio(dto.getDiscountRatio());
        product.setIsActive(dto.getIsActive());

        // Handle Relationships
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + dto.getCategoryId()));
            product.setCategory(category);
        }

        if (dto.getBrandId() != null) {
            Brand brand = brandRepository.findById(dto.getBrandId())
                    .orElseThrow(() -> new RuntimeException("Brand not found with id: " + dto.getBrandId()));
            product.setBrand(brand);
        }
        // Slug generation logic could be improved here, but assuming it's handled
        // elsewhere or relying on pre-persist if null,
        // but DTO doesn't pass slug for update usually.
        // For now, simple mapping.
    }
}
