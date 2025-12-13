package com.example.apps.products.services;

import java.util.List;
import com.example.apps.products.dtos.CategoryDTO;
import com.example.apps.products.dtos.CategoryDTOIU;

public interface ICategoryService {
    List<CategoryDTO> getAll();

    CategoryDTO getById(Long id);

    CategoryDTO create(CategoryDTOIU categoryDTOIU);

    CategoryDTO update(Long id, CategoryDTOIU categoryDTOIU);

    void delete(Long id);
}
