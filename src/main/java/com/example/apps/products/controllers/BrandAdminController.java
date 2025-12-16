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

import com.example.apps.products.dtos.BrandDTO;
import com.example.apps.products.dtos.BrandDTOIU;
import com.example.apps.products.services.IBrandService;
import com.example.settings.maindto.ApiTemplate;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/api/admin/brands")
@RequiredArgsConstructor
@Validated
public class BrandAdminController {

    private final IBrandService brandService;

    @GetMapping
    public ResponseEntity<ApiTemplate<Void, List<BrandDTO>>> getAll(HttpServletRequest servletRequest) {
        List<BrandDTO> brands = brandService.getAll();
        return ResponseEntity
                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null, brands));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiTemplate<Void, BrandDTO>> getById(@PathVariable Long id,
            HttpServletRequest servletRequest) {
        BrandDTO brand = brandService.getById(id);
        return ResponseEntity
                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null, brand));
    }

    @PostMapping
    public ResponseEntity<ApiTemplate<Void, BrandDTO>> create(@Valid @RequestBody BrandDTOIU brandDTOIU,
            HttpServletRequest servletRequest) {
        BrandDTO createdBrand = brandService.create(brandDTOIU);
        return ResponseEntity
                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null, createdBrand));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiTemplate<Void, BrandDTO>> update(@PathVariable Long id,
            @Valid @RequestBody BrandDTOIU brandDTOIU, HttpServletRequest servletRequest) {
        BrandDTO updatedBrand = brandService.update(id, brandDTOIU);
        return ResponseEntity
                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null, updatedBrand));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiTemplate<Void, String>> delete(@PathVariable Long id, HttpServletRequest servletRequest) {
        brandService.delete(id);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null,
                "Brand deleted successfully"));
    }
}
