package com.example.apps.orders.exceptions;

public class OrderAlreadyRefundedException extends RuntimeException {
    public OrderAlreadyRefundedException(String message) {
        super(message);
    }
}
