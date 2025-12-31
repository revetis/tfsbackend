package com.example.apps.settings.controllers;

import com.example.apps.settings.dtos.*;
import com.example.apps.settings.services.ISettingsService;
import com.example.tfs.maindto.ApiTemplate;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.hc.core5.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest/api/admin/settings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SettingsAdminController {

    private final ISettingsService settingsService;

    // ==================== Site Settings ====================

    @GetMapping
    public ResponseEntity<?> getSiteSettings() {
        SiteSettingsDTO settings = settingsService.getSiteSettings();
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_OK,
                "/admin/settings",
                null,
                settings));
    }

    @PutMapping
    public ResponseEntity<?> updateSiteSettings(@RequestBody @Valid SiteSettingsDTO dto) {
        SiteSettingsDTO updated = settingsService.updateSiteSettings(dto);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_OK,
                "/admin/settings",
                null,
                updated));
    }

    // ==================== Sliders ====================

    @GetMapping("/sliders")
    public ResponseEntity<?> getAllSliders() {
        List<SliderDTO> sliders = settingsService.getAllSliders();
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_OK,
                "/admin/settings/sliders",
                null,
                sliders));
    }

    @GetMapping("/sliders/{id}")
    public ResponseEntity<?> getSliderById(@PathVariable Long id) {
        SliderDTO slider = settingsService.getSliderById(id);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_OK,
                "/admin/settings/sliders/" + id,
                null,
                slider));
    }

    @PostMapping("/sliders")
    public ResponseEntity<?> createSlider(@RequestBody @Valid SliderDTO dto) {
        SliderDTO created = settingsService.createSlider(dto);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_CREATED,
                "/admin/settings/sliders",
                null,
                created));
    }

    @PutMapping("/sliders/{id}")
    public ResponseEntity<?> updateSlider(@PathVariable Long id, @RequestBody @Valid SliderDTO dto) {
        SliderDTO updated = settingsService.updateSlider(id, dto);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_OK,
                "/admin/settings/sliders/" + id,
                null,
                updated));
    }

    @DeleteMapping("/sliders/{id}")
    public ResponseEntity<?> deleteSlider(@PathVariable Long id) {
        settingsService.deleteSlider(id);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_OK,
                "/admin/settings/sliders/" + id,
                null,
                true));
    }

    @PutMapping("/sliders/{id}/order")
    public ResponseEntity<?> updateSliderOrder(@PathVariable Long id, @RequestParam Integer order) {
        settingsService.updateSliderOrder(id, order);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_OK,
                "/admin/settings/sliders/" + id + "/order",
                null,
                true));
    }

    // ==================== FAQs ====================

    @GetMapping("/faqs")
    public ResponseEntity<?> getAllFAQs() {
        List<FAQDTO> faqs = settingsService.getAllFAQs();
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_OK,
                "/admin/settings/faqs",
                null,
                faqs));
    }

    @GetMapping("/faqs/{id}")
    public ResponseEntity<?> getFAQById(@PathVariable Long id) {
        FAQDTO faq = settingsService.getFAQById(id);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_OK,
                "/admin/settings/faqs/" + id,
                null,
                faq));
    }

    @PostMapping("/faqs")
    public ResponseEntity<?> createFAQ(@RequestBody @Valid FAQDTO dto) {
        FAQDTO created = settingsService.createFAQ(dto);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_CREATED,
                "/admin/settings/faqs",
                null,
                created));
    }

    @PutMapping("/faqs/{id}")
    public ResponseEntity<?> updateFAQ(@PathVariable Long id, @RequestBody @Valid FAQDTO dto) {
        FAQDTO updated = settingsService.updateFAQ(id, dto);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_OK,
                "/admin/settings/faqs/" + id,
                null,
                updated));
    }

    @DeleteMapping("/faqs/{id}")
    public ResponseEntity<?> deleteFAQ(@PathVariable Long id) {
        settingsService.deleteFAQ(id);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_OK,
                "/admin/settings/faqs/" + id,
                null,
                true));
    }

    // ==================== Pages ====================

    @GetMapping("/pages")
    public ResponseEntity<?> getAllPages() {
        List<PageDTO> pages = settingsService.getAllPages();
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_OK,
                "/admin/settings/pages",
                null,
                pages));
    }

    @GetMapping("/pages/{id}")
    public ResponseEntity<?> getPageById(@PathVariable Long id) {
        PageDTO page = settingsService.getPageById(id);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_OK,
                "/admin/settings/pages/" + id,
                null,
                page));
    }

    @PostMapping("/pages")
    public ResponseEntity<?> createPage(@RequestBody @Valid PageDTO dto) {
        PageDTO created = settingsService.createPage(dto);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_CREATED,
                "/admin/settings/pages",
                null,
                created));
    }

    @PutMapping("/pages/{id}")
    public ResponseEntity<?> updatePage(@PathVariable Long id, @RequestBody @Valid PageDTO dto) {
        PageDTO updated = settingsService.updatePage(id, dto);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_OK,
                "/admin/settings/pages/" + id,
                null,
                updated));
    }

    @DeleteMapping("/pages/{id}")
    public ResponseEntity<?> deletePage(@PathVariable Long id) {
        settingsService.deletePage(id);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_OK,
                "/admin/settings/pages/" + id,
                null,
                true));
    }
}
