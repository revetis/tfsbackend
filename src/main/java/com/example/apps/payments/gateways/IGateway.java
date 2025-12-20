package com.example.apps.payments.gateways;

import com.example.apps.payments.dtos.PaymentRequestDTO;
import com.example.apps.payments.dtos.PaymentResponseDTO;

public interface IGateway {

    PaymentResponseDTO initializePayment(PaymentRequestDTO request);

    PaymentResponseDTO retrievePaymentDetails(String token, String conversationId);

    String getGatewayName();
}