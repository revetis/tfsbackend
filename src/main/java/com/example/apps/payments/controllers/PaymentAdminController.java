package com.example.apps.payments.controllers;

import org.apache.hc.core5.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.apps.payments.dtos.OrderReturnRequestDTO;
import com.example.apps.payments.dtos.PaymentAdminDTO;
import com.example.apps.payments.services.IPaymentService;
import com.example.tfs.maindto.ApiTemplate;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/rest/api/admin/payments")
@Slf4j
public class PaymentAdminController {

    @Autowired
    private IPaymentService paymentService;

    @PostMapping("/return/{orderNumber}")
    public ResponseEntity<?> returnFullPayment(@PathVariable String orderNumber) {
        log.info("Full refund/cancel requested by admin for order: {}", orderNumber);

        paymentService.returnPayment(orderNumber);

        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_OK,
                "Ödeme iade/iptal işlemi başarıyla tamamlandı ve n8n bildirimi gönderildi.",
                null,
                null));
    }

    @PostMapping("/refund-partial")
    public ResponseEntity<?> refundPartial(@RequestBody OrderReturnRequestDTO returnRequest) {
        log.info("Partial refund requested for Order ID: {}", returnRequest.getOrderId());

        paymentService.refundPartialPayment(returnRequest);

        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_OK,
                "Kısmi iade işlemi başarıyla gerçekleştirildi.",
                null,
                null));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiTemplate<Void, List<PaymentAdminDTO>>> getAllPayments(HttpServletRequest servletRequest) {
        List<PaymentAdminDTO> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                200,
                servletRequest.getRequestURI(),
                null,
                payments));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiTemplate<Void, PaymentAdminDTO>> getPaymentById(@PathVariable Long id,
            HttpServletRequest servletRequest) {
        PaymentAdminDTO payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                200,
                servletRequest.getRequestURI(),
                null,
                payment));
    }
}
