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
import com.example.tfs.maindto.ApiTemplate;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/api/admin/colors")
@RequiredArgsConstructor
@Validated
public class ColorAdminController {

        private final IColorService colorService;

        @GetMapping
        public ResponseEntity<ApiTemplate<Void, List<ColorDTO>>> getAll(HttpServletRequest servletRequest) {
                List<ColorDTO> colors = colorService.getAll();
                return ResponseEntity
                                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null,
                                                colors));
        }

        @GetMapping("/{id}")
        public ResponseEntity<ApiTemplate<Void, ColorDTO>> getById(@PathVariable Long id,
                        HttpServletRequest servletRequest) {
                ColorDTO color = colorService.getById(id);
                return ResponseEntity
                                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null,
                                                color));
        }

        @PostMapping
        public ResponseEntity<ApiTemplate<Void, ColorDTO>> create(@Valid @RequestBody ColorDTOIU colorDTOIU,
                        HttpServletRequest servletRequest) {
                ColorDTO createdColor = colorService.create(colorDTOIU);
                return ResponseEntity
                                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null,
                                                createdColor));
        }

        @PutMapping("/{id}")
        public ResponseEntity<ApiTemplate<Void, ColorDTO>> update(@PathVariable Long id,
                        @Valid @RequestBody ColorDTOIU colorDTOIU, HttpServletRequest servletRequest) {
                ColorDTO updatedColor = colorService.update(id, colorDTOIU);
                return ResponseEntity
                                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null,
                                                updatedColor));
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<ApiTemplate<Void, String>> delete(@PathVariable Long id,
                        HttpServletRequest servletRequest) {
                colorService.delete(id);
                return ResponseEntity
                                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null,
                                                "Color deleted successfully"));
        }
}
