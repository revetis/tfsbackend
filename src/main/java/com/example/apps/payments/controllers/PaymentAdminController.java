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
    public ResponseEntity<ApiTemplate<Void, List<PaymentAdminDTO>>> getAllPayments(
            @RequestParam(name = "_start", defaultValue = "0") int start,
            @RequestParam(name = "_end", defaultValue = "10") int end,
            @RequestParam(name = "_sort", defaultValue = "createdAt") String sortField,
            @RequestParam(name = "_order", defaultValue = "DESC") String sortOrder,
            @RequestParam(name = "q", required = false) String search,
            @RequestParam(name = "status", required = false) String status,
            HttpServletRequest servletRequest) {

        var result = paymentService.getAllPayments(start, end, sortField, sortOrder, search, status);

        var response = ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(result.totalCount()))
                .header("Access-Control-Expose-Headers", "X-Total-Count")
                .body(ApiTemplate.<Void, List<PaymentAdminDTO>>apiTemplateGenerator(
                        true,
                        200,
                        servletRequest.getRequestURI(),
                        null,
                        result.data()));
        return response;
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
