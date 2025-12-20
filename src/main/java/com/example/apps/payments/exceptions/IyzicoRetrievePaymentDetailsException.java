package com.example.apps.payments.exceptions;

import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = org.springframework.http.HttpStatus.BAD_REQUEST)
public class IyzicoRetrievePaymentDetailsException extends RuntimeException {
    public IyzicoRetrievePaymentDetailsException(String message) {
        super(message);
    }

    public IyzicoRetrievePaymentDetailsException(String message, Throwable cause) {
        super(message, cause);
    }

}
