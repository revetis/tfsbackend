package com.example.apps.products.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.apps.products.dtos.ColorDTO;
import com.example.apps.products.services.IColorService;
import com.example.tfs.maindto.ApiTemplate;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/api/public/colors")
@RequiredArgsConstructor
public class ColorPublicController {

    private final IColorService colorService;

    @GetMapping
    public ResponseEntity<ApiTemplate<Void, List<ColorDTO>>> getAll(HttpServletRequest servletRequest) {
        List<ColorDTO> colors = colorService.getAll();
        return ResponseEntity
                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null, colors));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiTemplate<Void, ColorDTO>> getById(@PathVariable Long id,
            HttpServletRequest servletRequest) {
        ColorDTO color = colorService.getById(id);
        return ResponseEntity
                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null, color));
    }
}
