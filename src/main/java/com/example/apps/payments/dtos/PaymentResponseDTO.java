package com.example.apps.payments.dtos;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResponseDTO {
    private String orderNumber;
    private String conversationId;
    private String basketId;
    private String paymentStatus;
    private String iyzicoPageUrl;
    private String token;
    private BigDecimal totalPrice;
    private String currency;

    private String paymentId;
    private String binNumber;
    private String cardFamily;
    private List<TransactionDTO> transactions;
}