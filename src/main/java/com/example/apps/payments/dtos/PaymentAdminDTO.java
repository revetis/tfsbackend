package com.example.apps.payments.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.apps.payments.enums.Currency;
import com.example.apps.payments.enums.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentAdminDTO {
    private Long id;
    private String orderNumber;
    private String token;
    private String paymentId;
    private BigDecimal totalPrice;
    private BigDecimal paidPrice;
    private Currency currency;
    private PaymentStatus status;
    private String selectedGateway;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String customerEmail;
    private String customerName;
    private String binNumber;
    private String cardAssociation;
    private String cardFamily;
    private String cardType;

    // Missing fields - added
    private String conversationId;
    private String basketId;
    private String ipAddress;
    private java.util.List<TransactionDTO> transactions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionDTO {
        private Long id;
        private String transactionId;
        private String status;
        private BigDecimal paidPrice;
        private LocalDateTime createdAt;
    }
}
