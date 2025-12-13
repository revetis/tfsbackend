package com.example.apps.products.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.apps.products.dtos.BrandDTO;
import com.example.apps.products.services.IBrandService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/api/public/brands")
@RequiredArgsConstructor
public class BrandPublicController {

    private final IBrandService brandService;

    @GetMapping
    public ResponseEntity<List<BrandDTO>> getAll() {
        return ResponseEntity.ok(brandService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BrandDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(brandService.getById(id));
    }
}
