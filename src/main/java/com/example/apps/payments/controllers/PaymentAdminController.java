package com.example.apps.payments.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.apps.payments.dtos.OrderReturnRequestDTO;
import com.example.apps.payments.services.IPaymentService;
import com.example.tfs.maindto.ApiTemplate;

import io.netty.handler.codec.http.HttpResponseStatus;
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
                HttpResponseStatus.OK.code(),
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
                HttpResponseStatus.OK.code(),
                "Kısmi iade işlemi başarıyla gerçekleştirildi.",
                null,
                null));
    }
}