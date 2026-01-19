package com.example.apps.invoices.entities;

import java.time.LocalDateTime;

import com.example.tfs.entities.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Singleton entity for invoice settings - admin panelden özelleştirilebilir
 */
@Entity
@Table(name = "invoice_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceSettings extends BaseEntity {

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "company_logo_url")
    private String companyLogoUrl;

    @Column(name = "company_address", columnDefinition = "TEXT")
    private String companyAddress;

    @Column(name = "company_phone")
    private String companyPhone;

    @Column(name = "company_email")
    private String companyEmail;

    @Column(name = "tax_number")
    private String taxNumber;

    @Column(name = "bank_account_info", columnDefinition = "TEXT")
    private String bankAccountInfo;

    @Column(name = "footer_text")
    private String footerText;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
