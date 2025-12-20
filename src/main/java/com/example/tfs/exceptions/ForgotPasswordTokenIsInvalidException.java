package com.example.tfs.exceptions;

import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = org.springframework.http.HttpStatus.FORBIDDEN)
public class ForgotPasswordTokenIsInvalidException extends RuntimeException {
    public ForgotPasswordTokenIsInvalidException(String message) {
        super(message);
    }

}
