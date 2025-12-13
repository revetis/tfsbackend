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

import com.example.apps.products.dtos.ColorDTO;
import com.example.apps.products.dtos.ColorDTOIU;
import com.example.apps.products.services.IColorService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/api/admin/colors")
@RequiredArgsConstructor
@Validated
public class ColorAdminController {

    private final IColorService colorService;

    @GetMapping
    public ResponseEntity<List<ColorDTO>> getAll() {
        return ResponseEntity.ok(colorService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ColorDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(colorService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ColorDTO> create(@Valid @RequestBody ColorDTOIU colorDTOIU) {
        return ResponseEntity.ok(colorService.create(colorDTOIU));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ColorDTO> update(@PathVariable Long id, @Valid @RequestBody ColorDTOIU colorDTOIU) {
        return ResponseEntity.ok(colorService.update(id, colorDTOIU));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        colorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
