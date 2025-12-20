package com.example.apps.orders.exceptions;

import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = org.springframework.http.HttpStatus.BAD_REQUEST)
public class OrderException extends RuntimeException {

    public OrderException(String message) {
        super(message);
    }

}
