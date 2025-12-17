package com.example.tfs.exceptions;

public class ForgotPasswordTokenIsInvalidException extends RuntimeException {
    public ForgotPasswordTokenIsInvalidException(String message) {
        super(message);
    }

}
