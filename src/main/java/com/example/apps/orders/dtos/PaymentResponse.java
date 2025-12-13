package com.example.apps.orders.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {

    private String paymentId;
    private String status; // SUCCESS, FAILURE, PENDING
    private String errorMessage;
    private String transactionId;
    private String conversationId;
}
