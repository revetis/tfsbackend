package com.example.apps.products.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.apps.products.dtos.BrandDTO;
import com.example.apps.products.services.IBrandService;
import com.example.tfs.maindto.ApiTemplate;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/api/public/brands")
@RequiredArgsConstructor
public class BrandPublicController {

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
}
