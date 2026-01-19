package com.example.apps.invoices.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
public class InvoiceDTO {

    // Order Info
    private Long orderId;
    private String orderNumber;
    private String invoiceNumber;
    private LocalDateTime invoiceGeneratedAt;
    private String invoicePdfUrl;

    // Company Info (Snapshot)
    private String companyName;
    private String companyAddress;
    private String companyTaxNumber;
    private String bankInfo;
    private String footerText;

    // Customer Info
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String customerAddress;

    // Items
    private List<InvoiceItemDTO> items;

    // Totals
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal shippingCost;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InvoiceItemDTO {
        private String productName;
        private Integer quantity;
        private Double taxRatio;
        private BigDecimal unitPriceWithoutTax;
        private BigDecimal unitPriceWithTax;
        private BigDecimal taxAmount;
        private BigDecimal totalPrice;
    }
}
