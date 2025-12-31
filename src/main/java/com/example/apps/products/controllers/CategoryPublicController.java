package com.example.apps.products.controllers;

import org.apache.hc.core5.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.apps.products.services.ICategoryService;
import com.example.tfs.maindto.ApiTemplate;

@RestController
@RequestMapping("/rest/api/public/categories")
public class CategoryPublicController {

    @Autowired
    private ICategoryService categoryService;

    @GetMapping("/main")
    public ResponseEntity<?> getAllMainCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) { // Default larger size for menu
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_OK,
                "/rest/api/public/categories/main",
                null,
                categoryService.getAllMainCategories(page, size)));
    }

    @GetMapping("/sub/{mainCategoryId}")
    public ResponseEntity<?> getSubCategoriesByMain(
            @PathVariable Long mainCategoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_OK,
                "/rest/api/public/categories/sub",
                null,
                categoryService.getSubCategoriesByMainCategoryId(mainCategoryId, page, size)));
    }
}
