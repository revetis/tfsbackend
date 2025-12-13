package com.example.apps.products.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.example.settings.utils.SlugUtils;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.apps.products.dtos.CategoryDTO;
import com.example.apps.products.dtos.CategoryDTOIU;
import com.example.apps.products.entities.Category;
import com.example.apps.products.repositories.CategoryRepository;
import com.example.apps.products.services.ICategoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements ICategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryDTO> getAll() {
        return categoryRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDTO getById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        return mapToDTO(category);
    }

    @Transactional
    @Override
    public CategoryDTO create(CategoryDTOIU categoryDTOIU) {
        Category category = new Category();
        mapToEntity(category, categoryDTOIU);
        Category savedCategory = categoryRepository.save(category);
        return mapToDTO(savedCategory);
    }

    @Transactional
    @Override
    public CategoryDTO update(Long id, CategoryDTOIU categoryDTOIU) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        mapToEntity(category, categoryDTOIU);
        Category savedCategory = categoryRepository.save(category);
        return mapToDTO(savedCategory);
    }

    @Override
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }

    private CategoryDTO mapToDTO(Category category) {
        if (category == null)
            return null;
        return new CategoryDTO(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getSlug(),
                category.getImage(),
                category.getIsActive());
    }

    private void mapToEntity(Category category, CategoryDTOIU dto) {
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setSlug(SlugUtils.toSlug(dto.getName()));
        category.setImage(dto.getImage());

        category.setIsActive(dto.getIsActive());
    }
}
