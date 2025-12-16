package com.example.apps.products.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.apps.products.dtos.CategoryDTO;
import com.example.apps.products.dtos.CategoryDTOIU;
import com.example.apps.products.services.ICategoryService;
import com.example.settings.maindto.ApiTemplate;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/api/admin/categories")
@RequiredArgsConstructor
@Validated
public class CategoryAdminController {

    private final ICategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiTemplate<Void, List<CategoryDTO>>> getAll(HttpServletRequest servletRequest) {
        List<CategoryDTO> categories = categoryService.getAll();
        return ResponseEntity
                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null, categories));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiTemplate<Void, CategoryDTO>> getById(@PathVariable Long id,
            HttpServletRequest servletRequest) {
        CategoryDTO category = categoryService.getById(id);
        return ResponseEntity
                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null, category));
    }

    @PostMapping
    public ResponseEntity<ApiTemplate<Void, CategoryDTO>> create(@Valid @RequestBody CategoryDTOIU categoryDTOIU,
            HttpServletRequest servletRequest) {
        CategoryDTO createdCategory = categoryService.create(categoryDTOIU);
        return ResponseEntity
                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null, createdCategory));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiTemplate<Void, CategoryDTO>> update(@PathVariable Long id,
            @Valid @RequestBody CategoryDTOIU categoryDTOIU, HttpServletRequest servletRequest) {
        CategoryDTO updatedCategory = categoryService.update(id, categoryDTOIU);
        return ResponseEntity
                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null, updatedCategory));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiTemplate<Void, String>> delete(@PathVariable Long id, HttpServletRequest servletRequest) {
        categoryService.delete(id);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null,
                "Category deleted successfully"));
    }
}
