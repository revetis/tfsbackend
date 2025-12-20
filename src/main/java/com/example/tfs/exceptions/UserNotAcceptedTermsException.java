package com.example.tfs.exceptions;

import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = org.springframework.http.HttpStatus.FORBIDDEN)
public class UserNotAcceptedTermsException extends RuntimeException {
    public UserNotAcceptedTermsException(String message) {
        super(message);
    }

}
