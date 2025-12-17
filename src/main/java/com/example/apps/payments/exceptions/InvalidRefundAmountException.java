package com.example.apps.payments.exceptions;

public class InvalidRefundAmountException extends RuntimeException {
    public InvalidRefundAmountException(String message) {
        super(message);
    }
}
