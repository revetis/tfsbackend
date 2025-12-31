package com.example.apps.settings.controllers;

import com.example.apps.settings.dtos.*;
import com.example.apps.settings.services.ISettingsService;
import com.example.tfs.maindto.ApiTemplate;
import lombok.RequiredArgsConstructor;
import org.apache.hc.core5.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest/api/public/settings")
@RequiredArgsConstructor
public class SettingsPublicController {

    private final ISettingsService settingsService;

    @GetMapping
    public ResponseEntity<?> getSiteSettings() {
        SiteSettingsDTO settings = settingsService.getSiteSettings();
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_OK,
                "/settings",
                null,
                settings));
    }

    @GetMapping("/sliders")
    public ResponseEntity<?> getActiveSliders() {
        List<SliderDTO> sliders = settingsService.getActiveSliders();
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_OK,
                "/settings/sliders",
                null,
                sliders));
    }

    @GetMapping("/faqs")
    public ResponseEntity<?> getActiveFAQs(@RequestParam(required = false) String category) {
        List<FAQDTO> faqs = category != null
                ? settingsService.getFAQsByCategory(category)
                : settingsService.getActiveFAQs();
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_OK,
                "/settings/faqs",
                null,
                faqs));
    }

    @GetMapping("/pages/{slug}")
    public ResponseEntity<?> getPageBySlug(@PathVariable String slug) {
        PageDTO page = settingsService.getPageBySlug(slug);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_OK,
                "/settings/pages/" + slug,
                null,
                page));
    }

    @GetMapping("/pages/footer")
    public ResponseEntity<?> getFooterPages() {
        List<PageDTO> pages = settingsService.getFooterPages();
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_OK,
                "/settings/pages/footer",
                null,
                pages));
    }
}
