package com.example.apps.payments.enums;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    PENDING,
    COMPLETED,
    FAILED,
    REFUNDED,
    CANCELLED
}
