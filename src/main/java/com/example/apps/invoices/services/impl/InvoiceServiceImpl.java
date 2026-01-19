package com.example.apps.invoices.services.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.apps.invoices.entities.InvoiceSettings;
import com.example.apps.invoices.repositories.InvoiceSettingsRepository;
import com.example.apps.invoices.services.IInvoiceService;
import com.example.apps.orders.entities.Order;
import com.example.apps.orders.entities.OrderItem;
import com.example.apps.orders.enums.OrderStatus;
import com.example.apps.orders.events.InvoiceUploadedEvent;
import com.example.apps.orders.repositories.OrderRepository;
import com.example.apps.orders.services.strategy.ReceiptGeneratorStrategy;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceServiceImpl implements IInvoiceService {

    private final OrderRepository orderRepository;
    private final InvoiceSettingsRepository invoiceSettingsRepository;
    private final ReceiptGeneratorStrategy receiptGeneratorStrategy;
    private final ApplicationEventPublisher eventPublisher;
    private final com.example.tfs.StorageService storageService;
    private final com.example.tfs.ApplicationProperties applicationProperties;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public String generateInvoiceNumber(Order order) {
        int currentYear = LocalDateTime.now().getYear();
        String sequenceName = "invoice_seq_" + currentYear;

        // Safer Sequence Check using READ ONLY check first
        boolean sequenceExists = false;
        try {
            Long count = ((Number) entityManager.createNativeQuery(
                    "SELECT count(*) FROM information_schema.sequences WHERE sequence_name = '" + sequenceName + "'")
                    .getSingleResult()).longValue();
            sequenceExists = count > 0;
        } catch (Exception e) {
            log.warn("Failed to check sequence existence: {}", e.getMessage());
        }

        if (!sequenceExists) {
            try {
                entityManager.createNativeQuery(
                        "CREATE SEQUENCE IF NOT EXISTS " + sequenceName + " START 1").executeUpdate();
            } catch (Exception e) {
                log.debug("Sequence creation failed (might exist): {}", e.getMessage());
            }
        }

        Long nextVal = ((Number) entityManager.createNativeQuery(
                "SELECT nextval('" + sequenceName + "')").getSingleResult()).longValue();

        String invoiceNumber = String.format("INV-%d-%05d", currentYear, nextVal);
        order.setInvoiceNumber(invoiceNumber);
        order.setInvoiceGeneratedAt(LocalDateTime.now());
        log.info("Generated internal invoice ref: {} for order: {}", invoiceNumber, order.getOrderNumber());
        return invoiceNumber;
    }

    @Override
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void createInvoiceForOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // Prepare Order data for Receipt/Invoice
        if (order.getInvoiceNumber() == null) {
            generateInvoiceNumber(order); // Optional for Receipt
        }

        InvoiceSettings settings = invoiceSettingsRepository.findByIsActiveTrue()
                .orElse(getDefaultSettings());

        order.setInvoiceCompanyName(settings.getCompanyName());
        order.setInvoiceCompanyAddress(settings.getCompanyAddress());
        order.setInvoiceCompanyTaxNumber(settings.getTaxNumber());
        order.setInvoiceBankInfo(settings.getBankAccountInfo());
        order.setInvoiceFooterText(settings.getFooterText());

        calculateTaxForOrder(order);
        orderRepository.save(order);
    }

    private void calculateTaxForOrder(Order order) {
        BigDecimal totalSubtotal = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;

        for (OrderItem item : order.getOrderItems()) {
            BigDecimal unitPriceWithTax = item.getPaidPrice();
            Double taxRatio = item.getTaxRatio() != null ? item.getTaxRatio() : 10.0;
            BigDecimal divisor = BigDecimal.ONE.add(
                    BigDecimal.valueOf(taxRatio).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
            BigDecimal unitPriceWithoutTax = unitPriceWithTax.divide(divisor, 2, RoundingMode.HALF_UP);
            BigDecimal unitTaxAmount = unitPriceWithTax.subtract(unitPriceWithoutTax);
            BigDecimal itemTaxAmount = unitTaxAmount.multiply(BigDecimal.valueOf(item.getQuantity()));

            item.setUnitPriceWithTax(unitPriceWithTax);
            item.setUnitPriceWithoutTax(unitPriceWithoutTax);
            item.setTaxAmount(itemTaxAmount);
            item.setTaxRatio(taxRatio);

            totalSubtotal = totalSubtotal.add(
                    unitPriceWithoutTax.multiply(BigDecimal.valueOf(item.getQuantity())));
            totalTax = totalTax.add(itemTaxAmount);
        }
        order.setSubtotal(totalSubtotal);
        order.setTaxAmount(totalTax);
    }

    @Override
    public byte[] generateReceiptPdf(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        return receiptGeneratorStrategy.generateReceipt(order);
    }

    @Override
    public void sendInvoiceEmail(Long orderId) {
        // Deprecated / handled via Event Listener now
        log.warn("sendInvoiceEmail called directly, consider using Event mechanism.");
    }

    @Override
    @Transactional
    public void uploadInvoiceFile(Long orderId, org.springframework.web.multipart.MultipartFile file) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Sipariş bulunamadı: " + orderId));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("İptal edilmiş siparişe fatura yüklenemez.");
        }

        try {
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".pdf";
            String filename = "invoice_" + orderId + "_" + java.util.UUID.randomUUID().toString() + extension;

            // Use StorageService to save in the configured static path
            String folder = "invoices";
            String storedFilename = storageService.store(file, folder);

            // Build the relative path for storage
            String relativePath = "uploads/" + folder + "/" + storedFilename;

            // Download URL via API - Construct absolute URL
            String backendUrl = applicationProperties.getBACKEND_URL();
            if (backendUrl == null || backendUrl.isEmpty()) {
                backendUrl = "http://localhost:8080";
            }
            if (backendUrl.endsWith("/")) {
                backendUrl = backendUrl.substring(0, backendUrl.length() - 1);
            }

            String downloadUrl = backendUrl + "/rest/api/admin/orders/" + orderId + "/invoice/download";

            order.setInvoiceUrl(downloadUrl);
            order.setInvoicePdfUrl(relativePath);
            order.setInvoiceUploadedAt(LocalDateTime.now());

            orderRepository.save(order);

            log.info("Invoice uploaded via StorageService for Order {}: {}", order.getOrderNumber(), relativePath);
            eventPublisher.publishEvent(new InvoiceUploadedEvent(this, order, downloadUrl));

        } catch (Exception e) {
            throw new RuntimeException("Fatura dosyası kaydedilemedi: " + e.getMessage());
        }
    }

    @Override
    public org.springframework.core.io.Resource getInvoiceFile(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Sipariş bulunamadı: " + orderId));

        String filePathStr = order.getInvoicePdfUrl();
        if (filePathStr == null || filePathStr.isEmpty()) {
            throw new RuntimeException("Bu sipariş için yüklenmiş bir fatura bulunamadı.");
        }

        try {
            // Use ApplicationProperties for the correct base path
            java.nio.file.Path filePath = java.nio.file.Paths.get(
                    applicationProperties.getSTATIC_PATH(), filePathStr);
            org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(
                    filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Fatura dosyası okunamıyor veya mevcut değil.");
            }
        } catch (java.net.MalformedURLException e) {
            throw new RuntimeException("Dosya yolu hatası: " + e.getMessage());
        }
    }

    private InvoiceSettings getDefaultSettings() {
        return InvoiceSettings.builder()
                .companyName("Satıcı Samet Ture")
                .companyAddress("Mimar Sinan Mah. 178. Sokak No:5/2 Atakum / SAMSUN")
                .taxNumber("8750714159")
                .bankAccountInfo("TR14 0086 4011 0000 9618 7004 82")
                .footerText("BİZİ TERCİH ETTİĞİNİZ İÇİN TEŞEKKÜR EDERİZ.")
                .build();
    }
}
