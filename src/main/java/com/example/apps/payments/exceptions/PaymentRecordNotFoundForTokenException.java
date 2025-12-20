package com.example.apps.payments.exceptions;

import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = org.springframework.http.HttpStatus.NOT_FOUND)
public class PaymentRecordNotFoundForTokenException extends RuntimeException {
    public PaymentRecordNotFoundForTokenException(String message) {
        super(message);
    }

    public PaymentRecordNotFoundForTokenException(String message, Throwable cause) {
        super(message, cause);
    }

}
