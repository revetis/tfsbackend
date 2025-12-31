package com.example.apps.payments.gateways.utils;

import com.example.apps.payments.enums.PaymentStatus;

public record GatewayResult(
                PaymentStatus status,
                String paymentId,
                String rawResponse,
                String binNumber,
                String cardAssociation,
                String cardFamily,
                String cardType) {
        public GatewayResult(PaymentStatus status, String paymentId, String rawResponse) {
                this(status, paymentId, rawResponse, null, null, null, null);
        }
}
