package com.example.apps.payments.exceptions;

import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = org.springframework.http.HttpStatus.BAD_REQUEST)
public class IyzijoPaymentCreateException extends RuntimeException {
    public IyzijoPaymentCreateException(String message) {
        super(message);
    }

    public IyzijoPaymentCreateException(String message, Throwable cause) {
        super(message, cause);
    }

}
