package com.example.apps.payments.exceptions;

import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = org.springframework.http.HttpStatus.NOT_FOUND)
public class NoPaymentGatewayFoundException extends RuntimeException {
    public NoPaymentGatewayFoundException(String message) {
        super(message);
    }

    public NoPaymentGatewayFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
