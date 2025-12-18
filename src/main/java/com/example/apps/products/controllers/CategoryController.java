package com.example.apps.products.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.apps.products.dtos.MainCategoryDTO;
import com.example.apps.products.dtos.MainCategoryDTOIU;
import com.example.apps.products.dtos.SubCategoryDTO;
import com.example.apps.products.dtos.SubCategoryDTOIU;
import com.example.apps.products.services.ICategoryService;
import com.example.tfs.maindto.ApiTemplate;

import io.netty.handler.codec.http.HttpResponseStatus;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@Validated
@RequestMapping("/rest/api/admin/categories")
public class CategoryController {

        @Autowired
        private ICategoryService categoryService;

        @GetMapping("/main-category/all")
        public ResponseEntity<?> getAllMainCategories(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {

                return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                                true,
                                HttpResponseStatus.OK.code(),
                                "/main-category/get-all",
                                null,
                                categoryService.getAllMainCategories(page, size)));
        }

        @PostMapping("/main-category/create")
        public ResponseEntity<?> createMainCategory(@RequestBody @Valid MainCategoryDTOIU dto) {
                MainCategoryDTO category = categoryService.createMainCategory(dto);
                return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                                true, HttpResponseStatus.CREATED.code(),
                                "/main-category/create", null, category));
        }

        @PutMapping("/main-category/update/{id}")
        public ResponseEntity<?> updateMainCategory(@PathVariable Long id,
                        @RequestBody @Valid MainCategoryDTOIU dto) {
                MainCategoryDTO category = categoryService.updateMainCategory(id, dto);
                return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                                true, HttpResponseStatus.OK.code(),
                                "/main-category/update", null, category));
        }

        @DeleteMapping("/main-category/delete/{id}")
        public ResponseEntity<?> deleteMainCategory(@PathVariable Long id) {
                return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                                true, HttpResponseStatus.OK.code(),
                                "/main-category/delete", null,
                                categoryService.deleteMainCategory(id)));
        }

        @GetMapping("/main-category/get/{id}")
        public ResponseEntity<?> getMainCategory(@PathVariable Long id) {
                return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                                true, HttpResponseStatus.OK.code(),
                                "/main-category/get", null,
                                categoryService.getMainCategoryById(id)));
        }

        @PostMapping("/sub-category/create")
        public ResponseEntity<?> createSubCategory(@RequestBody @Valid SubCategoryDTOIU dto) {
                SubCategoryDTO subCategory = categoryService.createSubCategory(dto);
                return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                                true, HttpResponseStatus.CREATED.code(),
                                "/sub-category/create", null, subCategory));
        }

        @PutMapping("/sub-category/update/{id}")
        public ResponseEntity<?> updateSubCategory(@PathVariable Long id,
                        @RequestBody @Valid SubCategoryDTOIU dto) {
                SubCategoryDTO subCategory = categoryService.updateSubCategory(id, dto);
                return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                                true, HttpResponseStatus.OK.code(),
                                "/sub-category/update", null, subCategory));
        }

        @DeleteMapping("/sub-category/delete/{id}")
        public ResponseEntity<?> deleteSubCategory(@PathVariable Long id) {
                return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                                true, HttpResponseStatus.OK.code(),
                                "/sub-category/delete", null,
                                categoryService.deleteSubCategory(id)));
        }

        @GetMapping("/sub-category/get/{id}")
        public ResponseEntity<?> getSubCategory(@PathVariable Long id) {
                return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                                true, HttpResponseStatus.OK.code(),
                                "/sub-category/get", null,
                                categoryService.getSubCategoryById(id)));
        }

        @PutMapping("/sub-category/change-main-category")
        public ResponseEntity<?> changeMainCategory(
                        @RequestParam Long subCategoryId,
                        @RequestParam Long mainCategoryId) {

                SubCategoryDTO subCategory = categoryService.changeMainCategoryOfSubCategory(subCategoryId,
                                mainCategoryId);

                return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                                true, HttpResponseStatus.OK.code(),
                                "/sub-category/change-main-category", null, subCategory));
        }

        @GetMapping("/sub-category/get-all")
        public ResponseEntity<?> getAllSubCategories(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {

                return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                                true,
                                HttpResponseStatus.OK.code(),
                                "/sub-category/get-all",
                                null,
                                categoryService.getAllSubCategories(page, size)));
        }

        @GetMapping("/sub-category/get-by-main-category/{mainCategoryId}")
        public ResponseEntity<?> getSubCategoriesByMainCategory(
                        @PathVariable Long mainCategoryId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {

                return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                                true,
                                HttpResponseStatus.OK.code(),
                                "/sub-category/get-by-main-category",
                                null,
                                categoryService.getSubCategoriesByMainCategoryId(mainCategoryId, page, size)));
        }

}
