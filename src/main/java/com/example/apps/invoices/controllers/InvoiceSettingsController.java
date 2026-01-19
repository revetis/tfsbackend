package com.example.apps.invoices.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.apps.invoices.dtos.InvoiceSettingsDTO;
import com.example.apps.invoices.entities.InvoiceSettings;
import com.example.apps.invoices.repositories.InvoiceSettingsRepository;
import com.example.tfs.maindto.ApiTemplate;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/api/admin/invoice-settings")
@RequiredArgsConstructor
public class InvoiceSettingsController {

    private final InvoiceSettingsRepository invoiceSettingsRepository;

    /**
     * Mevcut fatura ayarlarını getir
     */
    @GetMapping
    public ResponseEntity<?> getSettings() {
        InvoiceSettings settings = invoiceSettingsRepository.findByIsActiveTrue()
                .orElse(createDefaultSettings());

        return ResponseEntity.ok(
                ApiTemplate.apiTemplateGenerator(
                        true,
                        HttpStatus.OK.value(),
                        "/rest/api/admin/invoice-settings",
                        null,
                        mapToDTO(settings)));
    }

    /**
     * Fatura ayarlarını güncelle
     */
    @PutMapping
    public ResponseEntity<?> updateSettings(@RequestBody InvoiceSettingsDTO dto) {
        InvoiceSettings settings = invoiceSettingsRepository.findByIsActiveTrue()
                .orElse(createDefaultSettings());

        settings.setCompanyName(dto.getCompanyName());
        settings.setCompanyAddress(dto.getCompanyAddress());
        settings.setCompanyPhone(dto.getCompanyPhone());
        settings.setCompanyEmail(dto.getCompanyEmail());
        settings.setTaxNumber(dto.getTaxNumber());
        settings.setBankAccountInfo(dto.getBankAccountInfo());
        settings.setFooterText(dto.getFooterText());

        if (dto.getCompanyLogoUrl() != null) {
            settings.setCompanyLogoUrl(dto.getCompanyLogoUrl());
        }

        settings = invoiceSettingsRepository.save(settings);

        return ResponseEntity.ok(
                ApiTemplate.apiTemplateGenerator(
                        true,
                        HttpStatus.OK.value(),
                        "/rest/api/admin/invoice-settings",
                        null,
                        mapToDTO(settings)));
    }

    /**
     * Logo yükle
     */
    @PostMapping("/logo")
    public ResponseEntity<?> uploadLogo(@RequestPart("file") MultipartFile file) {
        // TODO: Logo dosyasını kaydet ve URL döndür
        // Bu basit implementasyonda sadece URL'i güncelliyoruz
        String logoUrl = "/uploads/invoice-logo." + getFileExtension(file.getOriginalFilename());

        InvoiceSettings settings = invoiceSettingsRepository.findByIsActiveTrue()
                .orElse(createDefaultSettings());
        settings.setCompanyLogoUrl(logoUrl);
        invoiceSettingsRepository.save(settings);

        return ResponseEntity.ok(
                ApiTemplate.apiTemplateGenerator(
                        true,
                        HttpStatus.OK.value(),
                        "/rest/api/admin/invoice-settings/logo",
                        null,
                        logoUrl));
    }

    private InvoiceSettings createDefaultSettings() {
        InvoiceSettings settings = InvoiceSettings.builder()
                .companyName("THEFIRSTSTEP")
                .companyAddress("")
                .companyPhone("")
                .companyEmail("")
                .taxNumber("")
                .bankAccountInfo("")
                .footerText("BİZİ TERCİH ETTİĞİNİZ İÇİN TEŞEKKÜR EDERİZ.")
                .isActive(true)
                .build();
        return invoiceSettingsRepository.save(settings);
    }

    private InvoiceSettingsDTO mapToDTO(InvoiceSettings settings) {
        return InvoiceSettingsDTO.builder()
                .id(settings.getId())
                .companyName(settings.getCompanyName())
                .companyLogoUrl(settings.getCompanyLogoUrl())
                .companyAddress(settings.getCompanyAddress())
                .companyPhone(settings.getCompanyPhone())
                .companyEmail(settings.getCompanyEmail())
                .taxNumber(settings.getTaxNumber())
                .bankAccountInfo(settings.getBankAccountInfo())
                .footerText(settings.getFooterText())
                .build();
    }

    private String getFileExtension(String filename) {
        if (filename == null)
            return "png";
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1) : "png";
    }
}
