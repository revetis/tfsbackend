package com.example.apps.orders.exceptions;

import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = org.springframework.http.HttpStatus.BAD_REQUEST)
public class OrderItemException extends RuntimeException {

    public OrderItemException(String message) {
        super(message);
    }

}
