package com.example.apps.products.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.apps.products.dtos.MainCategoryDTO;
import com.example.apps.products.dtos.MainCategoryDTOIU;
import com.example.apps.products.dtos.SubCategoryDTO;
import com.example.apps.products.dtos.SubCategoryDTOIU;
import com.example.apps.products.entities.MainCategory;
import com.example.apps.products.entities.SubCategory;
import com.example.apps.products.exceptions.MainCategoryException;
import com.example.apps.products.exceptions.SubCategoryException;
import com.example.apps.products.repositories.MainCategoryRepository;
import com.example.apps.products.repositories.SubCategoryRepository;
import com.example.apps.products.services.ICategoryService;

@Service
public class CategoryServiceImpl implements ICategoryService {

        @Autowired
        private MainCategoryRepository mainCategoryRepository;
        @Autowired
        private SubCategoryRepository subCategoryRepository;

        @Override
        @Transactional
        public MainCategoryDTO createMainCategory(MainCategoryDTOIU mainCategoryDTO) {
                MainCategory mainCategory = mainCategoryRepository.findByName(mainCategoryDTO.getName()).orElse(null);

                if (mainCategory != null) {
                        throw new MainCategoryException("This name already used");
                }

                MainCategory newMainCategory = MainCategory.builder()
                                .name(mainCategoryDTO.getName())
                                .description(mainCategoryDTO.getDescription())
                                .imageUrl(mainCategoryDTO.getImageUrl())
                                .enable(true)
                                .build();
                mainCategoryRepository.save(newMainCategory);
                return MainCategoryDTO.builder()
                                .id(newMainCategory.getId())
                                .name(newMainCategory.getName())
                                .description(newMainCategory.getDescription())
                                .imageUrl(newMainCategory.getImageUrl())
                                .enable(newMainCategory.getEnable())
                                .createdAt(newMainCategory.getCreatedAt())
                                .updatedAt(newMainCategory.getUpdatedAt())
                                .build();
        }

        @Override
        @Transactional
        public MainCategoryDTO updateMainCategory(Long id, MainCategoryDTOIU mainCategoryDTO) {
                MainCategory mainCategory = mainCategoryRepository.findByName(mainCategoryDTO.getName()).orElse(null);

                if (mainCategory != null && !mainCategory.getId().equals(id)) {
                        throw new MainCategoryException("This name already used");
                }

                MainCategory newMainCategory = mainCategoryRepository.findById(id)
                                .orElseThrow(() -> new MainCategoryException("Main category not found"));
                newMainCategory.setName(mainCategoryDTO.getName());
                newMainCategory.setDescription(mainCategoryDTO.getDescription());
                newMainCategory.setImageUrl(mainCategoryDTO.getImageUrl());
                newMainCategory.setEnable(mainCategoryDTO.getEnable());
                mainCategoryRepository.save(newMainCategory);
                return MainCategoryDTO.builder()
                                .id(newMainCategory.getId())
                                .name(newMainCategory.getName())
                                .description(newMainCategory.getDescription())
                                .imageUrl(newMainCategory.getImageUrl())
                                .enable(newMainCategory.getEnable())
                                .createdAt(newMainCategory.getCreatedAt())
                                .updatedAt(newMainCategory.getUpdatedAt())
                                .build();
        }

        @Override
        @Transactional
        public Boolean deleteMainCategory(Long id) {
                MainCategory mainCategory = mainCategoryRepository.findById(id)
                                .orElseThrow(() -> new MainCategoryException("Main category not found"));
                mainCategoryRepository.delete(mainCategory);
                return true;
        }

        @Override
        public MainCategoryDTO getMainCategoryById(Long id) {
                MainCategory mainCategory = mainCategoryRepository.findById(id)
                                .orElseThrow(() -> new MainCategoryException("Main category not found"));
                return MainCategoryDTO.builder()
                                .id(mainCategory.getId())
                                .name(mainCategory.getName())
                                .description(mainCategory.getDescription())
                                .imageUrl(mainCategory.getImageUrl())
                                .enable(mainCategory.getEnable())
                                .createdAt(mainCategory.getCreatedAt())
                                .updatedAt(mainCategory.getUpdatedAt())
                                .build();
        }

        @Override
        public Page<MainCategoryDTO> getAllMainCategories(int page, int size) {
                Page<MainCategory> mainCategories = mainCategoryRepository
                                .findAll(PageRequest.of(page, size));
                return mainCategories.map(mainCategory -> MainCategoryDTO.builder()
                                .id(mainCategory.getId())
                                .name(mainCategory.getName())
                                .description(mainCategory.getDescription())
                                .imageUrl(mainCategory.getImageUrl())
                                .enable(mainCategory.getEnable())
                                .createdAt(mainCategory.getCreatedAt())
                                .updatedAt(mainCategory.getUpdatedAt())
                                .build());
        }

        @Override
        @Transactional
        public SubCategoryDTO createSubCategory(SubCategoryDTOIU subCategoryDTO) {
                MainCategory mainCategory = mainCategoryRepository.findById(subCategoryDTO.getMainCategoryId())
                                .orElseThrow(() -> new MainCategoryException("Main category not found"));

                SubCategory newSubCategory = SubCategory.builder()
                                .name(subCategoryDTO.getName())
                                .description(subCategoryDTO.getDescription())
                                .mainCategory(mainCategory)
                                .enable(true)
                                .build();

                subCategoryRepository.save(newSubCategory);

                return SubCategoryDTO.builder()
                                .id(newSubCategory.getId())
                                .name(newSubCategory.getName())
                                .description(newSubCategory.getDescription())
                                .enable(newSubCategory.getEnable())
                                .createdAt(newSubCategory.getCreatedAt())
                                .updatedAt(newSubCategory.getUpdatedAt())
                                .mainCategory(MainCategoryDTO.builder()
                                                .id(mainCategory.getId())
                                                .name(mainCategory.getName())
                                                .description(mainCategory.getDescription())
                                                .enable(mainCategory.getEnable())
                                                .createdAt(mainCategory.getCreatedAt())
                                                .updatedAt(mainCategory.getUpdatedAt())
                                                .build())
                                .mainCategoryId(mainCategory.getId())
                                .build();
        }

        @Override
        @Transactional
        public SubCategoryDTO updateSubCategory(Long id, SubCategoryDTOIU subCategoryDTO) {
                SubCategory subCategory = subCategoryRepository.findById(id)
                                .orElseThrow(() -> new SubCategoryException("Sub category not found"));

                MainCategory mainCategory = mainCategoryRepository.findById(subCategoryDTO.getMainCategoryId())
                                .orElseThrow(() -> new MainCategoryException("Main category not found"));

                subCategory.setName(subCategoryDTO.getName());
                subCategory.setDescription(subCategoryDTO.getDescription());
                subCategory.setEnable(subCategoryDTO.getEnable());
                subCategory.setMainCategory(mainCategory);

                subCategoryRepository.save(subCategory);

                return SubCategoryDTO.builder()
                                .id(subCategory.getId())
                                .name(subCategory.getName())
                                .description(subCategory.getDescription())
                                .enable(subCategory.getEnable())
                                .createdAt(subCategory.getCreatedAt())
                                .updatedAt(subCategory.getUpdatedAt())
                                .mainCategory(MainCategoryDTO.builder()
                                                .id(mainCategory.getId())
                                                .name(mainCategory.getName())
                                                .description(mainCategory.getDescription())
                                                .enable(mainCategory.getEnable())
                                                .createdAt(mainCategory.getCreatedAt())
                                                .updatedAt(mainCategory.getUpdatedAt())
                                                .build())
                                .mainCategoryId(mainCategory.getId())
                                .build();
        }

        @Override
        @Transactional
        public SubCategoryDTO changeMainCategoryOfSubCategory(Long subCategoryId, Long mainCategoryId) {
                SubCategory subCategory = subCategoryRepository.findById(subCategoryId)
                                .orElseThrow(() -> new SubCategoryException("Sub category not found"));
                MainCategory mainCategory = mainCategoryRepository.findById(mainCategoryId)
                                .orElseThrow(() -> new MainCategoryException("Main category not found"));
                subCategory.setMainCategory(mainCategory);
                subCategoryRepository.save(subCategory);
                return SubCategoryDTO.builder()
                                .id(subCategory.getId())
                                .name(subCategory.getName())
                                .description(subCategory.getDescription())
                                .enable(subCategory.getEnable())
                                .createdAt(subCategory.getCreatedAt())
                                .updatedAt(subCategory.getUpdatedAt())
                                .mainCategory(MainCategoryDTO.builder()
                                                .id(mainCategory.getId())
                                                .name(mainCategory.getName())
                                                .description(mainCategory.getDescription())
                                                .enable(mainCategory.getEnable())
                                                .createdAt(mainCategory.getCreatedAt())
                                                .updatedAt(mainCategory.getUpdatedAt())
                                                .build())
                                .mainCategoryId(mainCategory.getId())
                                .build();
        }

        @Override
        @Transactional
        public Boolean deleteSubCategory(Long id) {
                SubCategory subCategory = subCategoryRepository.findById(id)
                                .orElseThrow(() -> new SubCategoryException("Sub category not found"));
                subCategoryRepository.delete(subCategory);
                return true;
        }

        @Override
        public SubCategoryDTO getSubCategoryById(Long id) {
                SubCategory subCategory = subCategoryRepository.findById(id)
                                .orElseThrow(() -> new SubCategoryException("Sub category not found"));
                return SubCategoryDTO.builder()
                                .id(subCategory.getId())
                                .name(subCategory.getName())
                                .description(subCategory.getDescription())
                                .enable(subCategory.getEnable())
                                .createdAt(subCategory.getCreatedAt())
                                .updatedAt(subCategory.getUpdatedAt())
                                .mainCategory(MainCategoryDTO.builder()
                                                .id(subCategory.getMainCategory().getId())
                                                .name(subCategory.getMainCategory().getName())
                                                .description(subCategory.getMainCategory().getDescription())
                                                .enable(subCategory.getMainCategory().getEnable())
                                                .createdAt(subCategory.getMainCategory().getCreatedAt())
                                                .updatedAt(subCategory.getMainCategory().getUpdatedAt())
                                                .build())
                                .mainCategoryId(subCategory.getMainCategory().getId())
                                .build();
        }

        @Override
        public Page<SubCategoryDTO> getAllSubCategories(int page, int size) {
                Page<SubCategory> subCategories = subCategoryRepository
                                .findAll(PageRequest.of(page, size));
                return subCategories.map(subCategory -> SubCategoryDTO.builder()
                                .id(subCategory.getId())
                                .name(subCategory.getName())
                                .description(subCategory.getDescription())
                                .enable(subCategory.getEnable())
                                .createdAt(subCategory.getCreatedAt())
                                .updatedAt(subCategory.getUpdatedAt())
                                .mainCategory(MainCategoryDTO.builder()
                                                .id(subCategory.getMainCategory().getId())
                                                .name(subCategory.getMainCategory().getName())
                                                .description(subCategory.getMainCategory().getDescription())
                                                .enable(subCategory.getMainCategory().getEnable())
                                                .createdAt(subCategory.getMainCategory().getCreatedAt())
                                                .updatedAt(subCategory.getMainCategory().getUpdatedAt())
                                                .build())
                                .mainCategoryId(subCategory.getMainCategory().getId())
                                .build());
        }

        @Override
        public Page<SubCategoryDTO> getSubCategoriesByMainCategoryId(Long mainCategoryId, int page, int size) {
                Page<SubCategory> subCategories = subCategoryRepository
                                .findAllByMainCategoryId(mainCategoryId, PageRequest.of(page, size));
                return subCategories.map(subCategory -> SubCategoryDTO.builder()
                                .id(subCategory.getId())
                                .name(subCategory.getName())
                                .description(subCategory.getDescription())
                                .enable(subCategory.getEnable())
                                .createdAt(subCategory.getCreatedAt())
                                .updatedAt(subCategory.getUpdatedAt())
                                .mainCategory(MainCategoryDTO.builder()
                                                .id(subCategory.getMainCategory().getId())
                                                .name(subCategory.getMainCategory().getName())
                                                .description(subCategory.getMainCategory().getDescription())
                                                .enable(subCategory.getMainCategory().getEnable())
                                                .createdAt(subCategory.getMainCategory().getCreatedAt())
                                                .updatedAt(subCategory.getMainCategory().getUpdatedAt())
                                                .build())
                                .mainCategoryId(subCategory.getMainCategory().getId())
                                .build());
        }

}
