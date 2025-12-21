package com.example.apps.payments.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TransactionDTO {
    private String transactionId;
    private String status;
    private String errorCode;
    private String errorMessage;
    private String rawResponse;

}