package com.example.apps.products.services;

import org.springframework.data.domain.Page;

import com.example.apps.products.dtos.MainCategoryDTO;
import com.example.apps.products.dtos.MainCategoryDTOIU;
import com.example.apps.products.dtos.SubCategoryDTO;
import com.example.apps.products.dtos.SubCategoryDTOIU;

public interface ICategoryService {
    MainCategoryDTO createMainCategory(MainCategoryDTOIU mainCategoryDTO);

    MainCategoryDTO updateMainCategory(Long id, MainCategoryDTOIU mainCategoryDTO);

    Boolean deleteMainCategory(Long id);

    MainCategoryDTO getMainCategoryById(Long id);

    Page<MainCategoryDTO> getAllMainCategories(int page, int size);

    SubCategoryDTO createSubCategory(SubCategoryDTOIU subCategoryDTO);

    SubCategoryDTO updateSubCategory(Long id, SubCategoryDTOIU subCategoryDTO);

    Boolean deleteSubCategory(Long id);

    SubCategoryDTO changeMainCategoryOfSubCategory(Long subCategoryId, Long mainCategoryId);

    SubCategoryDTO getSubCategoryById(Long id);

    Page<SubCategoryDTO> getSubCategoriesByMainCategoryId(Long mainCategoryId, int page, int size);

    Page<SubCategoryDTO> getAllSubCategories(int page, int size);
}
