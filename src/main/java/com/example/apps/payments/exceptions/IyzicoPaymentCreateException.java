package com.example.apps.payments.exceptions;

import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = org.springframework.http.HttpStatus.BAD_REQUEST)
public class IyzicoPaymentCreateException extends RuntimeException {
    public IyzicoPaymentCreateException(String message) {
        super(message);
    }

    public IyzicoPaymentCreateException(String message, Throwable cause) {
        super(message, cause);
    }

}
