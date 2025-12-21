package com.example.apps.payments.gateways;

import java.math.BigDecimal;

import com.example.apps.payments.dtos.PaymentRequestDTO;
import com.example.apps.payments.dtos.PaymentResponseDTO;
import com.example.apps.payments.gateways.utils.GatewayResult;

public interface IGateway {

    PaymentResponseDTO initializePayment(PaymentRequestDTO request);

    GatewayResult retrievePaymentDetails(String token, String conversationId);

    GatewayResult cancelPayment(String paymentId, String conversationId, String ip);

    GatewayResult refundPayment(String paymentId, String conversationId, String ip, BigDecimal amount);

    String getGatewayName();
}