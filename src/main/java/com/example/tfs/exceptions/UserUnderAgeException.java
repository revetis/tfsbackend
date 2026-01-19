package com.example.tfs.exceptions;

import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = org.springframework.http.HttpStatus.BAD_REQUEST)
public class UserUnderAgeException extends RuntimeException {
    public UserUnderAgeException(String message) {
        super(message);
    }
}
