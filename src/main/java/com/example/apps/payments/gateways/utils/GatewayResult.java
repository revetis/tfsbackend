package com.example.apps.payments.gateways.utils;

import com.example.apps.payments.enums.PaymentStatus;

public record GatewayResult(
        PaymentStatus status,
        String paymentId,
        String rawResponse) {
}
