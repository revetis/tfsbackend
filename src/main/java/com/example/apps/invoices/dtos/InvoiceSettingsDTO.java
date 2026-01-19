package com.example.apps.invoices.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceSettingsDTO {
    private Long id;
    private String companyName;
    private String companyLogoUrl;
    private String companyAddress;
    private String companyPhone;
    private String companyEmail;
    private String taxNumber;
    private String bankAccountInfo;
    private String footerText;
}
