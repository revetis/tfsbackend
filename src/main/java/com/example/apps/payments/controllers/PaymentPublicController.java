package com.example.apps.payments.controllers;

import org.apache.hc.core5.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.apps.payments.dtos.PaymentRequestDTO;
import com.example.apps.payments.services.IPaymentService;
import com.example.tfs.maindto.ApiTemplate;

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

    @Autowired
    private com.example.apps.orders.services.IOrderService orderService;

    @PostMapping("/purchase-shipment/{orderNumber}")
    public ResponseEntity<?> purchaseShipment(@PathVariable String orderNumber) {
        log.info("Manual shipment purchase requested for order: {}", orderNumber);
        paymentService.purchaseShipmentForOrder(orderNumber);

        // Fetch updated order details to return
        var updatedOrder = orderService.getByOrderNumber(orderNumber);

        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_OK,
                "Shipment purchase initiated",
                null,
                updatedOrder));
    }

    @PostMapping("/initialize")
    public ResponseEntity<?> initializePayment(@Valid @RequestBody PaymentRequestDTO requestDTO) {
        log.info("Payment initialization requested for order: {}", requestDTO.getOrderNumber());

        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_OK,
                "Payment initialized successfully",
                null,
                paymentService.createPayment(requestDTO)));
    }

    @Autowired
    private com.example.tfs.ApplicationProperties applicationProperties;

    @CrossOrigin(origins = "*", allowCredentials = "false")
    @PostMapping("/callback/iyzico")
    public void iyzicoCallback(
            @RequestParam("token") String token,
            jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {

        log.info("Callback received for token: {}", token);

        String frontendUrl = applicationProperties.getFRONTEND_URL();
        // Remove trailing slash if present to avoid double slash
        if (frontendUrl.endsWith("/")) {
            frontendUrl = frontendUrl.substring(0, frontendUrl.length() - 1);
        }

        try {
            // completePayment retrieves payment details from Iyzico using token
            // and returns PaymentResponseDTO containing order details
            var paymentResult = paymentService.completePayment(token);
            String orderNumber = paymentResult.getOrderNumber();

            if ("SUCCESS".equals(paymentResult.getPaymentStatus())) {
                response.sendRedirect(frontendUrl + "/payment/success?orderNumber=" + orderNumber);
            } else {
                String errorMessage = "Payment failed with status: " + paymentResult.getPaymentStatus();
                response.sendRedirect(frontendUrl + "/payment/error?message="
                        + java.net.URLEncoder.encode(errorMessage, java.nio.charset.StandardCharsets.UTF_8)
                        + "&orderNumber=" + orderNumber);
            }
        } catch (Exception e) {
            log.error("Payment completion failed: {}", e.getMessage());
            response.sendRedirect(frontendUrl + "/payment/error?message="
                    + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8));
        }
    }
}