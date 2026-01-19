package com.example.apps.invoices.services;

import com.example.apps.orders.entities.Order;

public interface IInvoiceService {

    /**
     * Fatura numarası oluşturur ve siparişe kaydeder
     */
    String generateInvoiceNumber(Order order);

    /**
     * Fatura PDF'ini (veya Bilgi Fişi) oluşturur ve byte array döner
     */
    byte[] generateReceiptPdf(Long orderId);

    /**
     * Sipariş için bilgi fişi/fatura oluşturur
     */
    void createInvoiceForOrder(Long orderId);

    /**
     * N8N üzerinden fatura e-postası gönderir (Artık EventListener kullanılıyor, bu
     * metot legacy olabilir veya silinebilir)
     */
    void sendInvoiceEmail(Long orderId);

    /**
     * Admin paneli üzerinden fatura dosyası yükler
     * 
     * @param orderId Sipariş ID
     * @param file    Fatura Dosyası
     */
    void uploadInvoiceFile(Long orderId, org.springframework.web.multipart.MultipartFile file);

    /**
     * Siparişin faturasını (dosyasını) getirir
     */
    org.springframework.core.io.Resource getInvoiceFile(Long orderId);
}
