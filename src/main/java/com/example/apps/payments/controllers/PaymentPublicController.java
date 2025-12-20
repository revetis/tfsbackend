package com.example.apps.payments.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.apps.payments.dtos.PaymentRequestDTO;
import com.example.apps.payments.services.IPaymentService;
import com.example.tfs.maindto.ApiTemplate;

import io.netty.handler.codec.http.HttpResponseStatus;
import jakarta.validation.Valid;

import com.example.apps.payments.gateways.utils.GatewayUtils;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/rest/api/public/payments")
@Validated
@Slf4j
public class PaymentPublicController {

    @Autowired
    private IPaymentService paymentService;

    @Autowired
    private GatewayUtils gatewayUtils;

    @PostMapping("/initialize")
    public ResponseEntity<?> initializePayment(@Valid @RequestBody PaymentRequestDTO requestDTO) {
        log.info("Payment initialization requested for order: {}", requestDTO.getOrderNumber());

        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpResponseStatus.ACCEPTED.code(),
                "Payment initialized successfully",
                null,
                paymentService.createPayment(requestDTO)));
    }

    @PostMapping("/callback/iyzico")
    public ResponseEntity<?> iyzicoCallback(
            @RequestParam("token") String token,
            @RequestParam("conversationId") String conversationId,
            @RequestParam("signature") String signature) {

        log.info("Callback received for token: {}", token);

        if (!gatewayUtils.isSignatureValid(signature, conversationId, token)) {
            log.warn("Invalid signature detected for token: {}", token);
            return ResponseEntity.status(HttpResponseStatus.FORBIDDEN.code()).body(ApiTemplate.apiTemplateGenerator(
                    false,
                    HttpResponseStatus.FORBIDDEN.code(),
                    "Invalid security signature",
                    null,
                    null));
        }

        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpResponseStatus.ACCEPTED.code(),
                "Payment completed successfully",
                null,
                paymentService.completePayment(token)));
    }
}