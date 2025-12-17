package com.example.apps.products.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.apps.products.dtos.CategoryDTO;
import com.example.apps.products.services.ICategoryService;
import com.example.tfs.maindto.ApiTemplate;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/api/public/categories")
@RequiredArgsConstructor
public class CategoryPublicController {

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
}
