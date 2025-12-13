package com.example.apps.products.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.apps.products.dtos.ColorDTO;
import com.example.apps.products.services.IColorService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/api/public/colors")
@RequiredArgsConstructor
public class ColorPublicController {

    private final IColorService colorService;

    @GetMapping
    public ResponseEntity<List<ColorDTO>> getAll() {
        return ResponseEntity.ok(colorService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ColorDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(colorService.getById(id));
    }
}
