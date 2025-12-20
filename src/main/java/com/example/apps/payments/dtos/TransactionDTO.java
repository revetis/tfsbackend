package com.example.apps.payments.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionDTO {
    private String transactionId;
    private String status;
    private String errorCode;
    private String errorMessage;
    private String rawResponse;

}