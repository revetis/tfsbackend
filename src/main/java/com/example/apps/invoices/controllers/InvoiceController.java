package com.example.apps.invoices.controllers;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.apps.invoices.dtos.InvoiceDTO;
import com.example.apps.invoices.services.IInvoiceService;
import com.example.apps.orders.entities.Order;
import com.example.apps.orders.repositories.OrderRepository;
import com.example.tfs.maindto.ApiTemplate;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/api")
@RequiredArgsConstructor
public class InvoiceController {

        private final IInvoiceService invoiceService;
        private final OrderRepository orderRepository;

        /**
         * Fatura PDF indir (Public - sipariş numarası ile)
         */
        @GetMapping("/public/invoices/{orderNumber}/pdf")
        public ResponseEntity<byte[]> getInvoicePdf(@PathVariable String orderNumber) {
                // This now returns the RECEIPT (Bilgi Fişi)
                // Ideally we should rename endpoint too, but keeping API contract for now.
                Order order = orderRepository.findByOrderNumber(orderNumber)
                                .orElseThrow(() -> new RuntimeException("Order not found"));

                byte[] pdfBytes = invoiceService.generateReceiptPdf(order.getId());

                return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_DISPOSITION,
                                                "attachment; filename=receipt_" + orderNumber + ".pdf")
                                .contentType(MediaType.APPLICATION_PDF)
                                .body(pdfBytes);
        }

        /**
         * Fatura PDF indir (Private - order ID ile)
         */
        @GetMapping("/private/invoices/{orderId}/pdf")
        public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable Long orderId) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

                byte[] pdfBytes = invoiceService.generateReceiptPdf(orderId);

                String filename = "fatura_"
                                + (order.getInvoiceNumber() != null ? order.getInvoiceNumber() : order.getOrderNumber())
                                + ".pdf";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_PDF);
                headers.setContentDispositionFormData("attachment", filename);
                headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

                return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        }

        /**
         * Fatura bilgilerini getir (JSON)
         */
        @GetMapping("/private/invoices/{orderId}")
        public ResponseEntity<?> getInvoiceDetails(@PathVariable Long orderId) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

                InvoiceDTO dto = mapToInvoiceDTO(order);

                return ResponseEntity.ok(
                                ApiTemplate.apiTemplateGenerator(
                                                true,
                                                HttpStatus.OK.value(),
                                                "/rest/api/private/invoices/" + orderId,
                                                null,
                                                dto));
        }

        /**
         * Admin: Faturayı yeniden oluştur
         */
        @PostMapping("/admin/invoices/{orderId}/regenerate")
        public ResponseEntity<?> regenerateInvoice(@PathVariable Long orderId) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

                invoiceService.createInvoiceForOrder(order.getId());

                return ResponseEntity.ok(
                                ApiTemplate.apiTemplateGenerator(
                                                true,
                                                HttpStatus.OK.value(),
                                                "/rest/api/admin/invoices/" + orderId + "/regenerate",
                                                null,
                                                "Invoice regenerated: " + order.getInvoiceNumber()));
        }

        /**
         * Admin: Fatura e-postası gönder
         */
        @PostMapping("/admin/invoices/{orderId}/send-email")
        public ResponseEntity<?> sendInvoiceEmail(@PathVariable Long orderId) {
                invoiceService.sendInvoiceEmail(orderId);

                return ResponseEntity.ok(
                                ApiTemplate.apiTemplateGenerator(
                                                true,
                                                HttpStatus.OK.value(),
                                                "/rest/api/admin/invoices/" + orderId + "/send-email",
                                                null,
                                                "Invoice email sent"));
        }

        private InvoiceDTO mapToInvoiceDTO(Order order) {
                return InvoiceDTO.builder()
                                .orderId(order.getId())
                                .orderNumber(order.getOrderNumber())
                                .invoiceNumber(order.getInvoiceNumber())
                                .invoiceGeneratedAt(order.getInvoiceGeneratedAt())
                                .invoicePdfUrl(order.getInvoicePdfUrl())
                                .companyName(order.getInvoiceCompanyName())
                                .companyAddress(order.getInvoiceCompanyAddress())
                                .companyTaxNumber(order.getInvoiceCompanyTaxNumber())
                                .bankInfo(order.getInvoiceBankInfo())
                                .footerText(order.getInvoiceFooterText())
                                .customerName(order.getCustomerName())
                                .customerEmail(order.getCustomerEmail())
                                .customerPhone(order.getCustomerPhone())
                                .customerAddress(order.getBillingAddress() != null
                                                ? order.getBillingAddress().getAddressLine() + ", "
                                                                + order.getBillingAddress().getCity()
                                                : null)
                                .items(order.getOrderItems().stream().map(item -> InvoiceDTO.InvoiceItemDTO.builder()
                                                .productName(item.getProductVariantName())
                                                .quantity(item.getQuantity())
                                                .taxRatio(item.getTaxRatio())
                                                .unitPriceWithoutTax(item.getUnitPriceWithoutTax())
                                                .unitPriceWithTax(item.getUnitPriceWithTax())
                                                .taxAmount(item.getTaxAmount())
                                                .totalPrice(item.getPaidPrice())
                                                .build()).toList())
                                .subtotal(order.getSubtotal())
                                .taxAmount(order.getTaxAmount())
                                .shippingCost(order.getShippingCost())
                                .discountAmount(order.getDiscountAmount())
                                .totalAmount(order.getTotalAmount())
                                .build();
        }
}
