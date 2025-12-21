package com.example.apps.payments.enums;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    PENDING,
    SUCCESS,
    FAILED,
    REFUNDED,
    CANCELLED
}
