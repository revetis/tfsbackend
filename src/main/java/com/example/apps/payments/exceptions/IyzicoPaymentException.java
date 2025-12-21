package com.example.apps.payments.exceptions;

public class IyzicoPaymentException extends RuntimeException {

    public IyzicoPaymentException(String message) {
        super(message);
    }

    public IyzicoPaymentException(String message, Throwable cause) {
        super(message, cause);
    }

}
