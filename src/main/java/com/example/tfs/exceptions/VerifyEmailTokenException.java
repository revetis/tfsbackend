package com.example.tfs.exceptions;

import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = org.springframework.http.HttpStatus.FORBIDDEN)
public class VerifyEmailTokenException extends RuntimeException {
    public VerifyEmailTokenException(String message) {
        super(message);
    }

}
