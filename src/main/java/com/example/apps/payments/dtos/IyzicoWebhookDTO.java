package com.example.apps.payments.dtos;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IyzicoWebhookDTO {

    private String iyziEventType; // PAYMENT_API, API_AUTH, etc.
    private String paymentId;
    private String paymentConversationId;
    private String status; // SUCCESS, FAILURE, INIT_THREEDS, etc.
    private BigDecimal paidPrice;
    private String currency;
    private String merchantOrderId;
    private String basketId;
}
