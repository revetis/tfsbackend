package com.example.apps.orders.exceptions;

public class OrderNotRefundableException extends RuntimeException {
    public OrderNotRefundableException(String message) {
        super(message);
    }
}
